package com.maintainx.aichat_service.dto;


import com.maintainx.aichat_service.model.AiDocument;

import java.time.Instant;
import java.util.UUID;

public record DocumentView(
        UUID id,
        String apartmentId,
        String uploadedBy,
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize,
        String storageLocation,
        String status,
        Instant uploadedAt,
        Instant processedAt
) {
    public static DocumentView from(AiDocument d) {
        return new DocumentView(
                d.getId(), d.getApartmentId(), d.getUploadedBy(), d.getOriginalFileName(),
                d.getStoredFileName(), d.getContentType(), d.getFileSize(), d.getStorageLocation(),
                d.getStatus().name(), d.getUploadedAt(), d.getProcessedAt()
        );
    }
}
