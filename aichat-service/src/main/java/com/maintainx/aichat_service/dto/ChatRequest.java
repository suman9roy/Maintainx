package com.maintainx.aichat_service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Sprint 1 chat request: a single stateless prompt.
 * Sessions, history, and memory retrieval are introduced in Sprint 3.
 */
public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        String message
) {}
