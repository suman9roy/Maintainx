CREATE TABLE ai_document_chunks
(
    id UUID PRIMARY KEY,

    document_id UUID NOT NULL,

    apartment_id VARCHAR(100) NOT NULL,

    chunk_index INTEGER NOT NULL CHECK (chunk_index >= 0),

    content TEXT NOT NULL,

    embedding VECTOR(768),

    page_number INTEGER,

    section VARCHAR(255),

    token_count INTEGER CHECK (token_count >= 0),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_document_chunks_document
        FOREIGN KEY (document_id)
        REFERENCES ai_documents(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_document_chunk
        UNIQUE (document_id, chunk_index)
);