package com.maintainx.aichat_service.rag.processing;



import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.model.DocumentChunk;
import com.maintainx.aichat_service.mapper.DocumentChunkMapper;
import com.maintainx.aichat_service.rag.chunking.DocumentChunkRequest;
import com.maintainx.aichat_service.rag.embedding.EmbeddingService;
import com.maintainx.aichat_service.rag.vector.VectorRepository;
import com.maintainx.aichat_service.repository.DocumentChunkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkPersistenceServiceImpl
        implements ChunkPersistenceService {

    private final EmbeddingService embeddingService;

    private final DocumentChunkMapper mapper;

    private final DocumentChunkJpaRepository repository;

    private final VectorRepository vectorRepository;

    @Override
    public void persist(
            AiDocument document,
            List<DocumentChunkRequest> chunks
    ) {

        for (DocumentChunkRequest chunk : chunks) {

            float[] embedding =
                    embeddingService.generateEmbedding(
                            chunk.content()
                    );

            DocumentChunk entity =
                    mapper.toEntity(document, chunk);

            entity = repository.save(entity);

            vectorRepository.saveEmbedding(
                    entity.getId(),
                    embedding
            );

        }

    }

}