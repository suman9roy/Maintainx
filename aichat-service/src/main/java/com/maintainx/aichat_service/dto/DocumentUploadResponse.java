package com.maintainx.aichat_service.dto;



import java.time.Instant;

public record DocumentUploadResponse(

        String fileName,

        long fileSize,

        Instant uploadedAt,

        String message

) {
}