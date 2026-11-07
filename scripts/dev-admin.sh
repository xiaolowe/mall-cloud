#!/usr/bin/env bash
# Run the Vue 3 admin dev server on :5176 with /api proxied to the gateway on :9000.
set -euo pipefail
source "$(dirname "$0")/env.sh"

cd "$MALL_HOME/mall-admin"
export PATH="$NODE_HOME/bin:$PATH"

if [[ ! -d node_modules ]]; then
  npm install
fi

exec npm run dev
