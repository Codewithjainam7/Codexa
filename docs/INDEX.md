# Codexa Master Documentation Index 📚

Welcome to the comprehensive technical documentation for **Codexa**—the AI-augmented, multi-language code review, static security auditing, and production readiness platform.

---

## 🌟 Core Overviews & Executive Summaries
- **[Technical Capabilities & Feature Reference](CAPABILITIES.md)**: Exhaustive breakdown of ingestion, AST parsing, polyglot rules, attack surface discovery, scoring, and remediation.
- **[Key Achievements, Milestones & Case Studies](ACHIEVEMENTS.md)**: Real-world production audits (SmartLot case study), 114 passing tests, 3GB benchmarks, and engineering milestones.
- **[Static Analysis & Security Rule Catalog](RULES_CATALOG.md)**: Detailed specifications for all 30+ rules mapped to OWASP Top 10 and CWE.
- **[Scale & Performance Benchmarks](BENCHMARKS.md)**: Empirical throughput benchmarks, buffer scaling, AST concurrency tests, and heap profiles.
- **[Changelog & Version History](CHANGELOG.md)**: Comprehensive release history across v1.2.0, v1.1.0, and v1.0.0.

---

## 🏗️ 1. Architecture & Engine Internals
- [High-Level Architecture & Pipeline Stages](architecture/pipeline-stages.md)
- [Multi-Language AST Engine](architecture/multi-language-engine.md)
- [Scoring Mathematics & Penalty Formulas](architecture/scoring-formula.md)
- [Java Concurrency & Managed ForkJoinPool](architecture/concurrency-model.md)
- [JVM Memory & Heap Sizing Guidelines](architecture/memory-management.md)
- [Multi-Tier Caching & AST Fingerprints](architecture/caching-strategy.md)
- [Custom Rule Provider Plugin Architecture](architecture/plugin-architecture.md)
- [SARIF v2.1.0 Schema Mapping](architecture/sarif-v2-mapping.md)
- [AI Remediation & Offline Fallback Routing](architecture/ai-fallback-routing.md)
- [Incremental Delta Analysis](architecture/delta-analysis.md)
- [Data Retention & Staging Cleanup Policies](architecture/data-retention-policy.md)

---

## 🔒 2. Security, Hardening & Compliance
- [Zero-Trust Code Ingestion Architecture](security/zero-trust-architecture.md)
- [Zip Slip Traversal Protection](security/zip-slip-protection.md)
- [SSRF Defense & IP Whitelisting Architecture](security/ssrf-defense.md)
- [In-Flight Secret Masking & Redaction](security/secret-masking-specs.md)
- [Threat Model & Security Boundary Analysis](security/threat-model.md)
- [CWE Top 25 Coverage Matrix](security/cwe-top25-mapping.md)
- [Cryptographic Baselines & Standards](security/cryptographic-standards.md)
- [Incident Response Runbook](security/incident-response-runbook.md)
- [RFC 5424 Audit Logging Standards](security/audit-logging-standards.md)
- [Supply Chain & SLSA Security](security/supply-chain-security.md)
- [GDPR & Data Privacy Guide](security/gdpr-compliance-guide.md)

---

## 📡 3. API Reference & Integration Guides
- [OpenAPI 3.0 Specifications](api/openapi-specs.md)
- [Rate Limiting & Token Bucket Algorithms](api/rate-limiting-guide.md)
- [Pagination & Sorting Specifications](api/pagination-specs.md)
- [Batch Analysis API](api/batch-analysis-api.md)
- [Server-Sent Events Streaming Guide](api/streaming-events-sse.md)
- [Authentication Tokens & API Key Lifecycle](api/authentication-tokens.md)
- [API Versioning Policy](api/versioning-policy.md)
- [Multi-Language SDK Quickstart](api/sdk-quickstart.md)
- [GitHub Actions Integration Guide](GITHUB_ACTIONS.md)
- [GitLab CI Pipeline Guide](GITLAB_CI.md)
- [SonarQube Integration Guide](SONARQUBE.md)
- [SARIF v2.1.0 Integration Guide](SARIF_INTEGRATION.md)

---

## 🚀 4. Operations, Deployment & DevOps
- [Docker Production Setup Guide](deployment/docker-setup.md)
- [Production Deployment Checklist](deployment/production-checklist.md)
- [Kubernetes Helm Deployment Guide](deployment/kubernetes-helm-guide.md)
- [AWS ECS Fargate Guide](deployment/aws-ecs-fargate.md)
- [Google Cloud Run Deployment](deployment/gcp-cloud-run.md)
- [Nginx Reverse Proxy Configuration](deployment/reverse-proxy-nginx.md)
- [Prometheus & Grafana Monitoring](deployment/monitoring-prometheus-grafana.md)
- [Backup & Disaster Recovery Runbook](deployment/backup-restore-runbook.md)
- [High Availability Clustering](deployment/high-availability-guide.md)
- [Linux Systemd Service Setup](deployment/systemd-service-setup.md)

---

## 🛠️ 5. Troubleshooting & Contributing
- [JVM Heap Dump & OOM Guide](troubleshooting/oom-heap-dumps.md)
- [Database Lock & Recovery Runbook](troubleshooting/corrupt-sqlite-recovery.md)
- [Platform Troubleshooting FAQ](TROUBLESHOOTING.md)
- [Backend Unit Testing Standards](contributing/unit-testing-standards.md)
- [Frontend UI Architecture Guidelines](contributing/frontend-component-guidelines.md)
- [Rule Authoring Guide](RULE_AUTHORING.md)
- [Release Process Checklist](contributing/release-process-checklist.md)
