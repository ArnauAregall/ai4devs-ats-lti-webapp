# AGENTS.md

## Project overview

The LTI Applicant Tracking System (ATS) is a full-stack web application for managing open job positions and guiding candidates through a hiring pipeline. Its current domain scope covers **Positions** — the seed entity that establishes the full technical foundation (backend API, database migrations, React UI, Docker images, and developer tooling). All future iterations — adding candidates, interview stages, hiring decisions — are intended to be driven by AI coding agents using this file as their primary source of truth for conventions and constraints.

## Repository layout

```
ai4devs-ats-lti-webapp/
├── AGENTS.md              ← This file; AI agent guidance
├── README.md              ← Human-readable project documentation and quick-start guide
├── run.sh                 ← Unified run script: ./run.sh dev | docker
├── docker-compose.yml     ← PostgreSQL (always) + backend/frontend (docker profile)
├── pom.xml                ← Maven aggregator (no parent; declares lti-ats-backend and lti-ats-frontend)
├── lti-ats-backend/       ← Spring Boot 4.x REST API
└── lti-ats-frontend/      ← React 18 single-page application (TypeScript)
```

## Backend — key conventions

- **Language and runtime:** Java 21, Spring Boot 4.x, Maven.
- **DB access:** Spring Data JDBC **only**. Never use `jakarta.persistence`, Hibernate, or JPA annotations anywhere in the codebase.
- **Package structure** (base: `tech.aregall.lidr.lti`):
  - `config/` — Only `@Configuration` classes and `@ConfigurationProperties` beans. No business logic.
  - `domain/entity/` — Spring Data JDBC entities using `@Table` / `@Id` / `@Column` from `org.springframework.data.relational.core.mapping` and `org.springframework.data.annotation`.
  - `domain/dto/` — Immutable Java `record` types for all inbound request bodies and outbound response payloads. No mutable fields.
  - `infrastructure/persistence/` — Interfaces extending `CrudRepository` or `PagingAndSortingRepository`. All query logic belongs here; services must not contain SQL.
  - `service/` — Orchestration only. Services may depend on repositories and other services. Must not import from `web/`.
  - `web/` — `@RestController` classes. A single `@RestControllerAdvice` (`GlobalExceptionHandler`) handles all exception mapping.
- **Flyway migration naming:** `V{major}.{minor}.{patch}.{sequence}__{description}.sql` (e.g., `V1.0.0.0__create_position_table.sql`). Always create a new migration file for schema changes; **never modify an existing one**.
- **REST conventions:** base path `/api/v1/{resource}` (plural nouns), standard HTTP status codes (200, 201, 204, 400, 404). No authentication or security configuration.
- **Testing:** Prefer integration tests over unit tests. Use `@SpringBootTest` + `@AutoConfigureMockMvc` + RestAssured MockMvc. Use Testcontainers (`PostgreSQLContainer`) with a shared `static` container and `@DynamicPropertySource`. **Never mock the repository layer.** Do not annotate test methods with `@Transactional`; manage state via the repository directly.
- **Current entities:**
  - `Position` — columns: `id` (UUID, PK, generated), `title` (VARCHAR 255, NOT NULL), `department` (VARCHAR 255, NOT NULL), `open_date` (DATE, NOT NULL), `created_at` (TIMESTAMP, NOT NULL), `updated_at` (TIMESTAMP, NOT NULL).

## Frontend — key conventions

- **Language:** TypeScript. All source files use `.ts` (logic/services) or `.tsx` (components/pages) extensions.
- **Types:** Domain types are defined in `src/services/api.ts` and imported where needed. Use `interface` for API response shapes. Avoid `any`; use typed generics on Axios calls.
- **API calls:** All HTTP calls go through `src/services/api.ts`. Never call Axios directly from components or pages.
- **Routing:** Centralised in `src/router/index.tsx`. The default route (`/`) redirects to `/positions`. Add new routes here when adding new pages.
- **UI library:** Ant Design (antd) v5. Do not introduce additional UI libraries.
- **Tests:** Live alongside the file they test (e.g., `PositionsPage.test.tsx` next to `PositionsPage.tsx`). Use Vitest + React Testing Library. Mock API calls with `vi.mock`. Use `vi.mocked()` to access mock-specific methods in TypeScript.
- **Current pages:**
  - `PositionsPage` — lists all open positions in an Ant Design Table; accessible at `/positions`.

## Running locally

See `README.md` for full instructions. The quickest path:

```bash
# Native backend + frontend, Postgres in Docker:
chmod +x run.sh && ./run.sh dev

# Everything in Docker (builds images if needed):
./run.sh docker
```

## Docker images

See `README.md` for build commands. Key environment variables:

- **Backend image:** `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- **Frontend image:** `BACKEND_URL` (default: `http://backend:8080`) — injected into the Nginx config at container start via `envsubst`. In Docker Compose, the backend is reachable via the service name `backend`.

## What to do before opening a pull request

- Run `mvn verify` and ensure all backend and frontend tests pass (the frontend module runs `npm test` via the Maven build).
- If you add a new entity, add a Flyway migration and update the **Current entities** section in this file.
- If you add a new page, update the **Current pages** section in this file.
- Ensure no new dependencies have been introduced without a justification comment in the relevant `pom.xml` or `package.json`.
