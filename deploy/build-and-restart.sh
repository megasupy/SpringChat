#!/usr/bin/env bash
# Same steps as .github/workflows/deploy.yml — run manually on the server after cloning.
#
#   chmod +x deploy/build-and-restart.sh
#   ./deploy/build-and-restart.sh          # uses branch main
#   ./deploy/build-and-restart.sh master
#   SPRINGCHAT_ROOT=/path/to/clone ./deploy/build-and-restart.sh main

set -euo pipefail

ROOT="${SPRINGCHAT_ROOT:-/home/azureuser/SpringChat}"
BRANCH="${1:-main}"

cd "$ROOT"
git fetch origin
git checkout "$BRANCH"
git pull --ff-only "origin/${BRANCH}"
mvn -B -DskipTests package
sudo systemctl restart springchat
