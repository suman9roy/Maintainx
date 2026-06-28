package com.maintainx.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Internal Spring application event — NOT sent over Kafka directly.
 *
 * Published inside the @Transactional verifyPayment() method.
 * PaymentEventListener picks this up via @TransactionalEventListener
 * and only THEN sends the actual Kafka message — guaranteeing Kafka
 * never fires unless the local DB transaction actually committed.
 */
@Getter
@AllArgsConstructor
public class PaymentVerifiedEvent {

    private final PaymentSuccessEvent kafkaPayload;
}