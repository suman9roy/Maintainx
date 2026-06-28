package com.maintainx.payment_service.kaffka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import com.maintainx.payment_service.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;



import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<
            String,
            PaymentSuccessEvent
            > kafkaTemplate;

    public void publishPaymentSuccess(

            PaymentSuccessEvent event) {

        kafkaTemplate.send(

                "payment-success-topic",

                event
        );



        log.info("Payment success event published — billId={}, flat={}, amount={}",
                event.getMaintenanceBillId(), event.getFlatNumber(), event.getAmount());
    }
}