package com.maintainx.aichat_service.dto;


//public record Citation(String documentId, String title, Integer chunkIndex, Double score) {
//    public static Citation from(RetrievalService.RetrievedChunk chunk) {
//        return new Citation(chunk.documentId(), chunk.title(), chunk.chunkIndex(), chunk.score());
//    }
//}
public record Citation(

        String documentId,

        String documentName,

        Integer page,

        String section,

        Double score

){}