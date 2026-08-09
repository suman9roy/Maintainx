package com.maintainx.aichat_service.model;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_document_chunks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private AiDocument document;

    @Column(nullable = false)
    private String apartmentId;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Stored inside PostgreSQL pgvector.
     * 
     * Not persisted by Hibernate. Embeddings are stored via raw SQL
     * to properly handle pgvector type conversion.
     */
    @Transient
    private String embedding;

    private Integer pageNumber;

    private String section;

    private Integer tokenCount;

    @Column(nullable = false)
    private Instant createdAt;
}