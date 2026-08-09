package com.maintainx.aichat_service.rag.ingestion;



import com.maintainx.aichat_service.dto.DocumentUploadResponse;
import com.maintainx.aichat_service.security.GatewayContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    DocumentUploadResponse uploadDocument(
            GatewayContext context,
            MultipartFile file
    ) throws IOException;

    List<DocumentMetadata> getDocuments(String apartmentId);

    ResponseEntity<byte[]> getDocument(String fileName, String apartmentId);

    ResponseEntity<String> deleteDocument(String fileName);
}