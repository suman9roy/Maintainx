package com.maintainx.aichat_service.controller;

import com.maintainx.aichat_service.dto.ChatRequest;
import com.maintainx.aichat_service.dto.ChatResponse;
import com.maintainx.aichat_service.security.GatewayConstants;
import com.maintainx.aichat_service.security.GatewayContext;
import com.maintainx.aichat_service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor

public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestHeader(GatewayConstants.USER_ID) String userId,
            @RequestHeader(GatewayConstants.ROLE) String role,
            @RequestHeader(GatewayConstants.APARTMENT_ID) String apartmentId,
            @Valid @RequestBody ChatRequest request
    ) {

        GatewayContext context = new GatewayContext(
                userId,
                role,
                apartmentId
        );
        log.info("Received chat request for user: {}", userId);
        return chatService.chat(context, request);
    }
}