package com.maintainx.aichat_service.rag.processing;



import java.util.UUID;

public interface DocumentProcessingService {

    /**
     * Executes the complete RAG ingestion pipeline
     * for an uploaded document.
     */
    void process(UUID documentId);

}