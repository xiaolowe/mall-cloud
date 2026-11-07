#!/usr/bin/env bash
# Build all modules with the project-local JDK and Maven.
set -euo pipefail
source "$(dirname "$0")/env.sh"

cd "$MALL_HOME"

if [[ -x "$NODE_HOME/bin/node" && -f "$MALL_HOME/mall-web/package.json" ]]; then
  "$MALL_HOME/scripts/build-web.sh"
fi

"$MAVEN_HOME/bin/mvn" -s "$MALL_HOME/maven-settings.xml" \
  -Dmaven.repo.local="$MALL_HOME/.m2repo" \
  -DskipTests clean package "$@"
