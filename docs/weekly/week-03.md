# Week 3 — Hybrid RAG

**Design:** [Document Q&A / RAG design](../architecture/document-qa-rag.md)  
**Dates:** Not recorded yet

## Goal

Implement document ingestion and question answering with local embeddings,
PostgreSQL full-text search, pgvector vector search, RRF, and validated citations.

## Session log

| Session | Planned milestone | Actual result | Verification | Status |
|---|---|---|---|---|
| 1 | Finalize RAG design | Created `docs/architecture/document-qa-rag.md` as the Week 3 design baseline. | Design document exists in the repository; implementation is not claimed here. | Implemented |
| 2 | Local embedding smoke test and chunker | Added Ollama embedding configuration and `EmbeddingService` with document/query prefixes and a 768-dimension check. Added offset-aware document chunks that preserve metadata. | `DocumentChunkerTest`: 8 passed. Opt-in `EmbeddingModelSmokeTest`: 2 passed; dimension 768. Full suite: 53 tests, 0 failures/errors, 2 skipped; `BUILD SUCCESS`. | Verified |
| 3 | Migration and document ingestion | Not started | [migration and ingestion tests] | Not started |
| 4 | Scoped vector retrieval | Not started | [query and ownership tests] | Not started |
| 5 | Scoped keyword retrieval | Not started | [full-text search tests] | Not started |
| 6 | RRF and cited answers | Not started | [ranking and citation validation] | Not started |
| 7 | Evaluation and hardening | Not started | [metrics and regression tests] | Not started |

A session may take more than one day. Move a milestone forward only when its
acceptance checks pass.

## Decisions changed during implementation

| Decision | Previous choice | New choice | Evidence / reason |
|---|---|---|---|
| Chunk representation | LangChain4j `TextSegment` splitting | Custom `DocumentChunk` with offsets and metadata | Chunker tests verify chunk bounds, offsets, overlap, and metadata preservation. |
| AI service wiring | Inject a standalone `ChatModel` / use Spring-managed `@AiService` beans | Route typed AI service calls through `AiGateway` | Application-context and full-suite tests pass without a standalone `ChatModel` bean. Provider calls still need separate verification. |

## Evaluation results

| Mode | Precision@3 | Recall@3 | MRR | Avg. latency |
|---|---:|---:|---:|---:|
| Vector | — | — | — | — |
| Keyword | — | — | — | — |
| Hybrid RRF | — | — | — | — |

## Current carry-over after Session 2

- The full-suite test context logged zero configured AI providers. Chat-provider calls were not verified by that run.
- Surefire logged a non-fatal forked-JVM shutdown timeout after tests completed successfully.
- Next: begin Session 3, database migration and document ingestion.

## End-of-week summary

- Working:
  - [Add verified behavior when Week 3 is complete]
- Tests passing:
  - [Record final commands and results at the end of the week]
- Known limitations:
  - [Update at the end of the week]
- Carry-over:
  - [Record remaining work at the end of the week]