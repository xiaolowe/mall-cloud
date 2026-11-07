#!/usr/bin/env bash
# Stop the project-local middleware.
set -euo pipefail
source "$(dirname "$0")/env.sh"

echo "==> Stopping Nacos"
if port_open 8848; then
  (cd "$NACOS_HOME" && sh bin/shutdown.sh >/dev/null 2>&1 || true)
  echo "  stopped"
else
  echo "  not running"
fi

echo "==> Stopping Redis"
if port_open "$REDIS_PORT"; then
  "$REDIS_HOME/src/redis-cli" -p "$REDIS_PORT" shutdown nosave >/dev/null 2>&1 || true
  echo "  stopped"
else
  echo "  not running"
fi

echo "==> Stopping MySQL"
if [[ -S "$MYSQL_SOCKET" ]]; then
  "$MYSQL_HOME/bin/mysqladmin" --socket="$MYSQL_SOCKET" -u"$MYSQL_USER" shutdown >/dev/null 2>&1 || true
  echo "  stopped"
else
  echo "  not running"
fi
