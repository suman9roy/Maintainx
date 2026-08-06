package com.maintainx.aichat_service.rag.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A rolling summary of everything in the session up to (and including)
 * {@code coveredUpToMsgId}. When the live message window grows past the
 * token budget, MemoryService compacts the oldest turns into one of
 * these so context survives without re-sending the full transcript.
 */
@Entity
@Table(name = "conversation_summary")
public class ConversationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Lob
    @Column(name = "summary_text", nullable = false)
    private String summaryText;

    @Column(name = "covered_up_to_msg_id", nullable = false)
    private Long coveredUpToMsgId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ConversationSummary() {
        // JPA
    }

    public ConversationSummary(UUID sessionId, String summaryText, Long coveredUpToMsgId) {
        this.sessionId = sessionId;
        this.summaryText = summaryText;
        this.coveredUpToMsgId = coveredUpToMsgId;
    }

    public Long getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public String getSummaryText() { return summaryText; }
    public Long getCoveredUpToMsgId() { return coveredUpToMsgId; }
    public Instant getCreatedAt() { return createdAt; }
}
