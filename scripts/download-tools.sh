#!/usr/bin/env bash
# Downloads the full local toolchain into .tools/ (macOS x86_64).
# Nothing is installed system-wide and no sudo is required.
set -euo pipefail

MALL_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$MALL_HOME"
mkdir -p downloads .tools

download() {
  local url="$1" target="$2"
  if [[ -s "$target" ]] && file "$target" | grep -q "gzip compressed"; then
    echo "  exists: $target"
    return
  fi
  echo "  downloading $target"
  curl -L --fail --retry 5 --retry-delay 3 -o "$target" "$url"
}

echo "==> JDK 17 (Temurin)"
download "https://api.adoptium.net/v3/binary/latest/17/ga/mac/x64/jdk/hotspot/normal/eclipse" downloads/jdk17.tar.gz
if [[ ! -d .tools/jdk-17 ]]; then
  tar -xzf downloads/jdk17.tar.gz -C .tools
  mv .tools/jdk-17* .tools/jdk-17
fi

echo "==> Maven 3.9.16"
download "https://mirrors.aliyun.com/apache/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz" downloads/maven.tar.gz
if [[ ! -d .tools/apache-maven-3.9.16 ]]; then
  tar -xzf downloads/maven.tar.gz -C .tools
fi

echo "==> Nacos 2.3.2"
download "https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.tar.gz" downloads/nacos.tar.gz
if [[ ! -d .tools/nacos ]]; then
  tar -xzf downloads/nacos.tar.gz -C .tools
fi

echo "==> Redis 7.2.5 (built from source)"
download "https://mirrors.huaweicloud.com/redis/redis-7.2.5.tar.gz" downloads/redis.tar.gz
if [[ ! -x .tools/redis-7.2.5/src/redis-server ]]; then
  tar -xzf downloads/redis.tar.gz -C .tools
  (cd .tools/redis-7.2.5 && make -j4 USE_SYSTEMD=no)
fi

echo "==> MySQL 8.0.28"
download "https://mirrors.aliyun.com/mysql/MySQL-8.0/mysql-8.0.28-macos11-x86_64.tar.gz" downloads/mysql.tar.gz
if [[ ! -d .tools/mysql ]]; then
  tar -xzf downloads/mysql.tar.gz -C .tools
  mv .tools/mysql-8.0.28-macos11-x86_64 .tools/mysql
  xattr -dr com.apple.quarantine .tools/mysql 2>/dev/null || true
fi

echo "==> Node.js 20.18.1 (for the Vue 3 frontend)"
download "https://npmmirror.com/mirrors/node/v20.18.1/node-v20.18.1-darwin-x64.tar.gz" downloads/node.tar.gz
if [[ ! -d .tools/node ]]; then
  tar -xzf downloads/node.tar.gz -C .tools
  mv .tools/node-v20.18.1-darwin-x64 .tools/node
fi

echo "==> Toolchain ready in $MALL_HOME/.tools"
