package com.maintainx.aichat_service.rag.retrival;

import com.maintainx.aichat_service.model.DocumentChunk;
import com.maintainx.aichat_service.rag.embedding.EmbeddingService;

import com.maintainx.aichat_service.repository.vector.DocumentChunkVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalServiceImpl
        implements RetrievalService {

    private final EmbeddingService embeddingService;

    private final DocumentChunkVectorRepository vectorRepository;

    @Override
    public List<RetrievedChunk> retrieve(
            String apartmentId,
            String question,
            int topK
    ) {

        float[] embedding =
                embeddingService.generateEmbedding(question);

        return vectorRepository.similaritySearch(
                apartmentId,
                embedding,
                topK
        );

    }

}