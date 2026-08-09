package com.maintainx.aichat_service.model;



public enum DocumentStatus {

    /**
     * File uploaded successfully.
     */
    UPLOADED,

    /**
     * Currently being processed by the ingestion pipeline.
     */
    PROCESSING,

    /**
     * Embeddings successfully generated.
     */
    COMPLETED,

    /**
     * Processing failed.
     */
    FAILED
}