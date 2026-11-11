# Week 3 — Hybrid RAG

**Design:** [Document Q&A / RAG design](../architecture/document-qa-rag.md)  

## Goal

Build document chunking and embedding, pgvector vector retrieval, PostgreSQL
full-text retrieval, Reciprocal Rank Fusion, reranking, grounded answers with
validated citations, and a small retrieval evaluation.

## Session log

| Session | Planned milestone | Actual result | Verification | Status |
|---|---|---|---|---|
| 1 | Finalize RAG design | Added the Document Q&A / RAG design document as the implementation baseline. | `docs/architecture/document-qa-rag.md` exists in the repository. | Verified |
| 2 | Local embedding smoke test and chunker | Configured the local Ollama embedding model, added an embedding service with dimension validation, and added offset-aware document chunks with metadata. | Embedding smoke test returned dimension 768. Chunker tests and the final suite passed. | Verified |
| 3 | pgvector and vector retrieval | Added chunk indexing and tenant/document-scoped vector retrieval using PostgreSQL and pgvector. | `PgVectorDocumentSearchIT` passed in the targeted integration test run. | Verified |
| 4 | Keyword search | Added PostgreSQL full-text search over indexed chunk text with tenant and document filters. | `PgVectorKeywordSearchIT` passed in the targeted integration test run. | Verified |
| 5 | Reciprocal Rank Fusion | Combined vector and keyword rankings by stable chunk ID, retaining source ranks and scores in the fused candidates. | RRF tests passed in the full test suite. | Verified |
| 6 | Reranking and grounded answers | Added a reranking path over fused candidates and a grounded answer service that limits citations to retrieved evidence and rejects out-of-scope evidence. | `GroundedAnswerServiceTest` passed in the targeted test run. | Verified |
| 7 | Evaluation and hardening | Evaluated keyword, vector, hybrid RRF, and reranked retrieval using the same three labeled questions and a four-chunk test corpus. | `RagRetrievalEvaluationTest`: 1 test, 0 failures, 0 errors, 0 skipped. Full suite: 63 tests, 0 failures, 0 errors, 0 skipped. | Verified |

## Decisions made during implementation

| Decision | Choice | Reason |
|---|---|---|
| Chunk representation | Custom `DocumentChunk` with offsets and metadata | Keeps chunk IDs, source offsets, and retrieval metadata available for evaluation and citations. |
| Tenant and document scope | Add scope from trusted service arguments during indexing and apply it to retrieval | Prevents caller-supplied chunk metadata from choosing its own retrieval scope. |
| Hybrid score combination | Reciprocal Rank Fusion | Vector similarity and PostgreSQL text rank use different scales; RRF combines their ranks without adding incomparable raw scores. |
| Reranking | Rerank a shortlist from hybrid retrieval | Reranking a smaller candidate set keeps the expensive ranking step bounded. |
| Evaluation keys | `documentId:chunkIndex` | Gives keyword, vector, hybrid, and reranked results the same stable comparison key. |

## Retrieval evaluation

The evaluation used three questions over a small four-chunk synthetic document. The test measured Precision@3, Recall@3, MRR@3, and latency for each retrieval mode.

The Maven summaries provided confirm that the evaluation test passed, but do not include the printed metric values. target\surefire-reports\TEST-com.Harsh.Productivity.Os.ai.evaluation.RagRetrievalEv
aluationTest.xml:164:keyword | avg P@3=0.000 avg Recall@3=0.000 avg MRR@3=0.000
avg latency=2.7ms
target\surefire-reports\TEST-com.Harsh.Productivity.Os.ai.evaluation.RagRetrievalEv
aluationTest.xml:165:vector | avg P@3=0.333 avg Recall@3=1.000 avg MRR@3=1.000 avg
latency=57.0ms
target\surefire-reports\TEST-com.Harsh.Productivity.Os.ai.evaluation.RagRetrievalEv
aluationTest.xml:166:hybrid-rrf | avg P@3=0.333 avg Recall@3=1.000 avg MRR@3=1.000
avg latency=58.7ms
target\surefire-reports\TEST-com.Harsh.Productivity.Os.ai.evaluation.RagRetrievalEv
aluationTest.xml:167:hybrid-reranked | avg P@3=0.333 avg Recall@3=1.000 avg
MRR@3=1.000 avg latency=45.0ms

| Mode | Precision@3 | Recall@3 | MRR@3 | Average latency |
|---|---:|---:|---:|---:|
| Keyword | See Surefire report | See Surefire report | See Surefire report | See Surefire report |
| Vector | See Surefire report | See Surefire report | See Surefire report | See Surefire report |
| Hybrid RRF | See Surefire report | See Surefire report | See Surefire report | See Surefire report |
| Hybrid reranked | See Surefire report | See Surefire report | See Surefire report | See Surefire report |

These results are a sanity check on the small evaluation set, not a general retrieval benchmark. Do not claim RRF or reranking improved retrieval unless the recorded scores show that.

## Tests and proof

| Command | Result |
|---|---|
| `.\mvnw.cmd -Dtest=RagRetrievalEvaluationTest test` | 1 test; 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS` |
| `.\mvnw.cmd -Dtest="RagMetricCalculatorTest,GroundedAnswerServiceTest,PgVectorDocumentSearchIT,PgVectorKeywordSearchIT" test` | 7 tests; 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS` |
| `.\mvnw.cmd test` | 63 tests; 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS` |

## End-of-week summary

- **Working:** Local embeddings, chunk indexing, scoped vector and keyword retrieval, RRF fusion, reranking, and grounded answer citation checks.
- **Tests passing:** The Day 7 evaluation, targeted retrieval and grounding checks, and the full 63-test suite.
- **Known limitations:** The evaluation set is small and synthetic. Its measurements do not establish performance on a larger or representative document collection.
- **Carry-over:** Week 1 and Week 2 follow-up work remains tracked separately.