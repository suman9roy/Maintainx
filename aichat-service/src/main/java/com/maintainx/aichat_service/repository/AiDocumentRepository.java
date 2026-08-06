package com.maintainx.aichat_service.repository;

import com.maintainx.aichat_service.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByApartmentIdOrderByCreatedAtDesc(String apartmentId);
}
