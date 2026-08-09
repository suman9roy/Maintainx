package com.maintainx.aichat_service.rag.retrival;



import com.maintainx.aichat_service.model.DocumentChunk;

import java.util.List;

public interface RetrievalService {

    List<RetrievedChunk> retrieve(
            String apartmentId,
            String question,
            int topK
    );

}
