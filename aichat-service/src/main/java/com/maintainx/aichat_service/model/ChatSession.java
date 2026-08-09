package com.maintainx.aichat_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "apartment_id", length = 100)
    private String apartmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private SessionRole role = SessionRole.RESIDENT;

    @Column(name = "title", length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    public enum SessionRole { RESIDENT, ADMIN }
    public enum SessionStatus { ACTIVE, ARCHIVED }

    protected ChatSession() {
        // JPA
    }

    public ChatSession(String userId, String apartmentId, SessionRole role, String title) {
        this.userId = userId;
        this.apartmentId = apartmentId;
        this.role = role;
        this.title = title;
    }

    public void touch(Instant when) {
        this.lastMessageAt = when;
        this.updatedAt = when;
    }

    // --- getters / setters ---

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getApartmentId() { return apartmentId; }
    public SessionRole getRole() { return role; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastMessageAt() { return lastMessageAt; }
}
