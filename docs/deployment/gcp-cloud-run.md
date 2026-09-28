# Codexa Google Cloud Run Serverless Deployment Guide

This guide details the architecture, resource tuning, Cloud SQL connectivity, and Secret Manager integration for deploying **Codexa** on **Google Cloud Run** (fully managed serverless container platform).

---

## 1. Cloud Architecture Topology

```
                              [ HTTPS Clients ]
                                      │
                                      ▼
                        [ Cloud Load Balancing ]
                    (Managed SSL, Cloud Armor DDoS/WAF)
                                      │
                                      ▼
                   [ Cloud Run Service: codexa-backend ]
                     (2 vCPU / 4 GiB RAM, Autoscaling)
                                      │
             ┌────────────────────────┴────────────────────────┐
             ▼                                                 ▼
[ Serverless VPC Access Connector ]                  [ GCP Secret Manager ]
             │                                        (OpenRouter API Key,
             ▼                                         DB Master Password)
   [ Cloud SQL: PostgreSQL 16 ]
 (Private IP / Cloud SQL Auth Proxy)
```

### Key Advantages:
- **Serverless Autoscaling**: Scales from 1 minimum instance up to 50 concurrent instances during peak PR gating hours.
- **Pay-Per-Execution Billing**: Sub-millisecond CPU allocation minimizes operational costs during idle development periods.
- **Integrated Zero-Trust**: Direct integration with Google Cloud IAM and Workload Identity.

---

## 2. Declarative Service Specification (`service.yaml`)

```yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: codexa-backend
  namespace: 'default'
  labels:
    cloud.googleapis.com/location: us-central1
  annotations:
    run.googleapis.com/ingress: all
spec:
  template:
    metadata:
      annotations:
        run.googleapis.com/cpu-throttling: 'false' # Keep CPU allocated for background async pipeline tasks
        run.googleapis.com/startup-cpu-boost: 'true'
        run.googleapis.com/cloudsql-instances: my-project:us-central1:codexa-postgres
        run.googleapis.com/vpc-access-connector: projects/my-project/locations/us-central1/connectors/codexa-vpc-connector
        autoscaling.knative.dev/minScale: '1'
        autoscaling.knative.dev/maxScale: '20'
    spec:
      containerConcurrency: 80
      timeoutSeconds: 300
      serviceAccountName: codexa-cloud-run-sa@my-project.iam.gserviceaccount.com
      containers:
        - image: gcr.io/my-project/codexa:1.3.0
          ports:
            - containerPort: 8080
          resources:
            limits:
              cpu: '2000m'
              memory: '4Gi'
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: 'prod'
            - name: CODEXA_STAGING_BASE_DIR
              value: '/tmp/codexa/staging'
            - name: CODEXA_STAGING_CLEANUP_ON_COMPLETION
              value: 'true'
            - name: SPRING_DATASOURCE_URL
              value: 'jdbc:postgresql:///codexa_db?cloudSqlInstance=my-project:us-central1:codexa-postgres&socketFactory=com.google.cloud.sql.postgres.SocketFactory'
            - name: SPRING_DATASOURCE_USERNAME
              value: 'codexa_user'
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  key: latest
                  name: CODEXA_DB_PASSWORD
            - name: OPENROUTER_API_KEY
              valueFrom:
                secretKeyRef:
                  key: latest
                  name: OPENROUTER_API_KEY
          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            periodSeconds: 15
```

---

## 3. Deployment CLI Commands

### Step 1: Create IAM Service Account & Grant Permissions
```bash
gcloud iam service-accounts create codexa-cloud-run-sa \
    --display-name="Codexa Cloud Run Service Account"

# Grant Secret Manager Access
gcloud projects add-iam-policy-binding my-project \
    --member="serviceAccount:codexa-cloud-run-sa@my-project.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"

# Grant Cloud SQL Client Access
gcloud projects add-iam-policy-binding my-project \
    --member="serviceAccount:codexa-cloud-run-sa@my-project.iam.gserviceaccount.com" \
    --role="roles/cloudsql.client"
```

### Step 2: Deploy to Cloud Run via Declarative YAML
```bash
gcloud run services replace service.yaml --region us-central1
```

### Step 3: Verify Traffic & Endpoint Health
```bash
ENDPOINT_URL=$(gcloud run services describe codexa-backend \
    --platform managed --region us-central1 --format 'value(status.url)')

curl -s "${ENDPOINT_URL}/actuator/health"
```

---

## 4. Key Performance Tuning Parameters

1. **`cpu-throttling: false`**: Required because Codexa executes static AST evaluation and AI remediation asynchronously in background Spring `@Async` virtual threads after the HTTP upload response is returned. Disabling CPU throttling guarantees full CPU allocation between request cycles.
2. **`containerConcurrency: 80`**: Leverages Java 21 Loom Virtual Threads, allowing a single 2 vCPU container to process up to 80 concurrent finding requests without thread pool saturation.
3. **`timeoutSeconds: 300`**: Grants a generous 5-minute deadline for large multi-thousand file repository ingestion and deep AST graph traversal.
