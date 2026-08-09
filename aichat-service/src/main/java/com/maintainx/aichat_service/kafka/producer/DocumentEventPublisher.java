package com.maintainx.aichat_service.kafka.producer;



import java.util.UUID;

public interface DocumentEventPublisher {

    void publishDocumentUploaded(UUID documentId);

}