# Week 3 — Hybrid RAG

**Design:** [Document Q&A / RAG design](../architecture/document-qa-rag.md)  
**Dates:** Not recorded

## Goal

Build document ingestion and question answering with local embeddings, PostgreSQL full-text search, pgvector vector search, reciprocal-rank fusion (RRF), and validated citations.

## Session log

| Session | Planned milestone | Actual result | Verification | Status |
|---|---|---|---|---|
| 1 | Finalize RAG design | Created `docs/architecture/document-qa-rag.md` as the Week 3 design baseline. | Design document is in the repository. This records the design, not completed implementation. | Verified |
| 2 | Local embedding smoke test and chunker | Added Ollama embedding configuration and `EmbeddingService`, including document/query prefixes and a 768-dimension check. Added offset-aware `DocumentChunk` records that preserve metadata. | `DocumentChunkerTest`: 8 passed. Opt-in `EmbeddingModelSmokeTest`: 2 passed; dimension 768. | Verified |
| 3 | pgvector schema, storage, and chunk indexing | Configured LangChain4j’s pgvector embedding store to use the application `DataSource`. Added chunk indexing with tenant, document, index, offset, and chunk metadata. | `PgVectorDocumentSearchIT` successfully stored and searched chunks in PostgreSQL. Flyway migration location corrected to `classpath:db/migration`. | Verified |
| 4 | Scoped vector retrieval | Added vector search filtered by both tenant ID and document ID. | `PgVectorDocumentSearchIT` found a matching chunk for the correct scope and returned no results for a different tenant or document. | Verified |
| 5 | Scoped keyword retrieval | Added PostgreSQL full-text search using `websearch_to_tsquery`, `ts_rank_cd`, and a GIN index over chunk text. Added tenant and document filtering to the keyword query. | `PgVectorKeywordSearchIT` passed. Full suite: 55 tests, 0 failures, 0 errors, 2 skipped. | Verified |
| 6 | RRF and cited answers | Not started. | Combine independently ranked vector and keyword results using Reciprocal Rank Fusion. Add citation construction and validation. | Not started |
| 7 | Evaluation and hardening | Not started. | Record retrieval metrics, citation validity, answer checks, and latency. | Not started |

A session may take more than one day. Mark a milestone complete only when its acceptance checks pass.

## Decisions made during implementation

| Decision | Choice | Evidence / reason |
|---|---|---|
| Chunk representation | Custom `DocumentChunk` with offsets and metadata | Chunker tests verify chunk bounds, offsets, overlap, and metadata preservation. |
| Embedding dimension at store startup | Use the configured expected dimension (`768`) rather than calling `embeddingModel.dimension()` while creating the store | The embedding smoke test confirmed dimension 768. Avoiding a model call during bean creation prevents an unnecessary Ollama request at application startup. |
| Flyway migration location | `classpath:db/migration` for the PostgreSQL migrations | The previous location pointed to a nonexistent `postgresql` subdirectory; the corrected location allowed PostgreSQL integration tests to run migrations. |
| Existing database migration state | Baseline the existing application schema at version 2, then apply V3 and later migrations | The local database already contained `users` and `task`; Flyway history recorded the baseline and successfully applied V3. |
| pgvector metadata storage | `COMBINED_JSONB` with an explicit `metadata JSONB NULL` column definition | Required by the pgvector store configuration; the storage and search integration tests passed with it. |
| Embedding-store primary key | Use `embedding_id` | The actual `document_embeddings` table uses `embedding_id`, not `id`. |
| Keyword search | PostgreSQL full-text search with `websearch_to_tsquery` and `ts_rank_cd` | Keyword retrieval handles literal terms, identifiers, and phrases that semantic search may rank lower. |
| Keyword index | GIN expression index over `to_tsvector('english', coalesce(text, ''))` | The index matches the search expression and supports PostgreSQL full-text lookup. |
| Retrieval scope | Apply tenant ID and document ID filters to both vector and keyword paths | Integration tests verify that another tenant or document receives no results. |
| AI service wiring | Route typed AI service calls through `AiGateway` | Application-context and full-suite tests pass without a standalone `ChatModel` bean. Provider calls still require separate verification. |

## Evaluation results

| Mode | Precision@3 | Recall@3 | MRR | Avg. latency |
|---|---:|---:|---:|---:|
| Vector | — | — | — | — |
| Keyword | — | — | — | — |
| Hybrid RRF | — | — | — | — |

Metrics are not yet measured; blank values do not represent zero.

## Current carry-over

- Vector storage and tenant/document-scoped vector retrieval are verified.
- PostgreSQL keyword retrieval and tenant/document filtering are verified.
- Vector and keyword retrieval currently return independent ranked lists.
- The latest full suite completed with **55 tests, 0 failures, 0 errors, and 2 skipped**.
- The current integration tests index chunks directly. A complete user-facing document upload and ingestion lifecycle has not yet been verified.
- Next: combine vector and keyword results with Reciprocal Rank Fusion, then build validated citations.

## End-of-week summary

- Working:
  - Ollama embedding generation with a verified 768-dimensional output.
  - Offset-aware document chunking with metadata preservation.
  - PostgreSQL and pgvector chunk storage.
  - Tenant/document-scoped vector search.
  - Tenant/document-scoped PostgreSQL keyword search.
- Tests passing:
  - `DocumentChunkerTest`: 8 passed.
  - `EmbeddingModelSmokeTest`: 2 passed; embedding dimension 768.
  - `PgVectorDocumentSearchIT`: passed.
  - `PgVectorKeywordSearchIT`: passed.
  - Full suite: 55 tests, 0 failures, 0 errors, 2 skipped.
- Known limitations:
  - Vector and keyword results are not yet fused into one hybrid ranking.
  - Citations and citation validation are not implemented.
  - Retrieval metrics have not yet been measured.
  - Complete document upload and ingestion lifecycle is not yet verified.
- Carry-over:
  - Implement RRF fusion.
  - Add cited answer output.
  - Add citation validity tests.
  - Measure retrieval quality and latency.