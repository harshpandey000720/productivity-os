# Productivity OS — Spring Boot Foundation

**Type:** Retrospective  
**Period:** Before the Week 1–3 roadmap  
**Evidence:** Original source tree, configuration, and tests shared during development

## Purpose

This document records the Spring Boot foundation built before the PostgreSQL and
RAG roadmap. It is a snapshot of existing work, not a claim that every feature
is production-ready.

## Application structure

The application was organized around familiar Spring layers:

| Layer | Responsibility |
|---|---|
| Controller | Expose REST endpoints and translate HTTP requests into application calls |
| DTO | Define request and response shapes independently from persistence entities |
| Service | Apply authentication, task, and AI-related business logic |
| Repository | Use Spring Data JPA to read and write persisted entities |
| Entity | Represent persisted users and tasks |
| Security | Configure request authorization and JWT filtering |
| Exception handling | Convert application errors into HTTP responses |

The original source tree included areas for controllers, DTOs, entities,
repositories, services, security/configuration, and exception handling.
Update the exact package names here if they have since changed.

## REST API

The shared code included API areas for:

- User registration and login.
- Task operations.
- An AI chat endpoint.

Record the actual route, HTTP method, request DTO, response DTO, and authorization
rule for each endpoint after checking the current controllers. Do not copy route
names from memory.

| Area | Controller | Request/response DTOs | Verification |
|---|---|---|---|
| Authentication | `AuthController` | `RegisterRequest`, `LoginRequest`, `AuthResponse` | [tests/result] |
| Tasks | `TaskController` | [actual DTOs, if used] | [tests/result] |
| AI chat | `AiController` | `AiChatRequest`, `AiChatResponse` | [tests/result] |

## Persistence: JPA and MySQL

The earlier configuration shared in development used MySQL for the application,
with Spring Data JPA repositories for users and tasks. H2 was also configured
for tests.

Document the **current** persistence setup here after checking the code:

- Application database: [MySQL / PostgreSQL / other]
- Schema changes: [Hibernate-managed / Flyway-managed / other]
- Entities: [actual entity classes]
- Repositories: [actual repository interfaces]
- Test database: [H2 / PostgreSQL Testcontainers / other]

The original MySQL configuration used Hibernate schema auto-update. If that has
been replaced by Flyway, record the migration that made the transition in
`week-01.md`.

## Maven

Maven and the Maven Wrapper are used to declare dependencies and build/test the
application.

Record the commands you have actually run successfully:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package