# Meta-Prompt: Bootstrap Full-Stack Monorepo — LTI Applicant Tracking System

> **How to use:** Copy the prompt below and pass it as the initial instruction to Claude Code
> (or any agentic coding assistant) in an empty repository. The agent will scaffold the
> entire project from scratch without further guidance.

---

## Prompt

### Persona

You are a senior full-stack software engineer with deep expertise in Java backend development
(Spring Boot, Domain-Driven Design) and modern React frontend development. You write
production-quality code — clean, well-structured, and immediately runnable — and you apply
architectural constraints consistently across the entire codebase. You do not add patterns,
dependencies, or configuration that have not been explicitly requested.

---

### Context and Goal

Bootstrap a new **full-stack monorepo** for the **LTI Applicant Tracking System (ATS)** — a
web application that helps manage open job positions and candidates through a hiring pipeline.

This initial version is a **seed**: it establishes the full technical foundation (backend,
frontend, database, Docker images, developer tooling, documentation) with a minimal but
real domain slice — job positions — so the application can be demonstrated end-to-end
from day one and iterated on by AI coding agents from that point forward.

The application must be fully runnable locally with a single Docker Compose command for
infrastructure and standard `mvn` / `npm` commands for the applications. Every file
generated must compile and run without modification.

---

### Repository Structure

The root is a Maven aggregator (multi-module POM). It contains two child modules plus
documentation, developer tooling, and AI agent guidance files at the root:

```
lti-ats/
├── AGENTS.md                   ← AI agent guidance (see specification below)
├── README.md                   ← Project documentation (see specification below)
├── dev.sh                      ← Convenience script: starts all local processes
├── docker-compose.yml          ← PostgreSQL only
├── pom.xml                     ← Maven aggregator; no parent, declares both modules
├── lti-ats-backend/            ← Spring Boot application
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/tech/aregall/lidr/lti/
│       │   │   ├── Application.java
│       │   │   ├── config/         ← @Configuration and @ConfigurationProperties only
│       │   │   ├── domain/
│       │   │   │   ├── entity/     ← Spring Data JDBC entities
│       │   │   │   ├── dto/        ← Java records (request bodies and responses)
│       │   │   │   └── enums/      ← Domain enumerations
│       │   │   ├── infrastructure/
│       │   │   │   └── persistence/ ← Repository interfaces
│       │   │   ├── service/        ← Business logic
│       │   │   └── web/            ← Controllers, request/, resource/ sub-packages
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/   ← Flyway SQL scripts
│       └── test/
│           └── java/tech/aregall/lidr/lti/
│               └── web/            ← Integration tests
└── lti-ats-frontend/           ← React 18 + TypeScript single-page application
    ├── Dockerfile
    ├── nginx.conf              ← Nginx config for serving the production build
    ├── pom.xml
    ├── package.json
    ├── tsconfig.json
    ├── tsconfig.node.json
    ├── vite.config.ts
    ├── index.html
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── components/
        ├── pages/
        ├── services/           ← Axios API clients
        └── router/
```

---

### Backend Specification

#### Technology stack
| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.x (latest stable) |
| Database | PostgreSQL 16 |
| DB access | Spring Data JDBC — **not JPA / Hibernate** |
| Schema management | Flyway |
| Utility | Lombok |
| Testing | JUnit 5, Spring Boot Test, Testcontainers, RestAssured (spring-mock-mvc) |
| Build | Maven |

#### Maven coordinates
- Root aggregator `groupId`: `tech.aregall.lidr.lti`
- Root aggregator `artifactId`: `lti-ats`
- Backend module `artifactId`: `lti-ats-backend`
- Base Java package: `tech.aregall.lidr.lti`

#### Domain model — `Position`

The seed domain entity is **`Position`**, representing an open job position in the ATS.

**Table: `position`**

| Column | Type | Constraints |
|---|---|---|
| `id` | `UUID` | `PRIMARY KEY DEFAULT gen_random_uuid()` |
| `title` | `VARCHAR(255)` | `NOT NULL` |
| `department` | `VARCHAR(255)` | `NOT NULL` |
| `open_date` | `DATE` | `NOT NULL` |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT now()` |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT now()` |

The corresponding Java entity class is `Position`, living in `domain/entity/`. Use Spring
Data JDBC annotations (`@Table`, `@Id`, `@Column`) from
`org.springframework.data.relational.core.mapping`. Do **not** use `jakarta.persistence`.

The REST resource is exposed at `/api/v1/positions` with the standard CRUD operations.

#### Flyway migrations

**`V1.0.0.0__create_position_table.sql`** — creates the `position` table using the schema
above.

**`V1.0.0.1__seed_positions.sql`** — inserts exactly three seed positions:

```sql
INSERT INTO position (title, department, open_date) VALUES
  ('Backend Engineer',       'Engineering', '2025-01-15'),
  ('Product Designer',       'Design',      '2025-02-01'),
  ('Engineering Manager',    'Engineering', '2025-03-10');
```

Both scripts go under `src/main/resources/db/migration/` and follow the naming convention:

```
V{major}.{minor}.{patch}.{sequence}__{description}.sql
```

Never modify an existing migration file — always add a new one for schema changes.

#### Architectural constraints

**Packaging**
- `config/` — only `@Configuration` classes and `@ConfigurationProperties` beans. Never
  embed configuration logic inside services or controllers.
- `domain/entity/` — Spring Data JDBC entities as described above.
- `domain/dto/` — Java `record` types for all inbound request bodies and outbound
  response payloads.
- `infrastructure/persistence/` — interfaces extending `CrudRepository` or
  `PagingAndSortingRepository`. All query logic belongs here; services must not contain SQL.
- `service/` — orchestration only. Services may depend on repositories and other services.
  They must not import from `web/` or reference infrastructure details directly.
- `web/` — `@RestController` classes with `request/` (inbound) and `resource/` (outbound)
  sub-packages.

**REST API**
- Base path: `/api/v1/{resource}` (plural noun).
- Implement standard CRUD: `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`.
- Return HTTP 404 with a descriptive message for missing entities.
- Use a single `@RestControllerAdvice` class for global exception handling.
- No authentication or security configuration.

**Testing — integration tests are preferred over unit tests**
- Every `@RestController` must have a matching `*IntegrationTest` class in `src/test/.../web/`.
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` + RestAssured MockMvc for all web tests.
- Use Testcontainers (`PostgreSQLContainer`) with a shared `static` container per test class.
- Wire the container into Spring via `@DynamicPropertySource`.
- Do not mock the repository layer — tests must hit the real database.
- The `GET /api/v1/positions` test must assert that the three seed positions are returned
  (Flyway applies the seed migration inside the Testcontainers database automatically).
- Cover at minimum: happy path (200/201), not found (404), and invalid input (400).
- Do not annotate test methods with `@Transactional`; manage state via the repository directly.

---

### Infrastructure — Docker Compose

Place `docker-compose.yml` at the repository root. It must define only the PostgreSQL
service with a named volume for persistence. No other services.

Default credentials: database `lti_ats`, user `lti_ats`, password `lti_ats`, port `5432`.

The backend `application.yml` must read connection details from environment variables with
these defaults so the app works out of the box with Docker Compose:
- `SPRING_DATASOURCE_URL` → `jdbc:postgresql://localhost:5432/lti_ats`
- `SPRING_DATASOURCE_USERNAME` → `lti_ats`
- `SPRING_DATASOURCE_PASSWORD` → `lti_ats`

---

### Docker Images

Each module must have its own `Dockerfile` at the module root, suitable for building a
production image without relying on developer tooling being present on the host.

**Backend — `lti-ats-backend/Dockerfile`**

Use a multi-stage build:
1. **Build stage** — Maven image (Java 21); runs `mvn package -DskipTests` and produces the
   fat JAR via the Spring Boot Maven plugin.
2. **Runtime stage** — Eclipse Temurin 21 JRE (slim); copies only the fat JAR from the
   build stage. Exposes port `8080`. Accepts runtime env vars for datasource configuration
   (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

**Frontend — `lti-ats-frontend/Dockerfile`**

Use a multi-stage build:
1. **Build stage** — Node 20 LTS image; runs `npm ci` then `npm run build` to produce the
   static `dist/` folder.
2. **Runtime stage** — `nginx:alpine`; copies `dist/` into the Nginx web root and uses the
   custom `nginx.conf` from the module root.

**Frontend Nginx config — `lti-ats-frontend/nginx.conf`**

The Nginx config must:
- Serve the React app from `/usr/share/nginx/html`.
- Support client-side routing by falling back to `index.html` for any unknown path
  (`try_files $uri $uri/ /index.html`).
- Proxy all requests starting with `/api/` to the backend container. At runtime the backend
  host is configured via the environment variable `BACKEND_URL` (default:
  `http://lti-ats-backend:8080`). Use the `envsubst` approach in the Dockerfile entrypoint
  to inject the variable into the Nginx config at container start.

---

### Frontend Specification

#### Technology stack
| Concern | Technology |
|---|---|
| Language | TypeScript |
| Package manager | npm |
| Build tool | Vite |
| UI framework | React 18 |
| Routing | React Router v6 |
| Design system | Ant Design (antd) v5 |
| HTTP client | Axios |
| Testing | Vitest + React Testing Library |

#### npm package name
Set `"name": "lti-ats-frontend"` in `package.json`.

#### Application shell

`App.tsx` must render an Ant Design `<Layout>` with:
- A `<Header>` displaying the application name: **"LTI Applicant Tracking System"**.
- A `<Sider>` with a navigation `<Menu>` linking to the available pages (starting with
  "Positions").
- A `<Content>` area where the router renders the active page.

The default route (`/`) must redirect to `/positions`.

#### Positions page — `src/pages/PositionsPage.tsx`

This is the landing page of the application. It must:
- Fetch the list of positions from `GET /api/v1/positions` on mount using the API service.
- Display the results in an Ant Design `<Table>` with the following columns:

| Column header | Data field | Notes |
|---|---|---|
| Title | `title` | Clickable; navigates to the position detail page |
| Department | `department` | Plain text |
| Open Since | `openDate` | Formatted as `YYYY-MM-DD` |

- Show an Ant Design `<Spin>` indicator while the data is loading.
- Show an Ant Design `<Alert>` with type `error` if the API call fails.
- Include an Ant Design `<Button type="primary">` labelled "New Position" in the page
  header that will eventually open a creation form (for this seed, it can be a no-op
  placeholder that logs to the console — but the button must be visible and correctly
  styled).

#### Source structure
- `src/services/api.ts` — Axios instance with `baseURL: '/api/v1'`. Exports the `Position`
  interface and one typed named function per CRUD operation (`getPositions`, `getPosition`,
  `createPosition`, `updatePosition`, `deletePosition`).
- `src/pages/` — one page component per route. Start with `PositionsPage.tsx`.
- `src/components/` — reusable Ant Design-based UI components.
- `src/router/index.tsx` — React Router `<Routes>` tree; default route redirects to
  `/positions`, which renders `PositionsPage`.
- `App.tsx` — root component as described above.
- `main.tsx` — entry point; mounts `<App />` into `#root`.
- `tsconfig.json` — TypeScript compiler config; strict mode, `react-jsx`, `vitest/globals` types.
- `tsconfig.node.json` — TypeScript config for `vite.config.ts`.

Vite must proxy `/api` to `http://localhost:8080` so the frontend talks to the backend
without CORS issues during local development.

#### Testing
- Place test files alongside the component: `ComponentName.test.tsx`.
- Use Vitest + React Testing Library. Mock Axios calls with `vi.mock`. Use `vi.mocked()` to access mock-specific methods with correct TypeScript types.
- `PositionsPage.test.tsx` must cover:
  - Renders the "LTI Applicant Tracking System" heading.
  - Displays the three seed positions (Title, Department, Open Since columns) when the
    API resolves successfully.
  - Shows an error alert when the API call fails.
  - Shows the "New Position" button.
- Create `src/test/setup.ts` imported as the Vitest `setupFiles` entry.

---

### Developer Convenience Script — `dev.sh`

Create an executable shell script `dev.sh` at the repository root that starts all local
processes in the correct order:

1. Starts Docker Compose in detached mode (`docker compose up -d`).
2. Waits for PostgreSQL to be ready before starting the backend (use `docker compose exec`
   with `pg_isready` in a loop, or a reasonable fixed sleep with a clear log message).
3. Starts the backend in the background (`mvn spring-boot:run -pl lti-ats-backend`),
   redirecting output to `logs/backend.log`.
4. Starts the frontend dev server in the background
   (`npm --prefix lti-ats-frontend run dev`), redirecting output to `logs/frontend.log`.
5. Prints a summary with the URLs:
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:8080/api/v1`
   - Positions endpoint: `http://localhost:8080/api/v1/positions`
   - Health check: `http://localhost:8080/actuator/health`
6. Traps `SIGINT` (Ctrl+C) to cleanly shut down all background processes and stop Docker
   Compose on exit.

The script must create the `logs/` directory automatically if it does not exist.
Add `logs/` to `.gitignore`.

---

### Documentation — `README.md`

Generate a `README.md` at the repository root. It must contain the following sections,
written in clear English prose with concrete commands:

1. **Overview** — one short paragraph describing the LTI Applicant Tracking System: what it
   is, what it does (manage open positions and hiring pipelines), and its tech stack.
2. **Prerequisites** — list of tools required (Java 21+, Maven 3.9+, Node 20+, npm 10+,
   Docker with Compose plugin).
3. **Local development**
   - Option A — one-command start using `./dev.sh`.
   - Option B — step-by-step manual start (Docker Compose, then backend, then frontend),
     with the exact commands to run in each case.
   - URLs table (Frontend / Backend API / Positions endpoint / Health check).
4. **Running the tests**
   - Backend: `mvn verify` (explain that Testcontainers pulls PostgreSQL automatically,
     no running database is needed).
   - Frontend: `npm --prefix lti-ats-frontend test`.
5. **Building Docker images**
   - Backend image: `docker build -t lti-ats-backend ./lti-ats-backend`.
   - Frontend image: `docker build -t lti-ats-frontend ./lti-ats-frontend`.
   - Running both images together: include a sample sequence of `docker network create`,
     `docker run` for the backend, and `docker run` for the frontend (passing `BACKEND_URL`,
     `SPRING_DATASOURCE_*` env vars, and linking to a running PostgreSQL container).
6. **Project structure** — reproduce the repository tree from the Repository Structure
   section of this prompt.
7. **Architecture** — brief description of the backend layering (web → service →
   infrastructure) and the frontend structure (pages, components, services).

---

### `AGENTS.md` — AI Agent Guidance

Generate an `AGENTS.md` file at the repository root. This file is the primary source of
truth for any AI coding agent working on this repository after the initial bootstrap. Write
it with the following content and structure:

```markdown
# AGENTS.md

## Project overview
[One paragraph: describe the LTI Applicant Tracking System, its purpose, its current
domain scope (Positions), and the intent that future iterations will be AI-agent-driven.]

## Repository layout
[Reproduce the directory tree with a one-line description of each top-level entry.]

## Backend — key conventions
- Language and runtime: Java 21, Spring Boot 4.x.
- DB access: Spring Data JDBC only. Never use jakarta.persistence or Hibernate.
- Package structure: config / domain / infrastructure / service / web — describe the
  responsibility of each.
- Flyway migration naming: V{major}.{minor}.{patch}.{sequence}__{description}.sql.
  Always create a new migration file for schema changes; never modify an existing one.
- Testing: prefer integration tests over unit tests. Use Testcontainers for all tests
  that touch the database. Never mock the repository layer.
- REST conventions: /api/v1/{resource}, plural nouns, standard HTTP status codes.
- Current entities: Position (id, title, department, open_date, created_at, updated_at).

## Frontend — key conventions
- Language: plain JavaScript (no TypeScript).
- All API calls go through src/services/api.ts; never call Axios directly from components.
- Routing is centralised in src/router/index.tsx. Default route redirects to /positions.
- Use Ant Design components. Do not introduce additional UI libraries.
- Tests live alongside the file they test (.test.tsx) and use Vitest + React Testing Library. Use vi.mocked() for typed mock access.
- Current pages: PositionsPage (lists all open positions).

## Running locally
[Refer to README.md. One-liner: `./dev.sh`.]

## Docker images
[Refer to README.md. Note SPRING_DATASOURCE_* env vars for the backend image and
BACKEND_URL for the frontend image.]

## What to do before opening a pull request
- Run `mvn verify` and ensure all backend integration tests pass.
- Run `npm --prefix lti-ats-frontend test` and ensure all frontend tests pass.
- If you add a new entity, add a Flyway migration and update the "Current entities"
  section in this file.
- If you add a new page, update the "Current pages" section in this file.
- Ensure no new dependencies have been introduced without a justification comment in
  the relevant pom.xml or package.json.
```

The agent must fill in all bracketed placeholders with accurate, project-specific content
derived from what it has generated. `AGENTS.md` must not contain generic advice; every rule
must be grounded in the actual codebase.

---

### Explicit Prohibitions

Do not generate any of the following:

- JavaScript files (`.js`, `.jsx`) in the frontend — use TypeScript exclusively.
- `jakarta.persistence` annotations anywhere in the backend.
- Unit tests that mock the repository layer.
- Helm or Kubernetes orchestration configuration.
- CI/CD pipeline files of any kind.
- OpenAPI / Swagger / springdoc dependencies or annotations.
- Spring Security or OAuth2 dependencies.
- Multiple `application.yml` profile files.
- Placeholder comments, TODO stubs, or unimplemented methods.

---

### Definition of Done

The repository is complete when all of the following hold:

1. `mvn verify` at the repository root succeeds with all backend integration tests green,
   including the assertion that `GET /api/v1/positions` returns the three seed positions.
2. `./dev.sh` starts Docker Compose, the backend, and the frontend in one command with no
   manual steps. Ctrl+C stops everything cleanly.
3. The backend is reachable at `http://localhost:8080/api/v1/positions` and returns the
   three seed positions as JSON. The health endpoint returns `200` at
   `http://localhost:8080/actuator/health`.
4. The frontend is reachable at `http://localhost:3000`, shows the heading
   **"LTI Applicant Tracking System"**, and the Positions page renders the three seed
   positions in an Ant Design table.
5. `npm --prefix lti-ats-frontend test` passes all frontend component tests.
6. `docker build -t lti-ats-backend ./lti-ats-backend` and
   `docker build -t lti-ats-frontend ./lti-ats-frontend` both succeed and produce
   runnable images.
7. `README.md` is accurate and complete — a developer with no prior knowledge of this
   repository can follow it from clone to running application without any external help.
8. `AGENTS.md` is accurate and complete — an AI coding agent can use it as the sole
   source of truth to make correct, convention-consistent changes to the codebase.
