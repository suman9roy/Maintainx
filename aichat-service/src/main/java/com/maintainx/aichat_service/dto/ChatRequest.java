package com.maintainx.aichat_service.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Sprint 3: {@code sessionId} is optional.
 * Omit it to start a new conversation; pass the one returned by a
 * previous {@link ChatResponse} to continue it with full memory.
 */
public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        String message,

        UUID sessionId
) {}
