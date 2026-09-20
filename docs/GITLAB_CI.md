# GitLab CI SAST Integration Guide

Integrate Codexa AST security findings and production readiness scores directly into GitLab's native Security & Compliance dashboard via SARIF v2.1.0.

---

## 1. Pipeline Configuration: `.gitlab-ci.yml`

```yaml
stages:
  - test
  - security

codexa_sast:
  stage: security
  image: alpine:latest
  before_script:
    - apk add --no-cache curl jq zip
  script:
    - echo "Packaging codebase for Codexa audit..."
    - zip -r /tmp/codebase.zip . -x "*.git*" "node_modules/*" "target/*" "dist/*"
    - |
      echo "Submitting archive to Codexa..."
      SUBMIT_RESP=$(curl -s -f -X POST "${CODEXA_API_URL}/api/v1/analyses/zip" \
        -F "file=@/tmp/codebase.zip")
      JOB_ID=$(echo "${SUBMIT_RESP}" | jq -r '.id')
      echo "Started analysis job: ${JOB_ID}"

    - |
      echo "Polling analysis job status..."
      while true; do
        JOB_DATA=$(curl -s "${CODEXA_API_URL}/api/v1/analyses/${JOB_ID}")
        STATUS=$(echo "${JOB_DATA}" | jq -r '.status')
        PERCENT=$(echo "${JOB_DATA}" | jq -r '.progressPercent')
        echo "Status: ${STATUS} (${PERCENT}%)..."
        
        if [ "${STATUS}" = "COMPLETED" ]; then
          SCORE=$(echo "${JOB_DATA}" | jq -r '.overallScore')
          VERDICT=$(echo "${JOB_DATA}" | jq -r '.verdict')
          echo "Audit complete! Score: ${SCORE} | Verdict: ${VERDICT}"
          break
        elif [ "${STATUS}" = "FAILED" ]; then
          echo "Codexa scan failed."
          exit 1
        fi
        sleep 3
      done

    - |
      echo "Downloading SARIF report for GitLab Security Dashboard..."
      curl -s "${CODEXA_API_URL}/api/v1/analyses/${JOB_ID}/reports/sarif" \
        -o gl-sast-report.sarif

  artifacts:
    reports:
      sast: gl-sast-report.sarif
    paths:
      - gl-sast-report.sarif
    expire_in: 30 days
```
