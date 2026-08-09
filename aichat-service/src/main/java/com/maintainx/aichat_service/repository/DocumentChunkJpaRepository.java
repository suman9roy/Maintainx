package com.maintainx.aichat_service.repository;


import com.maintainx.aichat_service.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkJpaRepository
        extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByDocumentId(UUID documentId);

    List<DocumentChunk> findByApartmentId(String apartmentId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO ai_document_chunks 
            (id, apartment_id, chunk_index, content, created_at, document_id, 
             embedding, page_number, section, token_count) 
            VALUES 
            (?1, ?2, ?3, ?4, ?5, ?6, CAST(?7 AS vector), ?8, ?9, ?10)
            """, nativeQuery = true)
    void insertChunkWithEmbedding(
            UUID id,
            String apartmentId,
            Integer chunkIndex,
            String content,
            java.time.Instant createdAt,
            UUID documentId,
            String embedding,
            Integer pageNumber,
            String section,
            Integer tokenCount
    );

}