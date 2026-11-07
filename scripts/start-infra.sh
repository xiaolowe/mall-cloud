#!/usr/bin/env bash
# Start MySQL, Redis and Nacos from the project-local .tools directory.
set -euo pipefail
source "$(dirname "$0")/env.sh"

echo "==> Starting MySQL on $MYSQL_PORT"
if port_open "$MYSQL_PORT"; then
  echo "  MySQL already running"
else
  "$MYSQL_HOME/bin/mysqld" \
    --basedir="$MYSQL_HOME" --datadir="$DATA_DIR/mysql" \
    --port="$MYSQL_PORT" --socket="$MYSQL_SOCKET" \
    --pid-file="$RUN_DIR/mysql.pid" --log-error="$DATA_DIR/mysql.err" \
    --mysqlx=0 --daemonize
  wait_for_port "$MYSQL_PORT" "MySQL" 60
fi

echo "==> Applying schema (idempotent)"
"$MYSQL_HOME/bin/mysql" --socket="$MYSQL_SOCKET" -u"$MYSQL_USER" \
  --default-character-set=utf8mb4 < "$MALL_HOME/sql/init.sql"
echo "  schema ready"

echo "==> Starting Redis on $REDIS_PORT"
if port_open "$REDIS_PORT"; then
  echo "  Redis already running"
else
  "$REDIS_HOME/src/redis-server" "$DATA_DIR/redis.conf"
  wait_for_port "$REDIS_PORT" "Redis" 30
fi

echo "==> Starting Nacos on 8848"
if port_open 8848; then
  echo "  Nacos already running"
else
  export JAVA_OPT_EXT="-Xms256m -Xmx256m -Xmn128m"
  (cd "$NACOS_HOME" && nohup sh bin/startup.sh -m standalone > "$LOG_DIR/nacos-start.log" 2>&1 &)
  wait_for_port 8848 "Nacos" 120
fi

echo "==> Infrastructure is ready"
