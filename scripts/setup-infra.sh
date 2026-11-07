#!/usr/bin/env bash
# One-time initialization of the project-local middleware (MySQL data dir, Redis config).
set -euo pipefail
source "$(dirname "$0")/env.sh"

echo "==> Checking project-local toolchain"
for path in "$JAVA_HOME/bin/java" "$MAVEN_HOME/bin/mvn" "$MYSQL_HOME/bin/mysqld" \
            "$REDIS_HOME/src/redis-server" "$NACOS_HOME/bin/startup.sh"; do
  if [[ ! -e "$path" ]]; then
    echo "Missing: $path" >&2
    echo "Run scripts/download-tools.sh (or restore .tools) first." >&2
    exit 1
  fi
done
echo "  toolchain OK"

echo "==> Initializing MySQL data directory"
mkdir -p "$DATA_DIR/mysql"
if [[ ! -d "$DATA_DIR/mysql/mysql" ]]; then
  "$MYSQL_HOME/bin/mysqld" --initialize-insecure \
    --basedir="$MYSQL_HOME" --datadir="$DATA_DIR/mysql" \
    --log-error="$DATA_DIR/mysql-init.err"
  echo "  MySQL data directory initialized (user root, empty password, local only)"
else
  echo "  MySQL data directory already exists"
fi

echo "==> Writing Redis config"
mkdir -p "$DATA_DIR/redis"
cat > "$DATA_DIR/redis.conf" <<EOF
port $REDIS_PORT
bind 127.0.0.1
protected-mode yes
daemonize yes
dir $DATA_DIR/redis
logfile $DATA_DIR/redis.log
save ""
appendonly no
EOF
echo "  config written to $DATA_DIR/redis.conf"

echo "==> Done"
