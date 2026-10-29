CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE document_embeddings(
    embedding_id UUID PRIMARY KEY,
    embedding vector(768)NOT NULL,
    text TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT
'{}'::jsonb
);