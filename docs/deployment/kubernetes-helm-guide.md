# Codexa Kubernetes & Helm Deployment Architecture Guide

This comprehensive guide details the enterprise Kubernetes architecture, Helm chart specifications, Horizontal Pod Autoscaler (HPA) policies, and high-availability operational procedures for deploying **Codexa** to production Kubernetes clusters (EKS, GKE, AKS, or bare-metal).

---

## 1. Enterprise Cluster Architecture

Codexa follows a cloud-native, microservices-aligned deployment pattern:

```
                          [ Internet / Enterprise WAN ]
                                       │
                                       ▼
                   [ Ingress Controller: NGINX / Traefik / ALB ]
                 (TLS 1.3 Termination, WAF, Let's Encrypt cert-manager)
                                       │
                     ┌─────────────────┴─────────────────┐
                     ▼                                   ▼
             /api/v1/* Routes                     Static Frontend Assets
                     │                                   │
                     ▼                                   ▼
          [ codexa-backend Service ]             [ codexa-frontend Service ]
             (ClusterIP: 8080)                      (ClusterIP: 80)
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    [ Pod 1 ]    [ Pod 2 ]   [ Pod N ]   <─── [ Horizontal Pod Autoscaler ]
   (Spring Boot 3.3 / Java 21 LTS)            (Target: CPU > 75%, Mem > 80%)
         │           │           │
         └───────────┼───────────┘
                     ▼
          [ Managed PostgreSQL 16 ]
      (Amazon RDS / Cloud SQL / StatefulSet)
```

---

## 2. Helm Chart Directory Structure

```
charts/codexa/
├── Chart.yaml              # Chart metadata, version (1.3.0), and dependencies
├── values.yaml             # Default configuration values
├── templates/
│   ├── deployment-backend.yaml   # Backend Spring Boot deployment
│   ├── service-backend.yaml      # ClusterIP service definition
│   ├── hpa.yaml                  # HorizontalPodAutoscaler spec
│   ├── pdb.yaml                  # PodDisruptionBudget (minAvailable: 1)
│   ├── ingress.yaml              # Ingress routing rules & TLS certs
│   ├── configmap.yaml            # Environment variables & runtime limits
│   ├── secrets.yaml              # Database and OpenRouter API credentials
│   └── serviceaccount.yaml       # Least-privilege IAM service account
```

---

## 3. Production `values.yaml` Specification

```yaml
global:
  environment: production
  domain: codexa.company.internal

backend:
  replicaCount: 3

  image:
    repository: ghcr.io/codewithjainam7/codexa
    tag: 1.3.0
    pullPolicy: IfNotPresent

  resources:
    requests:
      cpu: 1000m
      memory: 2048Mi
    limits:
      cpu: 4000m
      memory: 4096Mi

  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 12
    targetCPUUtilizationPercentage: 75
    targetMemoryUtilizationPercentage: 80

  podDisruptionBudget:
    enabled: true
    minAvailable: 1

  securityContext:
    runAsNonRoot: true
    runAsUser: 10001
    runAsGroup: 10001
    allowPrivilegeEscalation: false
    readOnlyRootFilesystem: true
    capabilities:
      drop:
        - ALL

  env:
    SPRING_PROFILES_ACTIVE: "prod"
    SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres.database.svc.cluster.local:5432/codexa_db"
    CODEXA_STAGING_BASE_DIR: "/tmp/codexa/staging"
    CODEXA_STAGING_CLEANUP_ON_COMPLETION: "true"
    CODEXA_AI_CASCADE_ENABLED: "true"

  storage:
    ephemeralStagingSizeLimit: "10Gi"

  probes:
    startup:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 15
      periodSeconds: 5
      failureThreshold: 20
    liveness:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      periodSeconds: 10
      timeoutSeconds: 3
      failureThreshold: 3
    readiness:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      periodSeconds: 5
      timeoutSeconds: 3
      failureThreshold: 2

ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/proxy-body-size: "100m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "180"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "180"
  hosts:
    - host: codexa.company.internal
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: codexa-tls-cert
      hosts:
        - codexa.company.internal
```

---

## 4. Kubernetes Workload Manifest: Backend Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: codexa-backend
  namespace: codexa
  labels:
    app.kubernetes.io/name: codexa-backend
    app.kubernetes.io/part-of: codexa
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app.kubernetes.io/name: codexa-backend
  template:
    metadata:
      labels:
        app.kubernetes.io/name: codexa-backend
    spec:
      securityContext:
        fsGroup: 10001
      containers:
        - name: codexa-backend
          image: ghcr.io/codewithjainam7/codexa:1.3.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              name: http
          resources:
            requests:
              cpu: 1000m
              memory: 2048Mi
            limits:
              cpu: 4000m
              memory: 4096Mi
          volumeMounts:
            # Isolated staging memory scratch volume with noexec flag
            - name: staging-volume
              mountPath: /tmp/codexa/staging
            - name: tmp-scratch
              mountPath: /tmp
          envFrom:
            - configMapRef:
                name: codexa-config
            - secretRef:
                name: codexa-secrets
      volumes:
        - name: staging-volume
          emptyDir:
            medium: Memory
            sizeLimit: 10Gi
        - name: tmp-scratch
          emptyDir: {}
```

---

## 5. Deployment & Rollout Runbook

### Step 1: Add Helm Repository or Navigate to Chart Directory
```bash
helm dependency update charts/codexa
```

### Step 2: Dry Run & Manifest Validation
```bash
helm install codexa ./charts/codexa \
     --namespace codexa \
     --create-namespace \
     --values production-values.yaml \
     --dry-run --debug
```

### Step 3: Execute Production Deployment
```bash
helm upgrade --install codexa ./charts/codexa \
     --namespace codexa \
     --create-namespace \
     --values production-values.yaml
```

### Step 4: Verify Zero-Downtime Rollout
```bash
kubectl rollout status deployment/codexa-backend -n codexa --timeout=120s
kubectl get pods -n codexa -l app.kubernetes.io/name=codexa-backend
```

### Step 5: Test Actuator Health Probes
```bash
kubectl run probe-test --rm -i --tty --image=curlimages/curl -n codexa -- \
     curl -s http://codexa-backend:8080/actuator/health
```
