# Productivity OS

A secure, full-stack task and productivity management platform built with Spring Boot — currently evolving from a task manager into an AI-augmented system ("Nexus") using LangChain4j.

## Status

**Core application: working and tested.** Auth, task management, and the frontend are fully functional. The AI layer is under active development — a basic chat endpoint exists today; the full orchestration layer (RAG, tool-calling agent, MCP, streaming) is in progress (see [Roadmap](#roadmap--nexus-ai-layer-in-progress)).

## Features

- **JWT authentication** — stateless login/register flow with BCrypt-hashed passwords
- **Task management** — full CRUD (title, description, status, priority, deadline)
- **Per-user ownership enforcement** — users can only view/edit/delete their own tasks, enforced at the service layer
- **Search & filter** — client-side filtering by status, priority, and text search
- **Light/dark theme** — persisted per browser
- **Test coverage** — unit tests (JUnit 5 + Mockito) and integration tests (`MockMvc`, `@DataJpaTest`) covering auth, CRUD, and ownership edge cases (e.g. cross-user access attempts)

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4, Spring Security, Spring Data JPA / Hibernate |
| Auth | JWT (`jjwt`), BCrypt |
| Database | MySQL (migrating to PostgreSQL + pgvector) |
| AI | Spring AI + Ollama (early integration) → migrating to LangChain4j |
| Frontend | HTML, CSS, vanilla JavaScript |
| Testing | JUnit 5, Mockito, MockMvc, H2 (in-memory test DB) |

## Roadmap — Nexus AI Layer (in progress)

Evolving the existing AI endpoint into a full orchestration layer, not a separate project:

- [ ] Multi-model gateway (OpenAI + Ollama) with automatic fallback
- [ ] Typed `AiServices` interfaces (LangChain4j) replacing raw prompt strings
- [ ] Hybrid RAG over task history — vector + keyword search with reranking
- [ ] ReAct-style agent with tool-calling (task queries, calculator, web search)
- [ ] Persistent chat memory (PostgreSQL-backed, not in-memory)
- [ ] MCP server (expose tools) + MCP client (consume an external server)
- [ ] Token-streaming React dashboard (SSE/WebSockets)
- [ ] Observability — per-call latency, token, and cost metrics

## API Endpoints

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/register` | Create a new account | No |
| POST | `/login` | Authenticate, returns JWT | No |
| GET | `/Tasks` | List current user's tasks | Yes |
| GET | `/Tasks/{id}` | Get a single task | Yes |
| POST | `/Tasks` | Create a task | Yes |
| PUT | `/Tasks/{id}` | Update a task | Yes |
| DELETE | `/Tasks/{id}` | Delete a task | Yes |
| POST | `/api/ai/chat` | AI chat (in progress) | Yes |

## Getting Started

### Prerequisites
- JDK 25
- MySQL 8+ running locally
- Maven (or use the included `./mvnw`)

### Configuration
Set the following before running (recommended as environment variables rather than hardcoded):

```
DB_URL=jdbc:mysql://localhost:3306/productivity_os
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_base64_secret
```

### Run

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Open `src/main/resources/static/index.html` in a browser (or serve it with any static file server) to use the frontend — it points to `API_BASE_URL = http://localhost:8080` by default.

### Tests

```bash
./mvnw test
```

## Security Notes

- Passwords hashed with BCrypt (strength 12)
- Stateless authentication via JWT, validated on every request through a custom filter
- Resource ownership checked at the service layer on every read/update/delete — prevents cross-user access to tasks

## License

No license specified yet.
