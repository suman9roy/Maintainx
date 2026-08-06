package com.maintainx.aichat_service.service;



import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PromptBuilder {

    private static final String SYSTEM_PROMPT = """
            You are MaintainX AI Assistant.

            Your purpose is to help residents and administrators
            with apartment management related queries.

            You can answer questions related to:

            - Apartment rules
            - Visitor policies
            - Parking policies
            - Complaints
            - Maintenance
            - Notices
            - Payments
            - Expenses
            - Society documents

            If a user asks something unrelated to apartment
            management, politely refuse and respond:

            "I'm MaintainX AI and I can only assist with
            apartment management related queries."

            Keep responses professional,
            concise,
            and factual.
            """;

    public String buildPrompt(String userMessage) {

        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("User message cannot be blank");
        }

        return SYSTEM_PROMPT +
                System.lineSeparator() +
                System.lineSeparator() +
                "User: " +
                userMessage.trim();
    }
}