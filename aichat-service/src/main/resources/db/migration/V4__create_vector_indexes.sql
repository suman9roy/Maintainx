CREATE INDEX idx_document_chunks_apartment
ON ai_document_chunks(apartment_id);
CREATE INDEX idx_document_chunks_document
ON ai_document_chunks(document_id);
CREATE INDEX idx_document_chunks_embedding
ON ai_document_chunks
USING hnsw (embedding vector_cosine_ops);