package com.maintainx.aichat_service.rag.ingestion;

import com.maintainx.aichat_service.security.GatewayContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentStorageService {

    /**
     * Stores an uploaded document and returns its metadata.
     */
    DocumentMetadata store(
            GatewayContext context,
            MultipartFile file
    ) throws IOException;

    void delete(String fileName);
}