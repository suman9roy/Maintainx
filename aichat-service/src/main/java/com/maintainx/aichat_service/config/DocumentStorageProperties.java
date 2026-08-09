package com.maintainx.aichat_service.config;



import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "maintainx.ai.storage")
public record DocumentStorageProperties(

        String location,

        List<String> allowedExtensions,

        long maxFileSize

) {
}