package com.maintainx.aichat_service.rag.ingestion;

import com.maintainx.aichat_service.dto.DocumentUploadResponse;
import com.maintainx.aichat_service.kafka.producer.DocumentEventPublisher;
import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.mapper.DocumentMapper;
import com.maintainx.aichat_service.repository.AiDocumentRepository;
import com.maintainx.aichat_service.security.GatewayContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentStorageService documentStorageService;
    private final AiDocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentEventPublisher documentEventPublisher;
    @Override
    public DocumentUploadResponse uploadDocument(
            GatewayContext context,
            MultipartFile file
    ) throws IOException {

        // Step 1 : Store file on disk
        DocumentMetadata metadata = documentStorageService.store(
                context,
                file
        );
        log.info("Document uploaded: {}", metadata.storedFileName());
        log.debug("Document metadata: {}", metadata);

        // Step 2 : Convert to Entity
        AiDocument document = documentMapper.toEntity(
                context,
                metadata
        );

        // Step 3 : Persist metadata
        AiDocument savedDocument = documentRepository.save(document);

        documentEventPublisher.publishDocumentUploaded(savedDocument.getId());

        // Step 4 : Build response
        return new DocumentUploadResponse(
                savedDocument.getOriginalFileName(),
                savedDocument.getFileSize(),
                savedDocument.getUploadedAt(),
                "Document uploaded successfully."
        );
    }

    @Override
    public List<DocumentMetadata> getDocuments(String apartmentId) {
        return documentRepository.findByApartmentId(apartmentId).stream()
                .map(documentMapper::toMetadata)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<byte[]> getDocument(String fileName,
                                              String apartmentId) throws ResourceNotFoundException {
        // Implement logic to retrieve the document from storage and return it as a ResponseEntity
        // For example, you can read the file from disk and return it as a byte array
        AiDocument document = documentRepository.findByStoredFileName(fileName);
        if (document == null || !document.getApartmentId().equals(apartmentId)) {
            return ResponseEntity.notFound().build();
        }
        try {


            Path filePath = Paths.get(document.getStorageLocation());
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException(
                        "Document file not found on server. It may have been moved or deleted."
                );
            }

            return ResponseEntity.ok(Files.readAllBytes(filePath));
        } catch (IOException |OutOfMemoryError |SecurityException e) {
            throw new RuntimeException(
                    "Failed to read document file: " + fileName,
                    e
            );
        }

    }

    @Override
    public ResponseEntity<String> deleteDocument(String fileName) {
        documentStorageService.delete(fileName);
        documentRepository.deleteByStoredFileName(fileName);
        return ResponseEntity.ok("Document deleted successfully.");
    }

}