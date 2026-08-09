package com.maintainx.aichat_service.rag.processing;



import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.model.DocumentStatus;
import com.maintainx.aichat_service.rag.chunking.ChunkingService;
import com.maintainx.aichat_service.rag.chunking.DocumentChunkRequest;
import com.maintainx.aichat_service.rag.extraction.TextExtractionService;
import com.maintainx.aichat_service.repository.AiDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingServiceImpl
        implements DocumentProcessingService {

    private final AiDocumentRepository documentRepository;

    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;
    private final ChunkPersistenceService chunkPersistenceService;

    @Override
    public void process(UUID documentId) {

        log.info("Processing document {}", documentId);

        AiDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found: " + documentId
                        ));

        try {

            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            String extractedText =
                    textExtractionService.extract(
                            Path.of(document.getStorageLocation())
                    );

            log.info(
                    "Successfully extracted {} characters from {}",
                    extractedText.length(),
                    document.getOriginalFileName()
            );

            /*
             * Sprint 2
             *
             * Next Steps:
             *
             * ChunkingService
             * ↓
             * EmbeddingService
             * ↓
             * Save Chunks
             */
            List<DocumentChunkRequest> chunks = chunkingService.chunk(extractedText);
            chunkPersistenceService.persist(document, chunks);

            document.setStatus(DocumentStatus.COMPLETED);
            document.setProcessedAt(java.time.Instant.now());

            documentRepository.save(document);

        } catch (Exception ex) {

            log.error(
                    "Failed to process document {}",
                    documentId,
                    ex
            );

            document.setStatus(DocumentStatus.FAILED);

            documentRepository.save(document);
        }

    }

}