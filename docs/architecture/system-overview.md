# Productivity OS — System Overview

**Purpose:** Describe the system as it exists, not just the system we hope to build.
**Last verified:** [YYYY-MM-DD]

## Product

Productivity OS is a Spring Boot application for authenticated users to manage
productivity tasks and use AI-assisted functionality.

## Verified baseline

This table is a starting point from code and configuration previously shared.
Re-check it against the current checkout before treating it as the current state.

| Area | Previously shared baseline | Current verified state |
|---|---|---|
| Backend | Java and Spring Boot | [confirm from pom.xml] |
| Authentication | JWT-based authentication | [tests/config checked?] |
| Task management | Task controller, service, repository, entity | [tests checked?] |
| Database | MySQL app configuration was shared | [MySQL or PostgreSQL now?] |
| Tests | H2 test configuration and service/integration tests were shared | [test command and result] |
| AI | Mock AI service existed in the shared code | [mock, local model, or gateway now?] |
| Tenant scope | Tenant ID was a Week 1 requirement | [which entities and queries have it?] |

## Request flow

```mermaid
flowchart TD
    C[Client] --> S[Spring Security and JWT]
    S --> W[Authenticated request]
    W --> API[Controller]
    API --> SERVICE[Service]
    SERVICE --> REPO[Repository]
    REPO --> DB[(Configured database)]