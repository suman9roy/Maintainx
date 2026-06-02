package com.maintainx.NotificationService.kaffka;



import com.maintainx.NotificationService.event.PaymentSuccessEvent;
import com.maintainx.NotificationService.service.EmailService;
import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final EmailService emailService;

    @KafkaListener(

            topics =
                    "payment-success-topic",

            groupId =
                    "notification-group"
    )
    public void consume(

            PaymentSuccessEvent event) {

        System.out.println(
                "Payment Event Received"
        );

        emailService.sendPaymentSuccessMail(

                event.getResidentEmail(),

                event.getAmount(),

                event.getFlatNumber()
        );
    }
}