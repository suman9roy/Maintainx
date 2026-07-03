package com.maintainx.NotificationService.kaffka;


import com.maintainx.NotificationService.event.JoinRequestStatusEvent;
import com.maintainx.NotificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    /**
     * Listens to join-request-status-topic published by resident-service
     * when an admin approves or rejects a join request.
     *
     * On APPROVED → sends approval email to resident
     * On REJECTED → sends rejection email with reason
     */
    @KafkaListener(
            topics   = "join-request-status-topic",
            groupId  = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        Object message = record.value();
        // Handle multiple payload types: already-deserialized object,
        // JSON string, or Map. Only process JoinRequestStatusEvent.
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
            log.warn("Skipping message on join-request-status-topic — unsupported payload type: {} at offset {}", message == null ? "null" : message.getClass(), record.offset());
            return;
        }

        if (event == null) {
            log.warn("Skipping message on join-request-status-topic — deserialized event was null");
            return;
        }

        log.info("Join request status event received: userId={}, status={}",
                event.getUserId(), event.getStatus());

        if ("APPROVED".equals(event.getStatus())) {
            emailService.sendJoinRequestApprovedMail(
                    event.getResidentEmail(),
                    event.getFullName(),
                    event.getFlatNumber()
            );
        } else if ("REJECTED".equals(event.getStatus())) {
            emailService.sendJoinRequestRejectedMail(
                    event.getResidentEmail(),
                    event.getFullName(),
                    event.getFlatNumber(),
                    event.getRejectionReason()
            );
        }
    }
}