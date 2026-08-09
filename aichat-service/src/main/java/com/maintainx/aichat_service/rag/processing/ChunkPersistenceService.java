package com.maintainx.aichat_service.rag.processing;



import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.rag.chunking.DocumentChunkRequest;

import java.util.List;

public interface ChunkPersistenceService {

    void persist(
            AiDocument document,
            List<DocumentChunkRequest> chunks
    );

}