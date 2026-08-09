package com.maintainx.aichat_service.config;

import com.maintainx.aichat_service.kafka.event.DocumentUploadedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

// @EnableKafka activates @KafkaListener processing (used by
// DocumentUploadedConsumer). Without it, listener methods are silently
// never invoked — no error, no consumer group, nothing.
//
// The consumer-side beans below are defined explicitly (matching the
// existing manual producer setup, rather than relying on Boot
// autoconfiguration) for two reasons:
//  1. This class already opts out of producer autoconfiguration by
//     defining its own ProducerFactory/KafkaTemplate, so a
//     kafkaListenerContainerFactory bean isn't guaranteed to appear
//     automatically either — Spring reported exactly that at startup.
//  2. The producer disables type-info headers (ADD_TYPE_INFO_HEADERS =
//     false) to keep messages simple, which means the consumer's
//     JsonDeserializer has no header to infer the target type from and
//     must be told the type explicitly (VALUE_DEFAULT_TYPE below) —
//     autoconfiguration wouldn't know to do this on its own.
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:ai-chat-group}")
    private String groupId;

    @Bean
    public ProducerFactory<String, DocumentUploadedEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Disable adding type info headers to simplify consumers
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate(ProducerFactory<String, DocumentUploadedEvent> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, DocumentUploadedEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        // Producer sends without type-info headers, so this side must be
        // told the target type explicitly rather than reading it from a
        // header that doesn't exist.
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DocumentUploadedEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DocumentUploadedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, DocumentUploadedEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, DocumentUploadedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(2);
        return factory;
    }
}