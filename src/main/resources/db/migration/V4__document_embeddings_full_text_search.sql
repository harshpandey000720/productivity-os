CREATE INDEX IF NOT EXISTS idx_document_embeddings_text_fts
    ON document_embeddings
    USING GIN (to_tsvector('english', coalesce(text, '')));

CREATE INDEX IF NOT EXISTS idx_document_embeddings_tenant_document
    ON document_embeddings ((metadata ->> 'tenantId'), (metadata ->> 'documentId'));