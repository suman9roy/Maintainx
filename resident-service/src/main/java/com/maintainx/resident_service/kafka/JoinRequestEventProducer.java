package com.maintainx.resident_service.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestEventProducer {

    private final KafkaTemplate<String, JoinRequestStatusEvent> kafkaTemplate;

    public void publishStatusUpdate(JoinRequestStatusEvent event) {
        kafkaTemplate.send("join-request-status-topic", event);
        log.info("Join request status event published: userId={}, status={}",
                event.getUserId(), event.getStatus());
    }
}