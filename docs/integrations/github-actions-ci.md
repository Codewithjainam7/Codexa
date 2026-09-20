# GitHub Actions CI/CD Integration & PR Quality Gate

Integrate Codexa into your GitHub CI/CD pipeline to automatically audit Pull Requests, fail builds on security violations, and upload findings to GitHub Code Scanning via SARIF v2.1.0.

---

## 1. Complete Pull Request Audit Workflow

Create `.github/workflows/codexa-audit.yml`:

```yaml
name: Codexa Security & Quality Gate

on:
  pull_request:
    branches: [ main, master ]
  push:
    branches: [ main ]

permissions:
  contents: read
  security-events: write
  pull-requests: write

jobs:
  codexa-audit:
    name: Codexa Static Analysis
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Package Source Code
        run: |
          zip -r codebase.zip . -x "*.git*" "*node_modules*" "*target*" "*dist*"

      - name: Submit to Codexa Platform
        id: submit
        run: |
          RESPONSE=$(curl -s -f -X POST "${{ secrets.CODEXA_API_URL }}/api/v1/analyses/zip" \
            -F "file=@codebase.zip")
          JOB_ID=$(echo "$RESPONSE" | jq -r '.id')
          echo "job_id=$JOB_ID" >> "$GITHUB_OUTPUT"
          echo "Started Codexa audit: $JOB_ID"

      - name: Poll Analysis Completion
        id: poll
        run: |
          JOB_ID="${{ steps.submit.outputs.job_id }}"
          while true; do
            RESULT=$(curl -s "${{ secrets.CODEXA_API_URL }}/api/v1/analyses/${JOB_ID}")
            STATUS=$(echo "$RESULT" | jq -r '.status')
            PERCENT=$(echo "$RESULT" | jq -r '.progressPercent')
            echo "Scan in progress: $STATUS ($PERCENT%)..."
            
            if [ "$STATUS" = "COMPLETED" ]; then
              SCORE=$(echo "$RESULT" | jq -r '.overallScore')
              VERDICT=$(echo "$RESULT" | jq -r '.verdict')
              CRITICALS=$(echo "$RESULT" | jq -r '.metrics.criticalCount')
              
              echo "score=$SCORE" >> "$GITHUB_OUTPUT"
              echo "verdict=$VERDICT" >> "$GITHUB_OUTPUT"
              echo "criticals=$CRITICALS" >> "$GITHUB_OUTPUT"
              break
            elif [ "$STATUS" = "FAILED" ]; then
              echo "Codexa audit job failed!"
              exit 1
            fi
            sleep 3
          done

      - name: Download SARIF Report
        run: |
          curl -s "${{ secrets.CODEXA_API_URL }}/api/v1/analyses/${{ steps.submit.outputs.job_id }}/reports/sarif" \
            -o codexa-results.sarif

      - name: Upload to GitHub Code Scanning
        uses: github/codeql-action/upload-sarif@v3
        if: always()
        with:
          sarif_file: codexa-results.sarif

      - name: Enforce Production Gate
        run: |
          VERDICT="${{ steps.poll.outputs.verdict }}"
          CRITICALS="${{ steps.poll.outputs.criticals }}"
          SCORE="${{ steps.poll.outputs.score }}"
          
          echo "========================================="
          echo "Codexa Readiness Score: ${SCORE}/100"
          echo "Verdict: ${VERDICT}"
          echo "Critical Vulnerabilities: ${CRITICALS}"
          echo "========================================="
          
          if [ "$VERDICT" = "NOT_READY" ] || [ "$CRITICALS" -gt 0 ]; then
            echo "❌ PR Gate Failed: Critical security defects or NOT_READY score detected."
            exit 1
          fi
          echo "✅ PR Gate Passed."
```
