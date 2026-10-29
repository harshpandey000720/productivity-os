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
| 5 | Scoped keyword retrieval | Not started. | Add PostgreSQL full-text search and verify tenant/document filtering. | Not started |
| 6 | RRF and cited answers | Not started. | Verify ranking behavior and that citations refer to retrieved, stored chunks. | Not started |
| 7 | Evaluation and hardening | Not started. | Record retrieval metrics, citation validity, answer checks, and latency. | Not started |

A session may take more than one day. Mark a milestone complete only when its acceptance checks pass.

## Decisions made during implementation

| Decision | Choice | Evidence / reason |
|---|---|---|
| Chunk representation | Custom `DocumentChunk` with offsets and metadata | Chunker tests verify chunk bounds, offsets, overlap, and metadata preservation. |
| Embedding dimension at store startup | Use the configured expected dimension (`768`) rather than calling `embeddingModel.dimension()` while creating the store | The embedding smoke test confirmed dimension 768. Avoiding a model call during bean creation prevents an unnecessary Ollama request at application startup. |
| Flyway migration location | `classpath:db/migration` for the PostgreSQL migrations | The previous location pointed to a nonexistent `postgresql` subdirectory; the corrected location allowed the PostgreSQL integration test to run migrations. |
| pgvector metadata storage | `COMBINED_JSONB` with an explicit `metadata JSONB NULL` column definition | Required by the pgvector store configuration; the storage/search integration test passed with it. |
| AI service wiring | Route typed AI service calls through `AiGateway` | Application-context and full-suite tests pass without a standalone `ChatModel` bean. Provider calls still require separate verification. |

## Evaluation results

| Mode | Precision@3 | Recall@3 | MRR | Avg. latency |
|---|---:|---:|---:|---:|
| Vector | — | — | — | — |
| Keyword | — | — | — | — |
| Hybrid RRF | — | — | — | — |

Metrics are not yet measured; do not treat the blank values as zero.

## Current carry-over

- The vector storage and scoped search slice is verified by a PostgreSQL integration test.
- The latest full test suite completed with **55 tests, 0 failures, 0 errors, and 2 skipped**.
- The current integration test indexes chunks directly. A complete user-facing document upload and ingestion lifecycle has not yet been verified.
- Next: implement scoped PostgreSQL full-text search, then combine keyword and vector results with RRF.

## End-of-week summary

- Working:
  - [Add verified behavior when the Week 3 scope is complete.]
- Tests passing:
  - Latest full suite: `55 tests, 0 failures, 0 errors, 2 skipped`.
  - Record the exact commands used for the targeted integration test and full suite.
- Known limitations:
  - [Update at the end of the week.]
- Carry-over:
  - [Record remaining work at the end of the week.]