package com.maintainx.aichat_service;

import com.maintainx.aichat_service.config.DocumentStorageProperties;
import com.maintainx.aichat_service.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableConfigurationProperties({

        DocumentStorageProperties.class,
        KafkaTopicProperties.class
})
public class AichatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AichatServiceApplication.class, args);
	}

}
