# GitHub Code Scanning & SARIF Integration

Codexa generates OASIS SARIF v2.1.0 reports for native ingestion into GitHub Security Alerts.

## Exporting SARIF from Codexa
```bash
curl -X GET "http://localhost:8080/api/v1/analyses/{jobId}/report?format=sarif" \
     -H "Accept: application/json" \
     -o codexa-results.sarif
```

## GitHub Actions Workflow Example
```yaml
name: "Codexa Security Scan"
on: [push, pull_request]

jobs:
  analyze:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Codexa CLI / API
        run: |
          curl -F "file=@project.zip" http://codexa.local/api/v1/analyses/zip > job.json
          JOB_ID=$(jq -r .jobId job.json)
          curl "http://codexa.local/api/v1/analyses/$JOB_ID/report?format=sarif" > codexa.sarif
      - name: Upload SARIF to GitHub Code Scanning
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: codexa.sarif
```
