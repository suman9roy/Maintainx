package com.maintainx.aichat_service.kafka.consumer;




import com.maintainx.aichat_service.config.KafkaTopicProperties;
import com.maintainx.aichat_service.kafka.event.DocumentUploadedEvent;
import com.maintainx.aichat_service.rag.processing.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadedConsumer {

    private final DocumentProcessingService processingService;

    @KafkaListener(
            topics = "${maintainx.kafka.topics.document-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(DocumentUploadedEvent event) {

        log.info(
                "Received document upload event {}",
                event.documentId()
        );

        processingService.process(
                event.documentId()
        );

    }

}