package com.maintainx.aichat_service.mapper;

import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.model.DocumentStatus;
import com.maintainx.aichat_service.rag.ingestion.DocumentMetadata;
import com.maintainx.aichat_service.security.GatewayContext;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public AiDocument  toEntity(
            GatewayContext context,
            DocumentMetadata metadata
    ) {

        return AiDocument.builder()
                .apartmentId(context.apartmentId())
                .uploadedBy(context.userId())
                .originalFileName(metadata.fileName())
                .storedFileName(metadata.storedFileName())
                .contentType(metadata.contentType())
                .fileSize(metadata.fileSize())
                .storageLocation(metadata.storageLocation())
                .status(DocumentStatus.UPLOADED)
                .uploadedAt(metadata.uploadedAt())
                .build();
    }

    public DocumentMetadata toMetadata(AiDocument aiDocument) {
        return DocumentMetadata.builder()
                .fileName(aiDocument.getOriginalFileName())
                .storedFileName(aiDocument.getStoredFileName())
                .contentType(aiDocument.getContentType())
                .fileSize(aiDocument.getFileSize())
                .storageLocation(aiDocument.getStorageLocation())
                .uploadedAt(aiDocument.getUploadedAt())
                .build();
    }
}