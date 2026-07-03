package com.maintainx.NotificationService.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mirrors com.maintainx.payment_service.event.PaymentSuccessEvent.
 * Field names AND types must match exactly for Jackson to deserialize
 * the Kafka message correctly — a UUID is serialized as a string and
 * will fail to bind to a Long field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {
    private UUID maintenanceBillId;

    private String flatNumber;

    private String residentEmail;

    private BigDecimal amount;

    private String paymentId;
}