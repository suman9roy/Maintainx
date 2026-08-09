package com.maintainx.aichat_service.rag.vector;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class PgVectorRepository implements VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveEmbedding(
            UUID chunkId,
            float[] embedding
    ) {

        String vector = toVectorLiteral(embedding);

        jdbcTemplate.update(
                """
                UPDATE ai_document_chunks
                   SET embedding = ?::vector
                 WHERE id = ?
                """,
                vector,
                chunkId
        );
    }

    private String toVectorLiteral(float[] embedding) {

        return IntStream.range(0, embedding.length)
                .mapToObj(i -> Float.toString(embedding[i]))
                .collect(Collectors.joining(",", "[", "]"));
    }
}