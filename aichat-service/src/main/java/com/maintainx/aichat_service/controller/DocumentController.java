package com.maintainx.aichat_service.controller;


import com.maintainx.aichat_service.dto.DocumentUploadResponse;
import com.maintainx.aichat_service.rag.ingestion.DocumentMetadata;
import com.maintainx.aichat_service.security.GatewayConstants;
import com.maintainx.aichat_service.security.GatewayContext;
import com.maintainx.aichat_service.rag.ingestion.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentUploadResponse uploadDocument(

            @RequestHeader(GatewayConstants.USER_ID)
            String userId,

            @RequestHeader(GatewayConstants.ROLE)
            String role,

            @RequestHeader(GatewayConstants.APARTMENT_ID)
            String apartmentId,

            @RequestPart("file")
            MultipartFile file

    ) throws IOException {

        GatewayContext context = new GatewayContext(
                userId,
                role,
                apartmentId
        );

        return documentService.uploadDocument(
                context,
                file
        );
    }

    @GetMapping
    public List<DocumentMetadata> getDocuments(@RequestHeader(GatewayConstants.APARTMENT_ID) String apartmentId) {
        return documentService.getDocuments(apartmentId);
    }
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String fileName,
                                              @RequestHeader(GatewayConstants.APARTMENT_ID) String apartmentId) {
        return documentService.getDocument(fileName, apartmentId);
    }
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<String> deleteDocument(@PathVariable String fileName) {
         return documentService.deleteDocument(fileName);

    }

}