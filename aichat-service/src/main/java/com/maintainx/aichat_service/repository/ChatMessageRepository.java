package com.maintainx.aichat_service.repository;


import com.maintainx.aichat_service.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<ChatMessage> findBySessionIdAndIdGreaterThanOrderByCreatedAtAsc(UUID sessionId, Long afterId);

    List<ChatMessage> findBySessionIdOrderByCreatedAtDesc(UUID sessionId, Pageable pageable);

    long countBySessionId(UUID sessionId);
}
