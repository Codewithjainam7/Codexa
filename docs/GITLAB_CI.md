# GitLab CI SAST Integration Guide

Integrate Codexa AST security findings into GitLab's native Security & Compliance dashboard.

## Pipeline Configuration: `.gitlab-ci.yml`

```yaml
stages:
  - test
  - security

codexa_sast:
  stage: security
  image: curlimages/curl:latest
  script:
    - echo "Submitting repository for Codexa AST inspection..."
    - |
      JOB_RESPONSE=$(curl -s -X POST "${CODEXA_API_URL}/api/v1/analyze/repo"         -H "Content-Type: application/json"         -d "{"repoUrl": "${CI_REPOSITORY_URL}", "branch": "${CI_COMMIT_REF_NAME}"}")
      JOB_ID=$(echo $JOB_RESPONSE | grep -o '"jobId":"[^"]*' | cut -d'"' -f4)
    - |
      until curl -s "${CODEXA_API_URL}/api/v1/jobs/${JOB_ID}" | grep -q '"status":"COMPLETED"'; do
        sleep 2
      done
    - curl -s "${CODEXA_API_URL}/api/v1/jobs/${JOB_ID}/export?format=sarif" -o gl-sast-report.sarif
  artifacts:
    reports:
      sast: gl-sast-report.sarif
```
