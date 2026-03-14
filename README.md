# LTI Applicant Tracking System

## Overview

The LTI Applicant Tracking System (ATS) is a web application that helps organizations manage open job positions and move candidates through a structured hiring pipeline. It is built as a full-stack monorepo: a **Spring Boot 4.x** backend exposing a RESTful API backed by **PostgreSQL 16** with **Spring Data JDBC** and **Flyway** migrations, and a **React 18 + TypeScript** single-page frontend built with **Vite**, **Ant Design 5**, and **Axios**. The project is designed to be iterated on by AI coding agents, guided by `AGENTS.md`.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 10+ |
| Docker (with Compose plugin) | Latest |

> **Docker mode** (`run.sh docker`) only requires Docker — Java, Maven and Node are not needed on the host.

### Installing Java and Maven with SDKMAN!

[SDKMAN!](https://sdkman.io) is the recommended way to install and manage Java and Maven on macOS and Linux.

**1. Install SDKMAN!:**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

**2. Install Java 21:**
```bash
sdk install java 21.0.7-tem
```

**3. Install Maven 3.9:**
```bash
sdk install maven 3.9.9
```

Verify the installation:
```bash
java -version   # should print openjdk 21...
mvn -version    # should print Apache Maven 3.9...
```

> To list all available Java versions: `sdk list java`

### Installing Node.js with nvm

[nvm](https://github.com/nvm-sh/nvm) is the recommended way to install and manage Node.js versions.

**1. Install nvm:**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.nvm/nvm.sh
```

**2. Install Node 20:**
```bash
nvm install 20
nvm use 20
```

Verify:
```bash
node -v   # should print v20...
npm -v    # should print 10...
```

---

## Running the Application

A single script `run.sh` covers both local development and fully containerised runs.

### Dev mode — native backend and frontend, Postgres in Docker

```bash
chmod +x run.sh   # only needed once
./run.sh dev
```

This script:
1. Starts PostgreSQL via Docker Compose.
2. Waits until the database is ready.
3. Starts the Spring Boot backend in the background (`logs/backend.log`).
4. Starts the Vite dev server in the background (`logs/frontend.log`).
5. Prints all service URLs.

Press **Ctrl+C** to stop everything cleanly.

### Docker mode — everything in containers

```bash
./run.sh docker
```

This runs `docker compose --profile docker up --build`. Docker Compose builds the backend and frontend images automatically if they are missing or outdated, then starts all three services (Postgres, backend, frontend) with proper dependency ordering and health checks.

Press **Ctrl+C** to stop all containers.

---

### Manual step-by-step (dev mode)

**1. Start PostgreSQL:**
```bash
docker compose up -d postgres
```

**2. Start the backend:**
```bash
mvn spring-boot:run -pl lti-ats-backend
```

**3. Start the frontend (in a separate terminal):**
```bash
npm --prefix lti-ats-frontend install   # first time only
npm --prefix lti-ats-frontend run dev
```

---

### Service URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1 |
| Positions endpoint | http://localhost:8080/api/v1/positions |
| Health check | http://localhost:8080/actuator/health |

---

## Running the Tests

### All modules (backend + frontend)

```bash
mvn verify
```

This single command runs the full test suite for both modules:
- **Backend**: integration tests via **Testcontainers** (automatically pulls and starts a PostgreSQL container — no running database required).
- **Frontend**: Vitest unit/component tests via the `frontend-maven-plugin` (downloads Node 20 on first run, then executes `npm test`).

### Backend only

```bash
mvn verify -pl lti-ats-backend
```

### Frontend only

```bash
mvn test -pl lti-ats-frontend
# or directly with npm:
npm --prefix lti-ats-frontend test
```

---

## Docker Compose Services

`docker-compose.yml` defines three services:

| Service | Profile | Description |
|---------|---------|-------------|
| `postgres` | _(always)_ | PostgreSQL 16 with a named volume and health check |
| `backend` | `docker` | Spring Boot API; waits for Postgres to be healthy |
| `frontend` | `docker` | React SPA served by Nginx; proxies `/api` to the backend |

The `docker` profile is activated by `run.sh docker` (or manually with `--profile docker`).

### Building images manually

```bash
# Backend — build context must be the repo root
docker build -t lti-ats-backend -f lti-ats-backend/Dockerfile .

# Frontend
docker build -t lti-ats-frontend ./lti-ats-frontend
```

---

## Project Structure

```
lti-ats/
├── AGENTS.md                   ← AI agent guidance
├── README.md                   ← Project documentation
├── run.sh                      ← Unified run script: ./run.sh dev | docker
├── docker-compose.yml          ← PostgreSQL (always) + backend/frontend (docker profile)
├── pom.xml                     ← Maven aggregator; declares lti-ats-backend + lti-ats-frontend
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
    ├── pom.xml                 ← Maven module; runs npm install + npm test via mvn verify
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

## Architecture

### Backend

The backend follows a strict layered architecture:

- **`web/`** — `@RestController` classes handle HTTP requests and delegate to the service layer. A single `@RestControllerAdvice` (`GlobalExceptionHandler`) handles all exception-to-HTTP-status mapping.
- **`service/`** — Orchestration layer. Services depend on repositories and other services. They contain no SQL and no web concerns.
- **`infrastructure/persistence/`** — Repository interfaces extending `CrudRepository` or `PagingAndSortingRepository`. All query logic lives here.
- **`domain/entity/`** — Spring Data JDBC entities annotated with `@Table` and `@Id`.
- **`domain/dto/`** — Immutable Java records for request bodies and response payloads.
- **`config/`** — Only `@Configuration` classes and `@ConfigurationProperties` beans.

Database schema is managed exclusively by **Flyway**. Never modify an existing migration file — always add a new one.

### Frontend

- **`src/services/api.ts`** — Single Axios instance with `baseURL: '/api/v1'`. Exports the `Position` interface and named functions for every CRUD operation; components never call Axios directly.
- **`src/router/index.tsx`** — Centralised React Router `<Routes>` tree. The default route redirects to `/positions`.
- **`src/pages/`** — One component per route. Each page is responsible for its own data fetching.
- **`src/components/`** — Reusable Ant Design-based UI components shared across pages.

Vite proxies `/api` to `http://localhost:8080` during local development, so the frontend and backend run without CORS configuration. In Docker mode, Nginx proxies `/api` to the `backend` service.
