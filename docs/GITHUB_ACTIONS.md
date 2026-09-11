# GitHub Actions CI/CD Integration Guide

Integrate Codexa deterministic AST scanning directly into GitHub pull request workflows.

## Workflow Example: `.github/workflows/codexa-scan.yml`

```yaml
name: Codexa Security & Production Readiness Audit

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  codexa-audit:
    name: AST Security & Quality Inspection
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Run Codexa Scan CLI
        run: |
          # Trigger scan against local repository
          JOB_ID=$(curl -s -X POST http://localhost:8080/api/v1/analyze/repo \
            -H "Content-Type: application/json" \
            -d "{\"repoUrl\": \"${{ github.server_url }}/${{ github.repository }}.git\", \"branch\": \"${{ github.ref_name }}\"}" | jq -r '.jobId')
          
          echo "Started Codexa Job: $JOB_ID"
          
          # Poll until completion
          while true; do
            STATUS=$(curl -s http://localhost:8080/api/v1/jobs/$JOB_ID | jq -r '.status')
            if [ "$STATUS" = "COMPLETED" ]; then
              break
            elif [ "$STATUS" = "FAILED" ]; then
              echo "Scan failed!"
              exit 1
            fi
            sleep 2
          done

          # Download SARIF v2.1.0 report
          curl -s "http://localhost:8080/api/v1/jobs/$JOB_ID/export?format=sarif" -o codexa.sarif

      - name: Upload SARIF to GitHub Code Scanning
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: codexa.sarif
          category: codexa-ast
```
