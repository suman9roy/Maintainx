package com.maintainx.aichat_service.config;



import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maintainx.kafka.topics")
public record KafkaTopicProperties(

        String documentUploaded,

        String documentProcessed,

        String documentFailed

) {
}