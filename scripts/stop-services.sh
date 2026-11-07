#!/usr/bin/env bash
# Stop all Spring Boot services started by start-services.sh.
set -euo pipefail
source "$(dirname "$0")/env.sh"

for pid_file in "$RUN_DIR"/mall-*.pid; do
  [[ -e "$pid_file" ]] || continue
  svc="$(basename "$pid_file" .pid)"
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" 2>/dev/null; then
    echo "==> Stopping $svc (pid $pid)"
    kill "$pid" 2>/dev/null || true
  fi
  rm -f "$pid_file"
done

echo "==> Services stopped"
