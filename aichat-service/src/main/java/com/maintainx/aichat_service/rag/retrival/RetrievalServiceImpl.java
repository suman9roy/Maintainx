package com.maintainx.aichat_service.rag.retrival;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sprint 2 - semantic search over the apartment knowledge base.
 *
 * Every query is filtered by {@code apartment_id} so one apartment's
 * society rules/bylaws can never leak into another apartment's answers
 * (multi-tenant isolation, per ADR-001 and the roadmap's "Apartment
 * Isolation" deliverable).
 */
@Service
public class d {

    private final VectorStore vectorStore;
    private final int defaultTopK;
    private final double similarityThreshold;

    public d(VectorStore vectorStore,
             @Value("${maintainx.ai-platform.rag.top-k:5}") int defaultTopK,
             @Value("${maintainx.ai-platform.rag.similarity-threshold:0.5}") double similarityThreshold) {
        this.vectorStore = vectorStore;
        this.defaultTopK = defaultTopK;
        this.similarityThreshold = similarityThreshold;
    }

    public List<RetrievedChunk> search(String query, String apartmentId) {
        return search(query, apartmentId, defaultTopK);
    }

    public List<RetrievedChunk> search(String query, String apartmentId, int topK) {
        FilterExpressionBuilder filter = new FilterExpressionBuilder();

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(filter.eq("apartment_id", apartmentId).build())
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        return results.stream().map(RetrievedChunk::from).toList();
    }

    /** A single retrieved chunk plus everything needed to cite it back to the source document. */
    public record RetrievedChunk(String text, String documentId, String title,
                                  Integer chunkIndex, Double score) {
        static RetrievedChunk from(Document d) {
            return new RetrievedChunk(
                    d.getText(),
                    String.valueOf(d.getMetadata().get("document_id")),
                    String.valueOf(d.getMetadata().get("title")),
                    (Integer) d.getMetadata().get("chunk_index"),
                    d.getScore()
            );
        }
    }
}
