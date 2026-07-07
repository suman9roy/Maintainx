package com.maintainx.payment_service.kaffka;

import com.maintainx.payment_service.event.PaymentVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentEventProducer paymentEventProducer;

    /**
     * Runs ONLY after the transaction in PaymentService.verifyPayment()
     * commits successfully. If that transaction rolls back for any
     * reason, this method never runs — so Kafka (and the "payment
     * successful" email it triggers) can never fire for a payment
     * that wasn't actually persisted.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentVerified(PaymentVerifiedEvent event) {
        log.info("Transaction committed — publishing Kafka event for paymentId={}",
                event.getKafkaPayload().getPaymentId());
        paymentEventProducer.publishPaymentSuccess(event.getKafkaPayload());
    }
}