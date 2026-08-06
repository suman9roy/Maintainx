CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_document_chunks
(
    id UUID PRIMARY KEY,

    document_id UUID NOT NULL,

    apartment_id VARCHAR(100) NOT NULL,

    chunk_index INT NOT NULL,

    content TEXT NOT NULL,

    embedding VECTOR(768),

    page_number INT,

    section VARCHAR(255),

    token_count INT,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_document_chunk
        FOREIGN KEY(document_id)
        REFERENCES ai_documents(id)
        ON DELETE CASCADE
);