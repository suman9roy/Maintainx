package com.maintainx.aichat_service.controller;

import com.maintainx.aichat_service.dto.ChatRequest;
import com.maintainx.aichat_service.dto.ChatResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 1 chat endpoint.
 *
 * Deliberately minimal: a single stateless prompt straight to the model,
 * no RAG (Sprint 2), no memory (Sprint 3), no tool calling (Sprint 6).
 * This exists to prove the wiring (Spring AI + Security + Gateway + Eureka)
 * end to end before layering intelligence on top.
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private static final String SYSTEM_PROMPT = """
            You are the MaintainX AI Assistant. You only help with apartment
            management topics: society rules, maintenance, complaints, visitors,
            parking, payments, notices, and amenities. If asked about anything
            else, politely say you can only assist with apartment management.
            """;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final String modelName;

    public ChatController(ChatClient.Builder chatClientBuilder,
                           @Value("${SPRING_AI_OLLAMA_CHAT_MODEL_NAME:${spring.ai.openai.chat.options.model:gpt-4o-mini}}") String modelName) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.modelName = modelName;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        long start = System.currentTimeMillis();
        log.info("Received chat request: {}", request.message());
        log.info("Using model: {}", modelName);
        String reply = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        log.info("Generated chat response: {}", reply);

        long took = System.currentTimeMillis() - start;
        return ResponseEntity.ok(new ChatResponse(reply, modelName, took));
    }
}
