package com.maintainx.aichat_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "apartment_id", nullable = false, length = 100)
    private String apartmentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum Status { PENDING, PROCESSING, INDEXED, FAILED }

    protected Document() {
        // JPA
    }

    public Document(String apartmentId, String title, String originalFilename,
                     String contentType, String storagePath, String uploadedBy) {
        this.apartmentId = apartmentId;
        this.title = title;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.uploadedBy = uploadedBy;
    }

    public void markProcessing() {
        this.status = Status.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markIndexed(int chunkCount) {
        this.status = Status.INDEXED;
        this.chunkCount = chunkCount;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getApartmentId() { return apartmentId; }
    public String getTitle() { return title; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public String getStoragePath() { return storagePath; }
    public Status getStatus() { return status; }
    public int getChunkCount() { return chunkCount; }
    public String getErrorMessage() { return errorMessage; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
