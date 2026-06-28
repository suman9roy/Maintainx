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

    private UUID maintenanceBillId;   // changed from Long to UUID

    private String flatNumber;

    private String residentEmail;

    private BigDecimal amount;        // also aligned to BigDecimal — was Double,
    // worked previously only because JSON numeric
    // coercion is loose, but BigDecimal is correct
    // and matches the producer side exactly

    private String paymentId;
}