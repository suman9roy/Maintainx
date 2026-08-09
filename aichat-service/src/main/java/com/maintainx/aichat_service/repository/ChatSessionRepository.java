package com.maintainx.aichat_service.repository;


import com.maintainx.aichat_service.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

    List<ChatSession> findByUserIdAndStatusOrderByUpdatedAtDesc(String userId, ChatSession.SessionStatus status);

}
