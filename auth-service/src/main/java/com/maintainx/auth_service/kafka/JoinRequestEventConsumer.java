package com.maintainx.auth_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.event.JoinRequestStatusEvent;
import com.maintainx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Listens to join-request-status-topic published by resident-service when
 * an admin approves or rejects a join request.
 *
 * On APPROVED, resident-service creates a Resident row in its own DB, but
 * the JWT this service issues is built from Users.apartmentId — not from
 * anything in resident-service. Without this consumer that column never
 * gets set, so an approved resident's token (including any token issued
 * after approval, until this ran) carries no apartmentId claim, the
 * gateway has nothing to forward as X-Apartment-Id, and every
 * apartment-scoped dashboard call comes back empty even though the
 * approval succeeded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestEventConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "join-request-status-topic",
            groupId = "auth-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        Object message = record.value();
        JoinRequestStatusEvent event = null;

        if (message instanceof JoinRequestStatusEvent) {
            event = (JoinRequestStatusEvent) message;
        } else if (message instanceof String) {
            try {
                event = objectMapper.readValue((String) message, JoinRequestStatusEvent.class);
            } catch (Exception e) {
                log.warn("Skipping message on join-request-status-topic — cannot parse string to JoinRequestStatusEvent: {}", e.getMessage());
                return;
            }
        } else if (message instanceof java.util.Map) {
            try {
                event = objectMapper.convertValue(message, JoinRequestStatusEvent.class);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping message on join-request-status-topic — cannot convert payload Map to JoinRequestStatusEvent: {}", e.getMessage());
                return;
            }
        } else {
            log.warn("Skipping message on join-request-status-topic — unsupported payload type: {} at offset {}",
                    message == null ? "null" : message.getClass(), record.offset());
            return;
        }

        if (event == null) {
            log.warn("Skipping message on join-request-status-topic — deserialized event was null");
            return;
        }

        // Only APPROVED links a user to an apartment. REJECTED needs no
        // action here — the user stays apartment-less and can re-apply.
        if (!"APPROVED".equals(event.getStatus())) {
            return;
        }

        if (event.getUserId() == null || event.getApartmentId() == null) {
            log.warn("Skipping APPROVED join-request event — missing userId or apartmentId: {}", event);
            return;
        }

        UUID userId = UUID.fromString(event.getUserId());
        UUID apartmentId = UUID.fromString(event.getApartmentId());

        userRepository.findById(userId).ifPresentOrElse(user -> {
            user.setApartmentId(apartmentId);
            userRepository.save(user);
            log.info("Linked user {} to apartment {} after join-request approval", userId, apartmentId);
        }, () -> log.warn("Join request approved for unknown userId={} — no matching Users row", userId));
    }
}
