package com.maintainx.aichat_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Lightweight, publicly reachable health endpoint distinct from
 * /actuator/health, useful for the API Gateway's own routing checks.
 */
@RestController
public class HealthController {

    @GetMapping("/api/ai/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "maintainx-ai-platform",
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }
}
