package com.maintainx.aichat_service.dto;

import com.maintainx.aichat_service.model.ChatSession;

import java.time.Instant;
import java.util.UUID;

public record ChatSessionSummary(
        UUID id,
        String title,
        String role,
        String status,
        Instant createdAt,
        Instant lastMessageAt
) {
    public static ChatSessionSummary from(ChatSession s) {
        return new ChatSessionSummary(
                s.getId(), s.getTitle(), s.getRole().name(), s.getStatus().name(),
                s.getCreatedAt(), s.getLastMessageAt()
        );
    }
}
