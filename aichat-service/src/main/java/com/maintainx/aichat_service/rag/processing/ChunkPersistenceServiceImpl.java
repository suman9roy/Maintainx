package com.maintainx.aichat_service.rag.processing;



import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.model.DocumentChunk;
import com.maintainx.aichat_service.mapper.DocumentChunkMapper;
import com.maintainx.aichat_service.rag.chunking.DocumentChunkRequest;
import com.maintainx.aichat_service.rag.embedding.EmbeddingService;
import com.maintainx.aichat_service.repository.DocumentChunkJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkPersistenceServiceImpl
        implements ChunkPersistenceService {

    private final EmbeddingService embeddingService;

    private final DocumentChunkMapper mapper;

    private final DocumentChunkJpaRepository repository;

    @Override
    public void persist(
            AiDocument document,
            List<DocumentChunkRequest> chunks
    ) {

        for (DocumentChunkRequest chunk : chunks) {
            log.info(
                    "Persisting chunk {} for document {}",
                    chunk.content(),
                    document.getId()
            );
            float[] embedding =
                    embeddingService.generateEmbedding(
                            chunk.content()
                    );

            String embeddingVector = toVectorLiteral(embedding);

            repository.insertChunkWithEmbedding(
                    UUID.randomUUID(),
                    document.getApartmentId(),
                    chunk.chunkIndex(),
                    chunk.content(),
                    Instant.now(),
                    document.getId(),
                    embeddingVector,
                    chunk.pageNumber(),
                    chunk.section(),
                    chunk.tokenCount()
            );

            log.info(
                    "Successfully persisted chunk for document {}",
                    document.getId()
            );

        }

    }

    private String toVectorLiteral(float[] embedding) {
        return IntStream.range(0, embedding.length)
                .mapToObj(i -> Float.toString(embedding[i]))
                .collect(Collectors.joining(",", "[", "]"));
    }

}