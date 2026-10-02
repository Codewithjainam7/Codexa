# Kubernetes GitOps Continuous Delivery with ArgoCD

## 1. Overview
Codexa can be continuously deployed to enterprise Kubernetes clusters using GitOps workflows powered by **ArgoCD**.

---

## 2. ArgoCD Application Manifest

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: codexa-production
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/Codewithjainam7/Codexa.git
    targetRevision: main
    path: k8s/overlays/production
  destination:
    server: https://kubernetes.default.svc
    namespace: codexa
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```
