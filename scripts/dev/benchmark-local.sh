#!/usr/bin/env bash
set -euo pipefail
echo "Running Codexa local benchmark..."
curl -s -X POST http://localhost:8080/api/v1/analysis/benchmark
