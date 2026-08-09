package com.maintainx.aichat_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID sessionId,
        String reply,
        String model,
        long tookMs,
        Instant timestamp,
        List<Citation> citations
) {}
