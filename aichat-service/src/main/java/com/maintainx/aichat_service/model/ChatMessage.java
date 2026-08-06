package com.maintainx.aichat_service.rag.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 20)
    private Sender sender;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum Sender { USER, ASSISTANT, SYSTEM }

    protected ChatMessage() {
        // JPA
    }

    public ChatMessage(UUID sessionId, Sender sender, String content, int tokenCount) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
        this.tokenCount = tokenCount;
    }

    public Long getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public Sender getSender() { return sender; }
    public String getContent() { return content; }
    public int getTokenCount() { return tokenCount; }
    public Instant getCreatedAt() { return createdAt; }
}
