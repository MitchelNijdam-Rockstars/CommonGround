#!/usr/bin/env bash
set -euo pipefail

COMPOSE="docker compose -f docker-compose.yaml -f docker-compose.sandbox.yml"

echo "Starting the database..."
docker compose up -d db

echo "Building sandbox image (first run will take a few minutes)..."
$COMPOSE build claude-sandbox

# Check if the sandbox already has a stored session
if ! docker volume inspect commonground_claude-config &>/dev/null || \
   ! docker run --rm -v commonground_claude-config:/root/.claude \
       busybox test -f /root/.claude/credentials.json 2>/dev/null; then
  echo ""
  echo "No sandbox session found. Logging in..."
  echo "A browser URL will appear — open it on this machine to authenticate."
  echo ""
  $COMPOSE run --rm claude-sandbox claude login
fi

echo "Launching Claude Code sandbox..."
$COMPOSE run --rm claude-sandbox
