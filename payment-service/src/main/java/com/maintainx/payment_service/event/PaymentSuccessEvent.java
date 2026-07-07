package com.maintainx.payment_service.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    // Changed from Long to UUID — must match Payment.maintenanceBillId.
    // IMPORTANT: the mirror class in notification-service
    // (NotificationService/event/PaymentSuccessEvent.java) must be
    // updated to match too, or Kafka JSON deserialization will fail
    // (a UUID serializes as a string, which cannot bind to a Long field).
    private UUID maintenanceBillId;

    private String flatNumber;

    private String residentEmail;

    private BigDecimal amount;

    private String paymentId;
}