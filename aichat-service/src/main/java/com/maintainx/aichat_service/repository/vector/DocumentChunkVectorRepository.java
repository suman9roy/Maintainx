package com.maintainx.aichat_service.repository.vector;

import com.maintainx.aichat_service.rag.retrival.RetrievedChunk;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkVectorRepository {

    /**
     * Updates the embedding vector for an already persisted chunk.
     */
    void updateEmbedding(
            UUID chunkId,
            float[] embedding
    );

    /**
     * Performs vector similarity search within an apartment.
     */
    List<RetrievedChunk> similaritySearch(
            String apartmentId,
            float[] queryEmbedding,
            int topK
    );

    /**
     * Removes all vectors belonging to a document.
     */
    void deleteByDocumentId(UUID documentId);

}