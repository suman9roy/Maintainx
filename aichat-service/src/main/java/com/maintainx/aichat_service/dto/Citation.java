package com.maintainx.aiplatform.dto;

import com.maintainx.aiplatform.rag.RetrievalService;

public record Citation(String documentId, String title, Integer chunkIndex, Double score) {
    public static Citation from(RetrievalService.RetrievedChunk chunk) {
        return new Citation(chunk.documentId(), chunk.title(), chunk.chunkIndex(), chunk.score());
    }
}
