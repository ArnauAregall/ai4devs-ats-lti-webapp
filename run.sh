#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODE="${1:-}"

usage() {
    echo "Usage: $0 <dev|docker>"
    echo ""
    echo "  dev    — PostgreSQL in Docker; backend and frontend run natively"
    echo "           (requires Java 21+, Maven 3.9+, Node 20+)"
    echo "  docker — everything in Docker containers; images are built if needed"
    echo ""
    exit 1
}

[ -z "$MODE" ] && usage
[ "$MODE" != "dev" ] && [ "$MODE" != "docker" ] && usage

# ── docker mode ────────────────────────────────────────────────────────────────
if [ "$MODE" = "docker" ]; then
    echo "==> Starting all services with Docker Compose (profile: docker)..."
    echo "    Images will be built automatically if they are missing or outdated."
    echo ""
    docker compose -f "$ROOT_DIR/docker-compose.yml" --profile docker up --build
    exit 0
fi

# ── dev mode ───────────────────────────────────────────────────────────────────
mkdir -p "$ROOT_DIR/logs"
BACKEND_PID=""
FRONTEND_PID=""

cleanup() {
    echo ""
    echo "Shutting down..."
    [ -n "$BACKEND_PID" ]  && kill "$BACKEND_PID"  2>/dev/null || true
    [ -n "$FRONTEND_PID" ] && kill "$FRONTEND_PID" 2>/dev/null || true
    docker compose -f "$ROOT_DIR/docker-compose.yml" stop postgres
    echo "Done."
}
trap cleanup SIGINT SIGTERM

echo "==> Starting PostgreSQL..."
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d postgres

echo "==> Waiting for PostgreSQL to be ready..."
until docker compose -f "$ROOT_DIR/docker-compose.yml" exec -T postgres \
    pg_isready -U lti_ats -d lti_ats > /dev/null 2>&1; do
    echo "    PostgreSQL not ready yet, retrying in 2s..."
    sleep 2
done
echo "    PostgreSQL is ready."

echo "==> Starting backend (logs/backend.log)..."
mvn spring-boot:run -pl lti-ats-backend \
    -f "$ROOT_DIR/pom.xml" \
    > "$ROOT_DIR/logs/backend.log" 2>&1 &
BACKEND_PID=$!

echo "==> Starting frontend dev server (logs/frontend.log)..."
npm --prefix "$ROOT_DIR/lti-ats-frontend" install --silent
npm --prefix "$ROOT_DIR/lti-ats-frontend" run dev \
    > "$ROOT_DIR/logs/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo ""
echo "============================================================"
echo "  LTI Applicant Tracking System — dev mode"
echo "============================================================"
echo "  Frontend:     http://localhost:3000"
echo "  Backend API:  http://localhost:8080/api/v1"
echo "  Positions:    http://localhost:8080/api/v1/positions"
echo "  Health:       http://localhost:8080/actuator/health"
echo "  Logs:         ./logs/backend.log  ./logs/frontend.log"
echo "============================================================"
echo "  Press Ctrl+C to stop all processes."
echo ""

wait
