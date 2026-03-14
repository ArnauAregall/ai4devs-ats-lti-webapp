# Implementation Plan: LTI Applicant Tracking System Bootstrap

## Context

The repository at `/Users/arnau.aregall/git/arnau/master-ai4devs/exercises/ai4devs-ats-lti-webapp` is currently empty (only `README.md` placeholder and `prompt-1.md` spec). The goal is to scaffold a complete full-stack monorepo for the LTI ATS from scratch, following the spec in `prompt-1.md` exactly.

---

## Files to Create

### Root level
| File | Purpose |
|------|---------|
| `pom.xml` | Maven aggregator (no parent, declares `lti-ats-backend` and `lti-ats-frontend`) |
| `docker-compose.yml` | PostgreSQL 16 only, named volume |
| `.gitignore` | Standard Java + Node ignores, plus `logs/` |
| `dev.sh` | Convenience script (chmod +x) |
| `README.md` | Full project documentation (overwrite placeholder) |
| `AGENTS.md` | AI agent guidance |

### Backend module — `lti-ats-backend/`
| File | Purpose |
|------|---------|
| `pom.xml` | Spring Boot 4.x, Data JDBC, Flyway, Lombok, Testcontainers, RestAssured |
| `Dockerfile` | Multi-stage: Maven build + Eclipse Temurin 21 JRE runtime |
| `src/main/java/.../Application.java` | `@SpringBootApplication` entry point |
| `src/main/java/.../domain/entity/Position.java` | `@Table("position")` Spring Data JDBC entity |
| `src/main/java/.../domain/dto/CreatePositionRequest.java` | Java record |
| `src/main/java/.../domain/dto/UpdatePositionRequest.java` | Java record |
| `src/main/java/.../domain/dto/PositionResponse.java` | Java record |
| `src/main/java/.../infrastructure/persistence/PositionRepository.java` | `CrudRepository<Position, UUID>` |
| `src/main/java/.../service/PositionService.java` | Business logic / orchestration |
| `src/main/java/.../web/PositionController.java` | `@RestController` at `/api/v1/positions` |
| `src/main/java/.../web/GlobalExceptionHandler.java` | `@RestControllerAdvice` |
| `src/main/resources/application.yml` | DB config via env vars, Flyway config, Actuator |
| `src/main/resources/db/migration/V1.0.0.0__create_position_table.sql` | DDL |
| `src/main/resources/db/migration/V1.0.0.1__seed_positions.sql` | 3 seed rows |
| `src/test/java/.../web/PositionControllerIntegrationTest.java` | Full integration test |

### Frontend module — `lti-ats-frontend/`
| File | Purpose |
|------|---------|
| `pom.xml` | Maven module using `frontend-maven-plugin`; runs `npm install` + `npm test` via `mvn verify` |
| `package.json` | React 18, Vite, antd v5, Axios, React Router v6, Vitest, RTL, TypeScript |
| `tsconfig.json` | TypeScript config; targets ES2020, `react-jsx`, strict mode, `vitest/globals` types |
| `tsconfig.node.json` | TypeScript config for `vite.config.ts` (composite, bundler resolution) |
| `vite.config.ts` | Proxy `/api` → `http://localhost:8080`, test config; uses `vitest/config` |
| `index.html` | Vite HTML entry |
| `nginx.conf` | SPA fallback + `/api/` proxy via envsubst |
| `Dockerfile` | Multi-stage: Node 20 build + nginx:alpine runtime |
| `src/main.tsx` | Mounts `<App />` into `#root` |
| `src/App.tsx` | Ant Design `<Layout>` with Header/Sider/Content |
| `src/router/index.tsx` | React Router routes; default → `/positions` |
| `src/services/api.ts` | `Position` interface + Axios instance + 5 typed CRUD functions |
| `src/pages/PositionsPage.tsx` | Table with loading/error states, typed columns, "New Position" button |
| `src/pages/PositionsPage.test.tsx` | Vitest + RTL tests (4 scenarios); uses `vi.mocked()` for typed mocks |
| `src/test/setup.ts` | Vitest setupFiles entry; ResizeObserver + matchMedia polyfills |

---

## Implementation Steps (Ordered)

### Step 1 — Root scaffolding
1. Create `.gitignore` (Java, Node, `logs/`, `target/`, `dist/`, `node/`)
2. Create root `pom.xml` — aggregator with `lti-ats-backend` and `lti-ats-frontend` modules, `groupId: tech.aregall.lidr.lti`, `artifactId: lti-ats`
3. Create `docker-compose.yml` — PostgreSQL 16 service with named volume, credentials `lti_ats/lti_ats/lti_ats`, port 5432
4. Create `dev.sh` (executable) — Docker Compose → wait for PG → backend bg → frontend bg → trap SIGINT
5. Overwrite `README.md` with full documentation
6. Create `AGENTS.md`

### Step 2 — Backend: config & domain
1. Create `lti-ats-backend/pom.xml` — parent Spring Boot 4.x BOM, dependencies: spring-boot-starter-web, spring-boot-starter-data-jdbc, spring-boot-starter-actuator, spring-boot-starter-validation, postgresql driver, flyway-core, lombok, testcontainers, spring-boot-testcontainers, rest-assured (spring6-mock-mvc)
2. Create `Application.java`
3. Create `Position.java` entity (Spring Data JDBC annotations, Lombok `@Data`)
4. Create request/response records: `CreatePositionRequest`, `UpdatePositionRequest`, `PositionResponse`
5. Create `PositionRepository` extending `CrudRepository<Position, UUID>`

### Step 3 — Backend: service & web
1. Create `PositionService` — CRUD methods, throws `PositionNotFoundException` for missing
2. Create `PositionController` — all 5 endpoints at `/api/v1/positions`
3. Create `PositionNotFoundException` — extends RuntimeException
4. Create `GlobalExceptionHandler` — handle `PositionNotFoundException` → 404, validation errors → 400
5. Create `application.yml` — datasource from env vars (with defaults), flyway, actuator health exposure
6. Create Flyway migrations: `V1.0.0.0__create_position_table.sql`, `V1.0.0.1__seed_positions.sql`

### Step 4 — Backend: Dockerfile
Multi-stage Dockerfile using `maven:3.9-eclipse-temurin-21` build stage → `eclipse-temurin:21-jre-alpine` runtime stage.

### Step 5 — Backend: integration tests
Create `PositionControllerIntegrationTest` with:
- Shared static `PostgreSQLContainer`
- `@DynamicPropertySource` for datasource config
- `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`
- RestAssured MockMvc setup in `@BeforeAll`
- Test: `GET /api/v1/positions` → 200, returns 3 seed positions
- Test: `GET /api/v1/positions/{id}` → 200 (existing), 404 (missing)
- Test: `POST /api/v1/positions` → 201 created
- Test: `PUT /api/v1/positions/{id}` → 200 (existing), 404 (missing)
- Test: `DELETE /api/v1/positions/{id}` → 204 (existing), 404 (missing)
- Test: `POST /api/v1/positions` with invalid body → 400

### Step 6 — Frontend: foundation
1. Create `pom.xml` using `com.github.eirslett:frontend-maven-plugin:1.15.1`; bind `install-node-and-npm` to `initialize`, `npm install` to `generate-resources`, `npm test` to `test` phase
2. Create `package.json` with all dependencies including `typescript`, `@types/react`, `@types/react-dom`
3. Create `tsconfig.json` and `tsconfig.node.json`
2. Create `vite.config.js` with proxy and vitest config
3. Create `index.html`
4. Create `src/test/setup.js`
5. Create `src/services/api.js`
6. Create `src/router/index.jsx`

### Step 7 — Frontend: components & pages
1. Create `src/main.jsx`
2. Create `src/App.jsx` — Layout with Header, Sider (Menu linking to Positions), Content
3. Create `src/pages/PositionsPage.jsx` — Table, Spin, Alert, "New Position" button
4. Create `src/pages/PositionsPage.test.jsx` — 4 test cases

### Step 8 — Frontend: Dockerfile + nginx
1. Create `nginx.conf` — SPA fallback, `/api/` proxy with envsubst for `BACKEND_URL`
2. Create `Dockerfile` — Node 20 build stage → nginx:alpine runtime with envsubst entrypoint

---

## Key Technical Decisions

- **Frontend as Maven module**: `lti-ats-frontend/pom.xml` uses `com.github.eirslett:frontend-maven-plugin` to download Node 20, run `npm install`, and execute `npm test` (`vitest run`) during the `test` phase. This means `mvn verify` from the root builds and tests both modules in one command. The `node/` directory (downloaded Node binary) is git-ignored.
- **TypeScript**: The frontend uses TypeScript throughout (`.ts` for logic, `.tsx` for components). Domain types are centralized in `src/services/api.ts`. Vitest globals (`vi`, `describe`, etc.) are typed via `"types": ["vitest/globals"]` in `tsconfig.json`. Mocked functions in tests use `vi.mocked()` for type-safe mock access.
- **Spring Boot 4.x**: Uses `jakarta.*` namespace (except not `jakarta.persistence`). Spring Data JDBC is safe.
- **UUID primary keys**: PostgreSQL `gen_random_uuid()` as default; Java side leaves `id = null` on new entities (Spring Data JDBC inserts and retrieves generated key).
- **No `@Transactional` in tests**: State managed via direct repository calls between tests. Created IDs tracked per-test and deleted in `@AfterEach`.
- **`@TestInstance(PER_CLASS)`**: Allows `@BeforeAll` to be non-static so `MockMvc` can be autowired before RestAssured setup.
- **envsubst in nginx**: Dockerfile CMD uses `envsubst '${BACKEND_URL}'` to replace only that variable in nginx.conf template, leaving nginx variables (`$uri`, `$host`, etc.) untouched.
- **Vite proxy**: `/api` → `http://localhost:8080` for local dev, no CORS needed.
- **Test renders App**: `PositionsPage.test.jsx` renders `<App />` wrapped in `MemoryRouter` to exercise the full layout including the "LTI Applicant Tracking System" heading.

---

## Verification

1. `mvn verify` — builds and tests both modules: backend integration tests (Testcontainers PG) + frontend Vitest tests
2. `./dev.sh` — starts everything; `curl http://localhost:8080/api/v1/positions` returns 3 positions
3. `docker build -t lti-ats-backend ./lti-ats-backend` — succeeds
4. `docker build -t lti-ats-frontend ./lti-ats-frontend` — succeeds
5. Browser at `http://localhost:3000` shows the ATS heading and positions table
