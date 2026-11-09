# Week 3 — Hybrid RAG

**Design:** [Document Q&A / RAG design](../architecture/document-qa-rag.md)  
**Dates:** Not recorded

## Goal

Build document ingestion and question answering with local embeddings, PostgreSQL full-text search, pgvector vector search, reciprocal-rank fusion (RRF), reranking, and validated citations.

## Session log

| Session | Planned milestone | Actual result | Verification | Status |
|---|---|---|---|---|
| 1 | Finalize RAG design | Created `docs/architecture/document-qa-rag.md` as the Week 3 design baseline. | Design document is in the repository. This records the design, not completed implementation. | Verified |
| 2 | Local embedding smoke test and chunker | Added Ollama embedding configuration and `EmbeddingService`, including document/query prefixes and a 768-dimension check. Added offset-aware `DocumentChunk` records that preserve metadata. | `DocumentChunkerTest`: 8 passed. Opt-in `EmbeddingModelSmokeTest`: 2 passed; dimension 768. | Verified |
| 3 | pgvector schema, storage, and chunk indexing | Configured LangChain4j’s pgvector embedding store to use the application `DataSource`. Added chunk indexing with tenant, document, index, offset, and chunk metadata. | `PgVectorDocumentSearchIT` successfully stored and searched chunks in PostgreSQL. Flyway migration location corrected to `classpath:db/migration`. | Verified |
| 4 | Scoped vector retrieval | Added vector search filtered by both tenant ID and document ID. | `PgVectorDocumentSearchIT` found a matching chunk for the correct scope and returned no results for a different tenant or document. | Verified |
| 5 | Scoped keyword retrieval | Added PostgreSQL full-text search using `websearch_to_tsquery`, `ts_rank_cd`, and a GIN index over chunk text. Added tenant and document filtering to the keyword query. | `PgVectorKeywordSearchIT` passed. Full suite at this stage: 55 tests, 0 failures, 0 errors, 2 skipped. | Verified |
| 6 | RRF fusion | Implemented RRF fusion; the hybrid search service combines vector and keyword results. | `RrfFusionServiceTest` passed. Full suite at this stage: 56 tests, 0 failures, 0 errors, 2 skipped. | Verified |
| 7 | Reranking and grounded answers | Added a configurable local ONNX reranker and a typed grounded-answer AI service routed through `AiGateway`. The answer service checks tenant/document scope and returns citations only for IDs among the selected evidence. | `LocalOnnxCandidateRerankerTest` and `GroundedAnswerServiceTest` passed. Latest full suite: 58 tests, 0 failures, 0 errors, 2 skipped. These tests verify service logic; a live ONNX model run and live Qwen call are not recorded yet. | Verified — service layer |
| 8 | Evaluation and hardening | Not started. | Measure retrieval quality and latency; verify the authenticated HTTP route and full local-model flow. | Not started |

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
| Hybrid ranking | Reciprocal Rank Fusion with `k = 60`, merging by embedding ID | Combines vector and keyword rankings without adding their differently scaled raw scores. The fusion test checks deduplication, ranks, scores, and metadata. |
| Reranking | Use LangChain4j’s `ScoringModel` interface with a local ONNX cross-encoder; retain an explicit RRF-only path when reranking is disabled | Unit tests verify score-based ordering. A live ONNX model smoke test is still needed before claiming runtime verification. |
| Grounded answers and citations | Use a typed answer service through `AiGateway`; build returned citations from retrieved candidate metadata and discard IDs outside the selected evidence | `GroundedAnswerServiceTest` verifies that an invented citation ID is not returned. Live provider behavior remains to be checked. |

## Evaluation results

| Mode | Precision@3 | Recall@3 | MRR | Avg. latency |
|---|---:|---:|---:|---:|
| Vector | — | — | — | — |
| Keyword | — | — | — | — |
| Hybrid RRF | — | — | — | — |
| Hybrid + reranker | — | — | — | — |

Metrics are not yet measured; blank values do not represent zero.

## Current carry-over

- Vector storage and tenant/document-scoped vector retrieval are verified.
- PostgreSQL keyword retrieval and tenant/document filtering are verified.
- RRF fusion is implemented and its fusion test passed.
- Grounded answer and citation-validation service logic is covered by unit tests.
- Latest full suite: **58 tests, 0 failures, 0 errors, and 2 skipped**.
- A live ONNX reranker smoke test and live Qwen answer call have not yet been recorded.
- The authenticated HTTP route must obtain `tenantId` from the logged-in principal; it must not accept tenant scope from the request body.
- Integration tests index chunks directly. A complete user-facing document upload and ingestion lifecycle has not yet been verified.
- Next: verify the live local-model flow and authenticated route, then measure retrieval quality and latency.

## Current summary (after Session 7)

- Working:
  - Ollama embedding generation with a verified 768-dimensional output.
  - Offset-aware document chunking with metadata preservation.
  - PostgreSQL and pgvector chunk storage.
  - Tenant/document-scoped vector search.
  - Tenant/document-scoped PostgreSQL keyword search.
  - RRF fusion of vector and keyword candidates.
  - Service-layer reranking and grounded-answer flow with citation-ID validation.
- Tests passing:
  - `DocumentChunkerTest`: 8 passed.
  - `EmbeddingModelSmokeTest`: 2 passed; embedding dimension 768.
  - `PgVectorDocumentSearchIT`: passed.
  - `PgVectorKeywordSearchIT`: passed.
  - `RrfFusionServiceTest`: passed.
  - `LocalOnnxCandidateRerankerTest`: passed.
  - `GroundedAnswerServiceTest`: passed.
  - Latest full suite: 58 tests, 0 failures, 0 errors, 2 skipped.
- Known limitations:
  - Live ONNX reranker scoring and live Qwen grounded generation have not yet been verified together.
  - The authenticated HTTP route and tenant resolution have not yet been verified.
  - Retrieval metrics have not yet been measured.
  - Complete document upload and ingestion lifecycle is not yet verified.
- Carry-over:
  - Run a local ONNX reranker smoke test.
  - Verify the grounded-answer flow through the authenticated route.
  - Add a small retrieval evaluation set and measure ranking and latency.