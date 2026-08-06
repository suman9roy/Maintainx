package com.maintainx.aichat_service.service;

package com.maintainx.aichat_service.service;

import com.maintainx.aichat_service.dto.ChatRequest;
import com.maintainx.aichat_service.dto.ChatResponse;
import com.maintainx.aichat_service.security.GatewayContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final ResponseBuilder responseBuilder;

    @Override
    public ChatResponse chat(
            GatewayContext context,
            ChatRequest request
    ) {

        long start = System.currentTimeMillis();

        String prompt = promptBuilder.buildPrompt(
                request.message()
        );

        String reply = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        long tookMs = System.currentTimeMillis() - start;

        UUID sessionId = request.sessionId() == null
                ? UUID.randomUUID()
                : request.sessionId();

        return responseBuilder.build(
                sessionId,
                reply,
                "qwen2.5:3b",
                tookMs
        );
    }
}