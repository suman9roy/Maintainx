package com.maintainx.auth_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpEventProducer {

    private final KafkaTemplate<String, OtpNotificationEvent> kafkaTemplate;

    public void publishOtp(OtpNotificationEvent event) {
        kafkaTemplate.send("otp-notification-topic", event);
        log.info("OTP notification event published: email={}, purpose={}",
                event.getEmail(), event.getPurpose());
    }
}