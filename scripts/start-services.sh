#!/usr/bin/env bash
# Start all Spring Boot services as background processes with small heaps.
set -euo pipefail
source "$(dirname "$0")/env.sh"

SERVICES=(mall-user mall-product mall-inventory mall-order mall-payment mall-gateway)
PORTS=(9001 9002 9003 9004 9005 9000)

for jar in "${SERVICES[@]}"; do
  if [[ ! -f "$MALL_HOME/$jar/target/$jar-1.0.0.jar" ]]; then
    echo "Missing jar for $jar. Run scripts/build.sh first." >&2
    exit 1
  fi
done

for i in "${!SERVICES[@]}"; do
  svc="${SERVICES[$i]}"
  port="${PORTS[$i]}"
  pid_file="$RUN_DIR/$svc.pid"

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "==> $svc already running (pid $(cat "$pid_file"))"
    continue
  fi

  echo "==> Starting $svc on $port"
  nohup "$JAVA_HOME/bin/java" \
    -Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m \
    -Duser.timezone=Asia/Shanghai \
    -Dhttp.nonProxyHosts='*' -Dhttps.nonProxyHosts='*' \
    -jar "$MALL_HOME/$svc/target/$svc-1.0.0.jar" \
    > "$LOG_DIR/$svc.log" 2>&1 &
  echo $! > "$pid_file"
done

echo "==> Waiting for services"
failed=0
for i in "${!SERVICES[@]}"; do
  svc="${SERVICES[$i]}"
  port="${PORTS[$i]}"
  ok=0
  for _ in $(seq 1 90); do
    if curl -sf "http://127.0.0.1:$port/actuator/health" >/dev/null 2>&1; then
      echo "  $svc is healthy on $port"
      ok=1
      break
    fi
    sleep 1
  done
  if [[ "$ok" -eq 0 ]]; then
    echo "  ERROR: $svc did not become healthy. See $LOG_DIR/$svc.log" >&2
    failed=1
  fi
done

if [[ "$failed" -ne 0 ]]; then
  exit 1
fi

echo "==> All services are up: http://127.0.0.1:9000"
