#!/usr/bin/env bash
# Build the Vue 3 admin frontend into mall-admin/dist.
set -euo pipefail
source "$(dirname "$0")/env.sh"

if [[ ! -x "$NODE_HOME/bin/node" ]]; then
  echo "Node.js not found at $NODE_HOME. Run scripts/download-tools.sh first." >&2
  exit 1
fi

cd "$MALL_HOME/mall-admin"
export PATH="$NODE_HOME/bin:$PATH"

if [[ ! -d node_modules ]]; then
  npm install
fi

npm run build
echo "==> Admin frontend built: $MALL_HOME/mall-admin/dist"
