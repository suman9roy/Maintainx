package com.maintainx.aichat_service.rag.ingestion;



import jakarta.ws.rs.core.EntityPart;
import lombok.Builder;

import java.time.Instant;
@Builder
public record DocumentMetadata(

        String apartmentId,

        String fileName,

        String storedFileName,

        String contentType,

        long fileSize,

        String storageLocation,

        Instant uploadedAt

) {

}