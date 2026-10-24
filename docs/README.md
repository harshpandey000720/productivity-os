# Project Documentation

## Project foundation

- [Spring Boot foundation](weekly/foundation.md) — the original application
  structure, REST API, persistence, security, and tests.

## Architecture

- [System overview](architecture/system-overview.md) — current system boundaries,
  components, and verified request flows.
- [Document Q&A / RAG design](architecture/document-qa-rag.md) — RAG scope,
  data model, retrieval, security, APIs, and evaluation plan.

## Roadmap engineering logs

- [Week 1](weekly/week-01.md) — PostgreSQL, Flyway, tenant groundwork, and baseline tests.
- [Week 2](weekly/week-02.md) — AI gateway reliability, health checks, and resilience tests.
- [Week 3](weekly/week-03.md) — RAG implementation progress and evaluation.

## Status rules

- `Planned` means intended, not implemented.
- `Implemented` means code exists.
- `Verified` means the relevant test or manual check passed.
- Do not mark work complete based only on a class or document existing.
- Retrospective notes should be supported by code, tests, or commits. Label
  anything uncertain instead of presenting it as fact.