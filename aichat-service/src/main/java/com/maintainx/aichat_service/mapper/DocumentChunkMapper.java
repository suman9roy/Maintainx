package com.maintainx.aichat_service.mapper;



import com.maintainx.aichat_service.model.AiDocument;
import com.maintainx.aichat_service.model.DocumentChunk;
import com.maintainx.aichat_service.rag.chunking.DocumentChunkRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DocumentChunkMapper {

    public DocumentChunk toEntity(
            AiDocument document,
            DocumentChunkRequest request
    ) {

        return DocumentChunk.builder()
                .document(document)
                .apartmentId(document.getApartmentId())
                .chunkIndex(request.chunkIndex())
                .content(request.content())
                .pageNumber(request.pageNumber())
                .section(request.section())
                .tokenCount(request.tokenCount())
                .createdAt(Instant.now())
                .build();

    }

}