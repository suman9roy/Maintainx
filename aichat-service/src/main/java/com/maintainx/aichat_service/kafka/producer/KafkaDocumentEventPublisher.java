package com.maintainx.aichat_service.kafka.producer;



import com.maintainx.aichat_service.config.KafkaTopicProperties;
import com.maintainx.aichat_service.kafka.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaDocumentEventPublisher
        implements DocumentEventPublisher {

    private final KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate;

    private final KafkaTopicProperties kafkaTopics;

    @Override
    public void publishDocumentUploaded(UUID documentId) {

        kafkaTemplate.send(
                kafkaTopics.documentUploaded(),
                documentId.toString(),
                new DocumentUploadedEvent(
                        documentId,
                        Instant.now()
                )
        );
        log.info("Document uploaded event published: {}", documentId);
    }

}