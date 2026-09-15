# Codexa Master Documentation Index

Welcome to the comprehensive technical documentation for **Codexa**—the AI-augmented, multi-language code review and security analysis platform.

## Documentation Catalog

### 1. Getting Started & API Reference
- [Architecture Overview](architecture/pipeline-stages.md)
- [OpenAPI Specification](api/openapi-specs.md)
- [Rate Limiting Guide](api/rate-limiting-guide.md)
- [Pagination Specifications](api/pagination-specs.md)
- [Batch Analysis API](api/batch-analysis-api.md)
- [Server-Sent Events Streaming](api/streaming-events-sse.md)
- [Authentication Tokens & Lifecycle](api/authentication-tokens.md)
- [API Versioning Policy](api/versioning-policy.md)
- [Multi-Language SDK Quickstart](api/sdk-quickstart.md)
- [GraphQL Draft Schema](api/graphql-schema-draft.md)

### 2. Architecture & Engine Internals
- [Multi-Language AST Engine](architecture/multi-language-engine.md)
- [Java 21 Virtual Threads Concurrency](architecture/concurrency-model.md)
- [JVM Memory & Heap Sizing](architecture/memory-management.md)
- [Multi-Tier Caching & AST Fingerprints](architecture/caching-strategy.md)
- [Custom Rule Provider Plugins](architecture/plugin-architecture.md)
- [SARIF v2.1.0 Schema Mapping](architecture/sarif-v2-mapping.md)
- [Incremental Delta Analysis](architecture/delta-analysis.md)
- [AI Provider Resilience & Fallbacks](architecture/resilience-patterns.md)
- [Data Retention & Archival Policies](architecture/data-retention-policy.md)

### 3. Security, Hardening & Compliance
- [Zero-Trust Code Ingestion](security/zero-trust-architecture.md)
- [Process Sandboxing & Seccomp](security/sandboxing-mechanisms.md)
- [CWE Top 25 Coverage Matrix](security/cwe-top25-mapping.md)
- [Cryptographic Baselines](security/cryptographic-standards.md)
- [Incident Response Runbook](security/incident-response-runbook.md)
- [RFC 5424 Audit Logging](security/audit-logging-standards.md)
- [Supply Chain & SLSA Security](security/supply-chain-security.md)
- [GDPR & Data Privacy](security/gdpr-compliance-guide.md)

### 4. Operations, Deployment & DevOps
- [Kubernetes Helm Deployment](deployment/kubernetes-helm-guide.md)
- [AWS ECS Fargate](deployment/aws-ecs-fargate.md)
- [Google Cloud Run](deployment/gcp-cloud-run.md)
- [Nginx Reverse Proxy](deployment/reverse-proxy-nginx.md)
- [Prometheus & Grafana Monitoring](deployment/monitoring-prometheus-grafana.md)
- [Backup & Disaster Recovery Runbook](deployment/backup-restore-runbook.md)
- [High Availability Clustering](deployment/high-availability-guide.md)
- [Linux Systemd Service](deployment/systemd-service-setup.md)

### 5. Rule Catalog
- [CR-SEC-001: SQL Injection](rules/CR-SEC-001.md)
- [CR-SEC-002: Hardcoded Secrets](rules/CR-SEC-002.md)
- [CR-SEC-007: Insecure Deserialization](rules/CR-SEC-007.md)
- [CR-SEC-008: Hardcoded Crypto Keys](rules/CR-SEC-008.md)
- [CR-SEC-009: Broken Object Authorization (BOLA)](rules/CR-SEC-009.md)
- [CR-PERF-001: N+1 Query Anti-Pattern](rules/CR-PERF-001.md)
- [CR-PERF-002: Resource Leak / Unclosed Stream](rules/CR-PERF-002.md)
- [CR-QUAL-003: High Cyclomatic Complexity](rules/CR-QUAL-003.md)
- [CR-QUAL-004: Swallowed Exceptions](rules/CR-QUAL-004.md)

### 6. Troubleshooting & Contributing
- [JVM Heap Dump & OOM Guide](troubleshooting/oom-heap-dumps.md)
- [SQLite Lock & Corruption Recovery](troubleshooting/corrupt-sqlite-recovery.md)
- [Backend Testing Conventions](contributing/unit-testing-standards.md)
- [Frontend UI Architecture Guidelines](contributing/frontend-component-guidelines.md)
- [Release Process Checklist](contributing/release-process-checklist.md)
