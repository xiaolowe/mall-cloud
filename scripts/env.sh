#!/usr/bin/env bash
# Project-local toolchain. Nothing is installed into the system or ~/.zshrc.
set -euo pipefail

MALL_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export MALL_HOME

export JAVA_HOME="$MALL_HOME/.tools/jdk-17/Contents/Home"
export MAVEN_HOME="$MALL_HOME/.tools/apache-maven-3.9.16"
export MYSQL_HOME="$MALL_HOME/.tools/mysql"
export REDIS_HOME="$MALL_HOME/.tools/redis-7.2.5"
export NACOS_HOME="$MALL_HOME/.tools/nacos"
export NODE_HOME="$MALL_HOME/.tools/node"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$MYSQL_HOME/bin:$REDIS_HOME/src:$NODE_HOME/bin:$PATH"

export DATA_DIR="$MALL_HOME/.data"
export RUN_DIR="$MALL_HOME/.run"
export LOG_DIR="$MALL_HOME/.logs"
mkdir -p "$DATA_DIR" "$RUN_DIR" "$LOG_DIR"

export MYSQL_PORT="${MYSQL_PORT:-3307}"
export MYSQL_SOCKET="$DATA_DIR/mysql.sock"
export MYSQL_USER="${MYSQL_USER:-root}"
export MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
export REDIS_PORT="${REDIS_PORT:-6379}"
export NACOS_ADDR="${NACOS_ADDR:-127.0.0.1:8848}"

port_open() {
  nc -z 127.0.0.1 "$1" >/dev/null 2>&1
}

wait_for_port() {
  local port="$1" name="$2" tries="${3:-60}"
  for _ in $(seq 1 "$tries"); do
    if port_open "$port"; then
      echo "  $name is up on port $port"
      return 0
    fi
    sleep 1
  done
  echo "  ERROR: $name did not start on port $port" >&2
  return 1
}
