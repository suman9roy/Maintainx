package com.maintainx.aichat_service.service;



import com.maintainx.aichat_service.dto.ChatRequest;
import com.maintainx.aichat_service.dto.ChatResponse;
import com.maintainx.aichat_service.security.GatewayContext;

public interface ChatService {

    ChatResponse chat(
            GatewayContext context,
            ChatRequest request
    );

}