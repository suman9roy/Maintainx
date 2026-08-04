package com.maintainx.aichat_service.dto;

public record ChatResponse(
        String reply,
        String model,
        long tookMs
) {}
