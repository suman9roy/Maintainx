ALTER TABLE ai_documents
    ALTER COLUMN uploaded_by TYPE VARCHAR(100) USING uploaded_by::VARCHAR(100);