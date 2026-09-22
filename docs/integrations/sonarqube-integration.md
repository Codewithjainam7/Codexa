# Codexa SonarQube & SonarCloud Integration Guide

This guide details how to import Codexa's standard **SARIF v2.1.0** static analysis results directly into SonarQube and SonarCloud enterprise dashboards.

---

## 1. Integration Architecture

Because Codexa produces standard OASIS SARIF v2.1.0 output mapped to CWE and OWASP Top 10 categories, SonarQube can ingest findings directly into its Quality Gates without requiring custom Java plugins:

```
[ Codexa CLI / API Analysis ] ===> [ codexa-report.sarif ] ===> [ SonarScanner CLI ] ===> [ SonarQube Server ]
```

---

## 2. Ingesting SARIF into SonarScanner

In your `sonar-project.properties` or build pipeline:
```properties
# Point SonarQube to the SARIF output exported by Codexa
sonar.sarifReportPaths=target/codexa-report.sarif
```

### GitHub Actions Workflow Example:
```yaml
name: Security Audit & SonarQube Ingestion
on: [push, pull_request]

jobs:
  codexa-sonar:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run Codexa Scan
        run: |
          curl -s -X POST https://codexa-ye85.onrender.com/api/v1/analyses/github \
            -H "Content-Type: application/json" \
            -d "{\"repoUrl\": \"${{ github.server_url }}/${{ github.repository }}\"}" > job.json
          # Wait and export SARIF
          curl -s https://codexa-ye85.onrender.com/api/v1/analyses/${JOB_ID}/sarif > codexa-report.sarif

      - name: SonarQube Scan
        uses: SonarSource/sonarqube-scan-action@v2
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        with:
          args: >
            -Dsonar.sarifReportPaths=codexa-report.sarif
```
