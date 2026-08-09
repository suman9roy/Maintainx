package com.maintainx.aichat_service.repository;


import com.maintainx.aichat_service.model.AiDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiDocumentRepository
        extends JpaRepository<AiDocument, UUID> {

    List<AiDocument> findByApartmentId(String apartmentId);

    void deleteByStoredFileName(String fileName);

    AiDocument findByStoredFileName(String fileName);
}