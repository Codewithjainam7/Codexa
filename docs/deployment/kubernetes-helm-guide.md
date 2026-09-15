# Codexa Kubernetes Helm Deployment Architecture

## Helm Deployment Architecture

```
[ Ingress Controller (Nginx / Traefik) ]
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
[ Backend Pod 1 ]   [ Backend Pod 2 ] (HPA: 2-10 replicas)
        │                   │
        └─────────┬─────────┘
                  ▼
      [ PostgreSQL StatefulSet / RDS ]
```

## Sample `values.yaml`

```yaml
replicaCount: 2

image:
  repository: ghcr.io/codewithjainam7/codexa
  tag: latest
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 2000m
    memory: 4096Mi
  requests:
    cpu: 500m
    memory: 1024Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 75

persistence:
  enabled: true
  size: 20Gi
  storageClass: "gp3"
```
