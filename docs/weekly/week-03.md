# Week 3 — Hybrid RAG

**Design:** [Document Q&A / RAG design](../architecture/document-qa-rag.md)  
**Dates:** [fill in only if known]

## Goal

Implement document ingestion and question answering with local embeddings,
PostgreSQL full-text search, pgvector vector search, RRF, and validated citations.

## Session log

| Session | Planned milestone | Actual result | Verification | Status |
|---|---|---|---|---|
| 1 | Finalize RAG design | Draft provided in chat; repository copy not confirmed | [review and commit] | Pending |
| 2 | Local embedding smoke test and chunker | Pending | [dimension check and unit tests] | Not started |
| 3 | Migration and document ingestion | Pending | [migration and ingestion tests] | Not started |
| 4 | Scoped vector retrieval | Pending | [query and ownership tests] | Not started |
| 5 | Scoped keyword retrieval | Pending | [full-text search tests] | Not started |
| 6 | RRF and cited answers | Pending | [ranking and citation validation] | Not started |
| 7 | Evaluation and hardening | Pending | [metrics and regression tests] | Not started |

A session may take more than one day. Move a milestone forward only when its
acceptance checks pass.

## Decisions changed during implementation

| Decision | Previous choice | New choice | Evidence / reason |
|---|---|---|---|
| [decision] | [old] | [new] | [test result, measurement, or constraint] |

## Evaluation results

| Mode | Precision@3 | Recall@3 | MRR | Avg. latency |
|---|---:|---:|---:|---:|
| Vector | — | — | — | — |
| Keyword | — | — | — | — |
| Hybrid RRF | — | — | — | — |

## End-of-week summary

- Working:
    - [verified behavior]
- Tests passing:
    - [command and result]
- Known limitations:
    - [honest list]
- Carry-over:
    - [next concrete task]