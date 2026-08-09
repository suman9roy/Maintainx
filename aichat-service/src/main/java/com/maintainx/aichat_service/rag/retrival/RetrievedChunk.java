package com.maintainx.aichat_service.rag.retrival;



import java.util.UUID;

public record RetrievedChunk(

        UUID chunkId,

        UUID documentId,

        String DocumentName,

        String content,

        Integer pageNumber,

        String section,

        Double similarity

) {
}