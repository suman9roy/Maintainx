package com.maintainx.notification_service.kaffka;

import com.maintainx.notification_service.event.OtpNotificationEvent;
import com.maintainx.notification_service.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    /**
     * Listens to otp-notification-topic published by auth-service on
     * registration, resend, and forgot-password requests.
     *
     * On EMAIL_VERIFICATION → sends the verify-your-email OTP mail
     * On PASSWORD_RESET      → sends the reset-your-password OTP mail
     */
    @KafkaListener(
            topics   = "otp-notification-topic",
            groupId  = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        Object message = record.value();
        OtpNotificationEvent event = null;

        if (message instanceof OtpNotificationEvent) {
            event = (OtpNotificationEvent) message;
        } else if (message instanceof String) {
            try {
                event = objectMapper.readValue((String) message, OtpNotificationEvent.class);
            } catch (Exception e) {
                log.warn("Skipping message on otp-notification-topic — cannot parse string to OtpNotificationEvent: {}", e.getMessage());
                return;
            }
        } else if (message instanceof java.util.Map) {
            try {
                event = objectMapper.convertValue(message, OtpNotificationEvent.class);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping message on otp-notification-topic — cannot convert payload Map to OtpNotificationEvent: {}", e.getMessage());
                return;
            }
        } else {
            log.warn("Skipping message on otp-notification-topic — unsupported payload type: {} at offset {}", message == null ? "null" : message.getClass(), record.offset());
            return;
        }

        if (event == null) {
            log.warn("Skipping message on otp-notification-topic — deserialized event was null");
            return;
        }

        log.info("OTP notification event received: email={}, purpose={}", event.getEmail(), event.getPurpose());

        if ("EMAIL_VERIFICATION".equals(event.getPurpose())) {
            emailService.sendEmailVerificationOtpMail(
                    event.getEmail(), event.getFullName(), event.getOtp(), event.getExpiryMinutes()
            );
        } else if ("PASSWORD_RESET".equals(event.getPurpose())) {
            emailService.sendPasswordResetOtpMail(
                    event.getEmail(), event.getFullName(), event.getOtp(), event.getExpiryMinutes()
            );
        } else {
            log.warn("Skipping OTP event with unknown purpose: {}", event.getPurpose());
        }
    }
}
