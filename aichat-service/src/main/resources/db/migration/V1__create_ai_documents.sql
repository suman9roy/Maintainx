CREATE TABLE ai_documents
(
    id UUID PRIMARY KEY,

    apartment_id VARCHAR(100) NOT NULL,

    uploaded_by BIGINT NOT NULL,

    original_file_name VARCHAR(255) NOT NULL,

    stored_file_name VARCHAR(255) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    storage_location VARCHAR(1000) NOT NULL,

    status VARCHAR(30) NOT NULL,

    uploaded_at TIMESTAMP NOT NULL,

    processed_at TIMESTAMP
);