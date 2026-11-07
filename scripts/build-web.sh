#!/usr/bin/env bash
# Build the Vue 3 frontend into mall-web/dist.
set -euo pipefail
source "$(dirname "$0")/env.sh"

if [[ ! -x "$NODE_HOME/bin/node" ]]; then
  echo "Node.js not found at $NODE_HOME. Run scripts/download-tools.sh first." >&2
  exit 1
fi

cd "$MALL_HOME/mall-web"
export PATH="$NODE_HOME/bin:$PATH"

if [[ ! -d node_modules ]]; then
  if [[ -f package-lock.json ]]; then
    npm ci
  else
    npm install
  fi
fi

npm run build
echo "==> Frontend built: $MALL_HOME/mall-web/dist"
