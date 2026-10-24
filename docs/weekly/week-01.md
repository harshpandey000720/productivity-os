# Week 1 — PostgreSQL and Project Foundation

**Type:** Retrospective; verify against the code and Git history.  
**Dates:** [fill in only if known]

## Goal

Move the application foundation toward PostgreSQL and prepare it for RAG:

- Dockerized PostgreSQL with pgvector available.
- Flyway-managed database migrations.
- Existing application data model moved from MySQL where applicable.
- Tenant ID present in core data from the start.
- Baseline tests and clearer package organization.

## Starting point

From configuration previously shared in chat:

- The application was configured for MySQL.
- Tests used H2.
- The application already had JWT authentication and task-management code.

Confirm whether that description matches the repository state at the start of this work.

## Planned work and evidence

| Item | Status | Evidence / notes |
|---|---|---|
| PostgreSQL starts locally in Docker | Not verified | [compose file and health check] |
| pgvector extension is available | Not verified | [migration or `SELECT extversion FROM pg_extension ...`] |
| Flyway owns schema changes | Not verified | [migration files and application config] |
| Data model moved from MySQL | Not verified | [which entities/configuration changed?] |
| Tenant ID added to core tables | Not verified | [entities, migrations, scoped queries] |
| Baseline test suite passes | Not verified | [exact command and result] |
| Package structure cleaned up | Not verified | [what moved, if anything] |

## Actual result

Write only what is supported by the current code, tests, or commit history:

- Completed:
    - [verified item]
- Incomplete:
    - [remaining item]
- Changed from the plan:
    - [what changed and why]

## Tests and proof

- Command run: `[exact command]`
- Result: `[pass/fail and test count]`
- Migration check: `[what was verified]`
- Data-preservation check: `[what was verified, if a migration happened]`

## Carry-over

- [specific remaining task]