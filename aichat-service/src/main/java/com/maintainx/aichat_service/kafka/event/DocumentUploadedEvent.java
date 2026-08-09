package com.maintainx.aichat_service.kafka.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Published after a document upload is persisted (status=PENDING) so
 * ingestion (Tika extraction -> chunking -> embedding -> pgvector) runs
 * asynchronously off the request thread. See ADR-001 Decision 4/7 and
 * dedevlopment.md Sprint 2: "Asynchronous Processing (Kafka)".
 */
public record DocumentUploadedEvent(UUID documentId, Instant uploadedAt) implements Serializable {}
