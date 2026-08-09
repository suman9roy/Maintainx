package com.maintainx.aichat_service.repository;


import com.maintainx.aichat_service.model.ConversationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {

    Optional<ConversationSummary> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);
}
