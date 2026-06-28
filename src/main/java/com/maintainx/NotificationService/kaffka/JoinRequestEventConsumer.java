package com.maintainx.NotificationService.kaffka;


import com.maintainx.NotificationService.event.JoinRequestStatusEvent;
import com.maintainx.NotificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestEventConsumer {

    private final EmailService emailService;

    /**
     * Listens to join-request-status-topic published by resident-service
     * when an admin approves or rejects a join request.
     *
     * On APPROVED → sends approval email to resident
     * On REJECTED → sends rejection email with reason
     */
    @KafkaListener(
            topics   = "join-request-status-topic",
            groupId  = "notification-group"
    )
    public void consume(JoinRequestStatusEvent event) {

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