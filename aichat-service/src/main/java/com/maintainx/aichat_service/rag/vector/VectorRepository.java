package com.maintainx.aichat_service.rag.vector;

import java.util.UUID;

public interface VectorRepository {

    void saveEmbedding(
            UUID chunkId,
            float[] embedding
    );

}