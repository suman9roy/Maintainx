package com.maintainx.aichat_service.service;


import com.maintainx.aichat_service.dto.ChatResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
public class ResponseBuilder {

    public ChatResponse build(
            UUID sessionId,
            String reply,
            String model,
            long tookMs
    ) {

        return new ChatResponse(
                sessionId,
                reply,
                model,
                tookMs,
                Instant.now(),
                Collections.emptyList()
        );
    }
}