package com.maintainx.aichat_service.memory;


import com.maintainx.aichat_service.model.ChatMessage;
import com.maintainx.aichat_service.model.ChatSession;
import com.maintainx.aichat_service.model.ConversationSummary;
import com.maintainx.aichat_service.repository.ChatMessageRepository;
import com.maintainx.aichat_service.repository.ChatSessionRepository;
import com.maintainx.aichat_service.repository.ConversationSummaryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Sprint 3 — Conversation Memory.
 *
 * Responsible for:
 *  - creating/resuming chat sessions
 *  - persisting every user/assistant turn
 *  - assembling the context window sent to the model: [rolling summary]
 *    + [recent messages that fit inside the token budget]
 *  - compacting old turns into a summary once the window grows too large
 */
@Service
public class MemoryService {

    /** Total tokens we're willing to spend on history + summary per request. */
    private final int contextTokenBudget;

    /** Once uncompacted history exceeds this many tokens, summarize the oldest turns. */
    private final int summarizeThresholdTokens;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ConversationSummaryRepository summaryRepository;
    private final TokenEstimator tokenEstimator;
    private final ChatClient chatClient;

    public MemoryService(ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository,
                          ConversationSummaryRepository summaryRepository,
                          TokenEstimator tokenEstimator,
                          ChatClient.Builder chatClientBuilder,
                          @Value("${maintainx.ai-platform.memory.context-token-budget:3000}") int contextTokenBudget,
                          @Value("${maintainx.ai-platform.memory.summarize-threshold-tokens:2000}") int summarizeThresholdTokens) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.summaryRepository = summaryRepository;
        this.tokenEstimator = tokenEstimator;
        this.chatClient = chatClientBuilder.build();
        this.contextTokenBudget = contextTokenBudget;
        this.summarizeThresholdTokens = summarizeThresholdTokens;
    }

    @Transactional
    public ChatSession getOrCreateSession(UUID sessionId, String userId, String apartmentId,
                                          ChatSession.SessionRole role) {
        if (sessionId != null) {
            ChatSession existing = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown session: " + sessionId));
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Session does not belong to this user");
            }
            return existing;
        }
        ChatSession session = new ChatSession(userId, apartmentId, role, null);
        return sessionRepository.save(session);
    }

    @Transactional
    public ChatMessage appendMessage(UUID sessionId, ChatMessage.Sender sender, String content) {
        int tokens = tokenEstimator.estimate(content);
        ChatMessage message = messageRepository.save(new ChatMessage(sessionId, sender, content, tokens));

        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.touch(Instant.now());
            if (session.getTitle() == null && sender == ChatMessage.Sender.USER) {
                session.setTitle(shorten(content, 60));
            }
            sessionRepository.save(session);
        });

        maybeSummarize(sessionId);
        return message;
    }

    /**
     * Builds the context to send to the model: an optional rolling summary
     * of everything older, followed by the most recent messages that still
     * fit inside {@code contextTokenBudget}.
     */
    @Transactional(readOnly = true)
    public ConversationContext buildContext(UUID sessionId) {
        Optional<ConversationSummary> latestSummary =
                summaryRepository.findTopBySessionIdOrderByCreatedAtDesc(sessionId);

        long coveredUpTo = latestSummary.map(ConversationSummary::getCoveredUpToMsgId).orElse(0L);
        List<ChatMessage> tail = messageRepository
                .findBySessionIdAndIdGreaterThanOrderByCreatedAtAsc(sessionId, coveredUpTo);

        int summaryTokens = latestSummary.map(s -> tokenEstimator.estimate(s.getSummaryText())).orElse(0);
        int budgetLeft = Math.max(0, contextTokenBudget - summaryTokens);

        // Keep the most recent messages within budget; drop oldest of the tail first if needed.
        List<ChatMessage> included = new ArrayList<>();
        int running = 0;
        for (int i = tail.size() - 1; i >= 0; i--) {
            ChatMessage m = tail.get(i);
            running += Math.max(m.getTokenCount(), 1);
            if (running > budgetLeft && !included.isEmpty()) {
                break;
            }
            included.add(0, m);
        }

        return new ConversationContext(
                latestSummary.map(ConversationSummary::getSummaryText).orElse(null),
                included
        );
    }

    /**
     * If the uncompacted tail has grown past the threshold, ask the model to
     * summarize the oldest half of it and persist that as the new rolling
     * summary, so future context windows stay bounded regardless of how
     * long the conversation runs.
     */
    @Transactional
    protected void maybeSummarize(UUID sessionId) {
        Optional<ConversationSummary> latestSummary =
                summaryRepository.findTopBySessionIdOrderByCreatedAtDesc(sessionId);
        long coveredUpTo = latestSummary.map(ConversationSummary::getCoveredUpToMsgId).orElse(0L);

        List<ChatMessage> tail = messageRepository
                .findBySessionIdAndIdGreaterThanOrderByCreatedAtAsc(sessionId, coveredUpTo);

        int tailTokens = tail.stream().mapToInt(ChatMessage::getTokenCount).sum();
        if (tailTokens < summarizeThresholdTokens || tail.size() < 4) {
            return;
        }

        int splitIndex = tail.size() / 2;
        List<ChatMessage> toCompact = tail.subList(0, splitIndex);

        StringBuilder transcript = new StringBuilder();
        latestSummary.ifPresent(s -> transcript.append("Previous summary: ").append(s.getSummaryText()).append("\n\n"));
        for (ChatMessage m : toCompact) {
            transcript.append(m.getSender()).append(": ").append(m.getContent()).append("\n");
        }

        String newSummary = chatClient.prompt()
                .system("""
                        Summarize the following apartment-management conversation so far
                        in 4-6 concise sentences. Preserve concrete facts (unit numbers,
                        dates, amounts, complaint IDs, decisions made). Do not add
                        commentary, greetings, or anything not present in the transcript.
                        """)
                .user(transcript.toString())
                .call()
                .content();

        Long newCoveredUpTo = toCompact.get(toCompact.size() - 1).getId();
        summaryRepository.save(new ConversationSummary(sessionId, newSummary, newCoveredUpTo));
    }

    @Transactional(readOnly = true)
    public List<ChatSession> listSessions(String userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getFullHistory(UUID sessionId, String userId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown session: " + sessionId));
        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session does not belong to this user");
        }
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private String shorten(String text, int maxLen) {
        String trimmed = text.strip();
        return trimmed.length() <= maxLen ? trimmed : trimmed.substring(0, maxLen - 1) + "…";
    }

    /** Assembled context ready to hand to the ChatClient for the next turn. */
    public record ConversationContext(String rollingSummary, List<ChatMessage> recentMessages) {}
}
