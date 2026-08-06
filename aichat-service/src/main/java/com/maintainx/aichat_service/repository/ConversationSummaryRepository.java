package com.maintainx.aiplatform.repository;

import com.maintainx.aiplatform.model.ConversationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {

    Optional<ConversationSummary> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);
}
