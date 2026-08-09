package com.maintainx.aichat_service.repository.vector;

import com.maintainx.aichat_service.rag.retrival.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PgVectorDocumentChunkRepository
        implements DocumentChunkVectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void updateEmbedding(
            UUID chunkId,
            float[] embedding
    ) {

        jdbcTemplate.update(
                """
                UPDATE ai_document_chunks
                   SET embedding = ?::vector
                 WHERE id = ?
                """,
                toVectorLiteral(embedding),
                chunkId
        );

    }

    @Override
    public List<RetrievedChunk> similaritySearch(
            String apartmentId,
            float[] embedding,
            int topK
    ) {

        String sql = """
            SELECT
                c.id,
                c.document_id,
                d.original_file_name,
                c.content,
                c.page_number,
                c.section,
                1 - (c.embedding <=> ?::vector) AS similarity
            FROM ai_document_chunks c
            JOIN ai_documents d
              ON d.id = c.document_id
            WHERE c.apartment_id = ?
              AND c.embedding IS NOT NULL
            ORDER BY c.embedding <=> ?::vector
            LIMIT ?
            """;

        String vector = toVectorLiteral(embedding);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RetrievedChunk(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("document_id")),
                        rs.getString("original_file_name"),
                        rs.getString("content"),
                        rs.getObject("page_number", Integer.class),
                        rs.getString("section"),
                        rs.getDouble("similarity")
                ),
                vector,
                apartmentId,
                vector,
                topK
        );
    }

    @Override
    public void deleteByDocumentId(UUID documentId) {

        jdbcTemplate.update(
                """
                DELETE FROM ai_document_chunks
                WHERE document_id = ?
                """,
                documentId
        );

    }

    private String toVectorLiteral(float[] embedding) {

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(embedding[i]);
        }

        builder.append("]");

        return builder.toString();
    }
}