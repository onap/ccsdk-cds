#!/usr/bin/env bash
# start-dev.sh
#
# Start all three services that make up the e2e test environment without
# running any tests.  Useful for manual exploratory testing and debugging.
#
#   0. mock-processor   – Node HTTP stub for blueprints-processor  (port 8080)
#   1. LoopBack BFF     – TypeScript backend                       (port 3000)
#   2. Angular dev-server – frontend with test proxy config        (port 4200)
#
# Usage (from cds-ui/e2e-playwright):
#   ./start-dev.sh
#
# Stop everything with Ctrl-C; the EXIT trap kills all child processes.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$(cd "$SCRIPT_DIR/../server" && pwd)"
CLIENT_DIR="$(cd "$SCRIPT_DIR/../designer-client" && pwd)"
PROXY_CONF="$SCRIPT_DIR/proxy.conf.test.json"
BACKEND_SCRIPT="$SCRIPT_DIR/start-backend-http.js"
MOCK_SCRIPT="$SCRIPT_DIR/mock-processor/server.js"

# ── colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
log()  { echo -e "${GREEN}[start-dev]${NC} $*"; }
warn() { echo -e "${YELLOW}[start-dev]${NC} $*"; }
err()  { echo -e "${RED}[start-dev]${NC} $*" >&2; }

# ── cleanup on exit ────────────────────────────────────────────────────────────
PIDS=()
cleanup() {
    log "Shutting down all services..."
    for pid in "${PIDS[@]}"; do
        kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
    log "All services stopped."
}
trap cleanup EXIT INT TERM

# ── wait for a URL to return HTTP 200 ─────────────────────────────────────────
wait_for_url() {
    local url="$1" label="$2" attempts=0 max=60
    warn "Waiting for $label at $url ..."
    until curl -sf "$url" -o /dev/null; do
        attempts=$((attempts + 1))
        if [[ $attempts -ge $max ]]; then
            err "$label did not become ready within ${max}s – aborting."
            exit 1
        fi
        sleep 1
    done
    log "$label is ready."
}

# ── 0. mock-processor ──────────────────────────────────────────────────────────
log "Starting mock-processor (port 8080)..."
node "$MOCK_SCRIPT" &
PIDS+=($!)

wait_for_url "http://localhost:8080/api/v1/blueprint-model/" "mock-processor"

# ── 1. LoopBack BFF ───────────────────────────────────────────────────────────
log "Building and starting LoopBack BFF (port 3000)..."
(cd "$SERVER_DIR" && npm run build && node "$BACKEND_SCRIPT") &
PIDS+=($!)

wait_for_url "http://localhost:3000/ping" "LoopBack BFF"

# ── 2. Angular dev-server ─────────────────────────────────────────────────────
log "Starting Angular dev-server (port 4200)..."
(cd "$CLIENT_DIR" && NODE_OPTIONS=--openssl-legacy-provider npx ng serve \
    --port 4200 \
    --proxy-config "$PROXY_CONF") &
PIDS+=($!)

wait_for_url "http://localhost:4200" "Angular dev-server"

# ── all up ────────────────────────────────────────────────────────────────────
echo ""
log "All services are running:"
log "  mock-processor   → http://localhost:8080/api/v1/blueprint-model/"
log "  LoopBack BFF     → http://localhost:3000/ping"
log "  Angular UI       → http://localhost:4200"
echo ""
log "Press Ctrl-C to stop all services."

# Block until interrupted
wait
