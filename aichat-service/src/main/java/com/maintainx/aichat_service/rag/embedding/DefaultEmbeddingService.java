package com.maintainx.aichat_service.rag.embedding;



import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultEmbeddingService
        implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] generateEmbedding(String text) {

        return embeddingModel.embed(text);

    }
}