package com.maintainx.aiplatform.dto;

import com.maintainx.aiplatform.model.ChatMessage;

import java.time.Instant;

public record ChatMessageView(
        Long id,
        String sender,
        String content,
        Instant createdAt
) {
    public static ChatMessageView from(ChatMessage m) {
        return new ChatMessageView(m.getId(), m.getSender().name(), m.getContent(), m.getCreatedAt());
    }
}
