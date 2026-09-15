# Codexa Google Cloud Run Deployment Specification

## Overview
Google Cloud Run enables deploying Codexa as an auto-scaling container that scales to zero when idle and scales up rapidly under analysis bursts.

## Deployment Command

```bash
gcloud run deploy codexa-app \
    --image gcr.io/my-project/codexa:latest \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --cpu 2 \
    --memory 4Gi \
    --concurrency 80 \
    --timeout 300s \
    --set-env-vars SPRING_PROFILES_ACTIVE=cloud,CODEXA_OFFLINE_MODE=true
```

## Cloud Run Considerations
- **Execution Timeout**: Max request timeout set to 300 seconds to accommodate large repository scans.
- **Concurrency**: Set to 80 requests per container instance to maximize Java 21 Virtual Thread efficiency.
