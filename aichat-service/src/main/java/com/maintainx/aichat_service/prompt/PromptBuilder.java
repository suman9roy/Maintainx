package com.maintainx.aichat_service.prompt;

import com.maintainx.aichat_service.rag.retrival.RetrievedChunk;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    public Prompt buildPrompt(
            String question,
            List<RetrievedChunk> chunks
    ) {

        StringBuilder context = new StringBuilder();

        for (RetrievedChunk chunk : chunks) {

            context.append("Document: ")
                    .append(chunk.DocumentName())
                    .append("\n");

            if (chunk.pageNumber() != null) {
                context.append("Page: ")
                        .append(chunk.pageNumber())
                        .append("\n");
            }

            context.append(chunk.content())
                    .append("\n\n");
        }

        String systemPrompt = """
You are MaintainX AI Assistant, a helpful assistant for residents and
admins of an apartment community.

For greetings, thanks, or general small talk, respond naturally and
briefly — you do not need document context for these.

For any substantive question about the apartment, community, expenses,
complaints, notices, or policies, you must answer ONLY from the supplied
apartment documents below. Rules for those questions:

1. Never invent information.

2. If the answer is not present in the context,
reply politely that you could not find it, and suggest the resident
contact the office or an admin for further help.

3. Keep answers concise.

4. Mention the source document if applicable.
""";

        String userPrompt = """
Apartment Context

%s

-------------------------

Question

%s
""".formatted(
                context,
                question
        );

        return new Prompt(
                List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPrompt)
                )
        );
    }
}