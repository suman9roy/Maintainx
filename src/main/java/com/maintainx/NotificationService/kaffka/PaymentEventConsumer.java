package com.maintainx.NotificationService.kaffka;



import com.maintainx.NotificationService.event.PaymentSuccessEvent;
import com.maintainx.NotificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payment-success-topic",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        Object message = record.value();
        // The message payload may be already deserialized (domain object),
        // a Map, or a String. Handle all cases safely and only process
        // PaymentSuccessEvent instances.
        PaymentSuccessEvent event = null;

        if (message instanceof PaymentSuccessEvent) {
            event = (PaymentSuccessEvent) message;
        } else if (message instanceof String) {
            try {
                event = objectMapper.readValue((String) message, PaymentSuccessEvent.class);
            } catch (Exception e) {
                log.warn("Skipping message on payment-success-topic — cannot parse string to PaymentSuccessEvent: {}", e.getMessage());
                return;
            }
        } else if (message instanceof java.util.Map) {
            try {
                event = objectMapper.convertValue(message, PaymentSuccessEvent.class);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping message on payment-success-topic — cannot convert payload Map to PaymentSuccessEvent: {}", e.getMessage());
                return;
            }
        } else {
            log.warn("Skipping message on payment-success-topic — unsupported payload type: {} at offset {}",
                    message == null ? "null" : message.getClass(), record.offset());
            return;
        }

        if (event == null) {
            log.warn("Skipping message on payment-success-topic — deserialized event was null");
            return;
        }

        log.info("Payment Event Received — billId={}, flat={}, amount={}",
                event.getMaintenanceBillId(), event.getFlatNumber(), event.getAmount());

        emailService.sendPaymentSuccessMail(
                event.getResidentEmail(),
                event.getAmount(),

                event.getFlatNumber()
        );
    }
}