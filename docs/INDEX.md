# Codexa Comprehensive Documentation Index

Welcome to the definitive documentation portal for **Codexa: Deterministic AST Code Review & Production Readiness Platform**.

---

## 1. Empirical Performance & Accuracy Benchmarks
* [Codexa Engine Performance Targets & Empirical SLAs](benchmarks/performance-targets.md)
* [Accuracy, Precision & False Positive Benchmark Report](benchmarks/accuracy-and-false-positives.md)
* [Enterprise Monorepo Scaling Benchmarks & Delta Analysis](benchmarks/monorepo-scaling-guide.md)
* [AST Rule Engine Micro-Benchmarks & Latency Catalog](benchmarks/ast-rule-engine-throughput.md)
* [SQLite WAL Concurrency, IOPS & Transaction Benchmarks](benchmarks/sqlite-wal-concurrency-benchmarks.md)
* [Frontend Performance, Core Web Vitals & Lighthouse Metrics](benchmarks/frontend-render-benchmarks.md)

---

## 2. Features & User Experience
* [Security Playground & Code Sandbox](features/security-playground.md)
* [Interactive Visual SBOM & Blast-Radius Graph](features/sbom-blast-radius-graph.md)
* [Attack Surface Threat Map & Architectural Perimeter](features/attack-surface-threat-map.md)
* [Executive Dynamic Vector Trust Badges](features/executive-trust-badges.md)

---

## 3. Developer Extensions & Plugins
* [Chrome & Chromium Browser Extension](integrations/chrome-extension.md)
* [Visual Studio Code Extension](integrations/vscode-extension.md)
* [JetBrains IntelliJ IDEA Plugin](integrations/intellij-plugin.md)

---

## 4. Security Architecture & Ingress Defense
* [OAuth2 & OpenID Connect (OIDC) Enterprise Hardening](security/oauth2-oidc-hardening.md)
* [JSON Web Token (JWT) Defense-in-Depth Specification](security/jwt-security-best-practices.md)
* [Cross-Origin Resource Sharing (CORS) Security Architecture](security/cors-policy-architecture.md)
* [Cross-Site Request Forgery (CSRF) Modern SPA Defense](security/csrf-protection-guide.md)
* [Rate Limiting & Denial-of-Service Defense Architecture](security/rate-limiting-algorithms.md)
* [Content Security Policy (CSP) Level 3 Implementation](security/content-security-policy.md)

---

## 5. Rules Catalog & Standards
### Infrastructure-as-Code (IaC)
* [CR-IAC-001: Dockerfile Root Execution](rules/CR-IAC-001.md)
* [CR-IAC-002: Dockerfile Untagged Base Image (:latest)](rules/CR-IAC-002.md)
* [CR-IAC-003: Kubernetes Privileged Container Escalation](rules/CR-IAC-003.md)
* [CR-IAC-005: Terraform Unrestricted Ingress (0.0.0.0/0)](rules/CR-IAC-005.md)
* [CR-IAC-006: Terraform Unencrypted Storage Volumes](rules/CR-IAC-006.md)

### OWASP API Security Top 10 (2023)
* [CR-API-001: Mass Assignment & BOPLA](rules/CR-API-001.md)
* [CR-API-002: Unrestricted Resource Consumption & Pagination](rules/CR-API-002.md)
* [CR-API-003: Broken Function Level Authorization (BFLA)](rules/CR-API-003.md)

### OWASP Top 10 for Large Language Models (LLMs)
* [CR-LLM-001: Direct Prompt Injection](rules/CR-LLM-001.md)
* [CR-LLM-002: Insecure Output Handling & Sinks](rules/CR-LLM-002.md)

### Open-Source Legal Compliance
* [CR-LIC-001: Viral Copyleft License Detection (GPL/AGPL)](rules/CR-LIC-001.md)

---

## 6. Enterprise Regulatory Compliance
* [SOC 2 Type II Audit Readiness Guide](compliance/soc2-type2-audit-guide.md)
* [ISO/IEC 27001:2022 Annex A Controls Mapping](compliance/iso27001-annex-a.md)
* [PCI-DSS v4.0 Secure Software Lifecycle Mapping](compliance/pci-dss-v4.md)
* [FedRAMP Moderate Security Baseline Technical Control Mapping](compliance/fedramp-moderate-mapping.md)
* [HIPAA Security Rule Technical Safeguards Implementation Guide](compliance/hipaa-technical-safeguards.md)
* [2024 CWE Top 25 Most Dangerous Software Weaknesses](compliance/cwe-top-25-coverage.md)
* [Comprehensive Enterprise Compliance Matrix](compliance/compliance-matrix.md)

---

## 7. Architecture & Internal Systems
* [Deterministic AST Parser Engine & Rule Lifecycle](architecture/ast-parser-engine.md)
* [Multi-Engine Threat Modeling & Surface Discovery](architecture/multi-engine-threat-modeling.md)
* [Production Readiness Scoring Formula](architecture/scoring-formula.md)
* [Production Readiness Scoring Formula Calibration](architecture/score-weighting-calibration.md)
* [SQLite Write-Ahead Logging (WAL) Concurrency Tuning](architecture/sqlite-wal-concurrency.md)
* [Java 21 Virtual Threads & Fiber Concurrency](architecture/virtual-threads-deep-dive.md)
* [OpenRouter Multi-LLM Resilient Fallback Routing](architecture/openrouter-ai-fallback.md)
* [SARIF v2.1.0 Specification & GitHub Code Scanning](architecture/sarif-v21-specification.md)
* [Server-Sent Events (SSE) Live Review Streaming Engine](architecture/streaming-sse-architecture.md)
* [Content-Addressable Storage (CAS) & AST Hash Caching](architecture/content-addressable-storage.md)
* [Spring Boot 3 Bean Lifecycle & Dependency Injection](architecture/dependency-injection-framework.md)
* [Multi-Stage Ingestion & Analysis Orchestrator](architecture/pipeline-orchestrator.md)
* [SBOM & CVE Dependency Scanner](architecture/sbom-cve-scanner.md)

---

## 8. Operations, Cloud Deployment & Observability
* [Render Production Operations Runbook](operations/render-production-runbook.md)
* [Multi-Container Docker Compose Production Guide](operations/docker-compose-production.md)
* [Kubernetes GitOps Continuous Delivery with ArgoCD](operations/kubernetes-gitops-argo.md)
* [Prometheus Metrics Catalog & Micrometer Instrumentation](operations/prometheus-metrics-catalog.md)
* [Grafana Observability Dashboards & Alert Rules](operations/grafana-dashboard-templates.md)
* [Centralized Log Aggregation with Promtail & Loki](operations/log-aggregation-loki.md)
* [Business Continuity & Disaster Recovery (BCDR) Runbook](operations/disaster-recovery-plan.md)
* [Docker Container Setup Guide](deployment/docker-setup.md)
* [Production Deployment Checklist](deployment/production-checklist.md)
