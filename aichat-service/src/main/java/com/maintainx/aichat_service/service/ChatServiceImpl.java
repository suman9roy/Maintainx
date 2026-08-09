package com.maintainx.aichat_service.service;

import com.maintainx.aichat_service.dto.ChatRequest;
import com.maintainx.aichat_service.dto.ChatResponse;
import com.maintainx.aichat_service.prompt.PromptBuilder;
import com.maintainx.aichat_service.rag.retrival.RetrievalService;
import com.maintainx.aichat_service.rag.retrival.RetrievedChunk;
import com.maintainx.aichat_service.security.GatewayContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final ResponseBuilder responseBuilder;
    private final RetrievalService retrievalService;

    @Override
    public ChatResponse chat(
            GatewayContext context,
            ChatRequest request
    ) {

        long start = System.currentTimeMillis();
        log.info("Processing chat request for user: {} in apartment: {}", context.userId(), context.apartmentId());
        // Step 1 : Retrieve relevant chunks
        List<RetrievedChunk> retrievedChunks =
                retrievalService.retrieve(
                        context.apartmentId(),
                        request.message(),
                        5
                );
        log.info("Retrieved {} chunks for user: {}", retrievedChunks.size(), context.userId());

        // Step 2 : Build RAG prompt
        Prompt prompt =
                promptBuilder.buildPrompt(
                        request.message(),
                        retrievedChunks
                );
        log.info("RAG prompt content: {}", prompt.toString());
        log.info("Built RAG prompt for user: {}", context.userId());

        // Step 3 : Send prompt to LLM
        String reply = chatClient.prompt(prompt)
                .call()
                .content();
        log.info("Received reply for user: {}", context.userId());
        log.info("Reply content: {}", reply);
        long tookMs = System.currentTimeMillis() - start;

        UUID sessionId = request.sessionId() == null
                ? UUID.randomUUID()
                : request.sessionId();

        return responseBuilder.build(
                sessionId,
                reply,
                "qwen2.5:3b",
                tookMs
        );
    }
}