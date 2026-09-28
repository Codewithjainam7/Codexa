# Codexa Master Documentation Index 📚

Welcome to the comprehensive technical documentation for **Codexa**—the AI-augmented, multi-language code review, static security auditing, and production readiness platform.

---

## 🌟 Core Overviews & Executive Summaries
- **[Official Academic Project Synopsis (PDF)](../Codexa_Project_Synopsis.pdf)**: Complete 7-page academic and technical synopsis with cover page, index, guide details (Mrs. Mohini Ma'am), team members, core algorithms, and empirical benchmarks.
- **[Technical Capabilities & Feature Reference](CAPABILITIES.md)**: Exhaustive breakdown of ingestion, AST parsing, polyglot rules, attack surface discovery, scoring, and remediation.
- **[Key Achievements, Milestones & Case Studies](ACHIEVEMENTS.md)**: Real-world production audits (SmartLot case study), 135 passing tests, 3GB benchmarks, and engineering milestones.
- **[Static Analysis & Security Rule Catalog](RULES_CATALOG.md)**: Detailed specifications for all 30+ rules mapped to OWASP Top 10 and CWE.
- **[Scale & Performance Benchmarks](BENCHMARKS.md)**: Empirical throughput benchmarks, buffer scaling, AST concurrency tests, and heap profiles.
- **[Changelog & Version History](CHANGELOG.md)**: Comprehensive release history across v1.3.0, v1.2.0, and v1.0.0.

---

## 🏛️ 1. Architecture Decision Records (ADRs)
- **[ADR Overview & Index](adr/README.md)**: Inventory of formal architectural decisions using MADR.
- **[ADR-0001: Record Architecture Decisions](adr/0001-record-architecture-decisions.md)**: Adopting Markdown Architectural Decision Records (MADR) format.
- **[ADR-0002: Java 21 LTS & Virtual Threads](adr/0002-java-21-virtual-threads.md)**: Project Loom virtual threads for I/O and ForkJoinPool for CPU tasks.
- **[ADR-0003: Unified AST Multi-Language Engine](adr/0003-ast-multi-language-engine.md)**: Hybrid engine architecture (JavaParser + Polyglot Token Scanners).
- **[ADR-0004: SQLite WAL Mode & PostgreSQL Dual-Engine](adr/0004-sqlite-wal-storage.md)**: Zero-config developer SQLite WAL and enterprise PostgreSQL.
- **[ADR-0005: Standardization on OASIS SARIF v2.1.0](adr/0005-sarif-standard-output.md)**: Universal security reporting standard for GitHub and IDE tools.
- **[ADR-0006: Monochromatic Luxury Cybersecurity Design System](adr/0006-monochromatic-luxury-ui.md)**: OLED black canvas, subtle micro-borders, and reserved emerald functional accents.
- **[ADR-0007: Resilient AI Model Cascade & Offline Circuit Breaker](adr/0007-openrouter-resilient-fallback.md)**: Multi-provider cascade and offline deterministic template fallback.
- **[ADR-0008: Content-Addressable SHA-256 AST & Remediation Caching](adr/0008-content-addressable-ast-cache.md)**: Eliminating redundant AST parses and saving 68%+ AI inference tokens.

---

## 🛡️ 2. Static Rule Specifications (OWASP Top 10 & CWE)
- **[CR-SQL-001: SQL Injection Detection](rules/CR-SQL-001.md)** (CWE-89, A03:2021)
- **[CR-CMD-001: OS Command Injection](rules/CR-CMD-001.md)** (CWE-78, A03:2021)
- **[CR-PATH-001: Path Traversal & Zip Slip](rules/CR-PATH-001.md)** (CWE-22, A01:2021)
- **[CR-AUTH-001: Missing Access Control & Endpoints](rules/CR-AUTH-001.md)** (CWE-306, A01:2021)
- **[CR-CORS-001: Permissive Wildcard CORS Policy](rules/CR-CORS-001.md)** (CWE-942, A01:2021)
- **[CR-RLS-001: Insecure Direct Object Reference (IDOR)](rules/CR-RLS-001.md)** (CWE-639, A01:2021)
- **[CR-SECRET-001: Hardcoded Secrets & Shannon Entropy](rules/CR-SECRET-001.md)** (CWE-798, A02:2021)
- **[CR-CRYPTO-001: Broken Cryptography & Legacy Ciphers](rules/CR-CRYPTO-001.md)** (CWE-327, A02:2021)
- **[CR-HASH-001: Insecure Hash Algorithms (MD5 / SHA-1)](rules/CR-HASH-001.md)** (CWE-328, A02:2021)
- **[CR-RAND-001: Insecure Pseudorandom Generators](rules/CR-RAND-001.md)** (CWE-338, A02:2021)
- **[CR-XSS-001: Cross-Site Scripting Detection](rules/CR-XSS-001.md)** (CWE-79, A03:2021)
- **[CR-PARAM-001: Prototype Pollution & Parameter Merging](rules/CR-PARAM-001.md)** (CWE-1321, A03:2021)
- **[CR-QUAL-001: High Cyclomatic Complexity (> 25)](rules/CR-QUAL-001.md)** (CWE-1074, A04:2021)
- **[CR-QUAL-003: Deep Control Flow Nesting (> 6)](rules/CR-QUAL-003.md)** (CWE-1075, A04:2021)
- **[CR-PERF-001: String Concatenation in Loops](rules/CR-PERF-001.md)** (CWE-400, A04:2021)
- **[CR-CONFIG-001: Disabled TLS Validation & Hostname Checks](rules/CR-CONFIG-001.md)** (CWE-295, A05:2021)
- **[CR-CSRF-001: Disabled CSRF Protection on Mutating Routes](rules/CR-CSRF-001.md)** (CWE-352, A05:2021)
- **[CR-DEBUG-001: Exposed Actuator & Debug Endpoints](rules/CR-DEBUG-001.md)** (CWE-489, A05:2021)
- **[CR-PASS-001: Plaintext Password Storage & Weak KDFs](rules/CR-PASS-001.md)** (CWE-916, A07:2021)
- **[CR-JWT-001: JWT Verification Flaws & Alg Bypass](rules/CR-JWT-001.md)** (CWE-347, A07:2021)
- **[CR-DESER-001: Insecure Object Deserialization](rules/CR-DESER-001.md)** (CWE-502, A08:2021)
- **[CR-QUAL-004: Duplicated Code Blocks & N-Gram Shingles](rules/CR-QUAL-004.md)** (CWE-1041, A08:2021)
- **[CR-QUAL-006: Swallowed Exceptions & Empty Catch Blocks](rules/CR-QUAL-006.md)** (CWE-390, A09:2021)
- **[CR-LOG-001: Sensitive Data Logging & AI Token Filtering](rules/CR-LOG-001.md)** (CWE-532, A09:2021)
- **[CR-SSRF-001: Server-Side Request Forgery](rules/CR-SSRF-001.md)** (CWE-918, A10:2021)

---

## 🏗️ 3. Architecture & Engine Internals
- **[High-Level Architecture & Pipeline Stages](architecture/pipeline-stages.md)**: 4-stage pipeline (Ingestion, AST Parsing, Rule Evaluation, Scoring).
- **[Multi-Language Static Analysis & Polyglot Engine](architecture/multi-language-engine.md)**: 3-tier polyglot architecture (JavaParser + Token Lexers + IaC Auditing).
- **[AI Model Fallback & Multi-Provider Cascade](architecture/ai-fallback-routing.md)**: 3-tier cascade, OpenRouter routing, and offline deterministic template engine.
- **[Scoring Mathematics & Asymptotic Penalty Formulas](architecture/scoring-formula.md)**: Exponential decay and asymptotic saturation formulas for readiness score.
- **[Java Concurrency & Managed ForkJoinPool Work-Stealing](architecture/concurrency-model.md)**: Virtual threads and parallel analysis scheduling.
- **[JVM Memory & 64KB Streaming Buffer Architecture](architecture/memory-management.md)**: Bounded memory footprint and streaming archive extractors.
- **[Multi-Tier Caching & Content-Addressable LLM Cache](architecture/caching-strategy.md)**: SHA-256 AST hash cache for instantaneous remediation lookups.
- **[Incremental Delta Analysis & PR Diff Scanning](architecture/delta-analysis.md)**: Scanning only changed lines and modified AST nodes in pull requests.
- **[Fault-Tolerant Resilience Patterns & Circuit Breakers](architecture/resilience-patterns.md)**: Isolation boundaries, retry decorators, and circuit breaker transitions.
- **[Custom Rule Provider Plugin SPI Architecture](architecture/plugin-architecture.md)**: Java ServiceLoader SPI and Spring auto-discovery for custom rules.
- **[SARIF v2.1.0 Schema Mapping Reference](architecture/sarif-v2-mapping.md)**: OASIS SARIF v2.1.0 JSON schema mapping and physical URI normalization.
- **[Data Retention, Ephemeral Staging & Lifecycle Policy](architecture/data-retention-policy.md)**: Zero-Residual Ingestion, directory sweeps, and database pruning.

---

## 🔒 4. Security, Hardening & Compliance
- **[Zero-Trust Code Ingestion Architecture](security/zero-trust-architecture.md)**: 5-layer defense-in-depth model and container sandboxing.
- **[Zip Slip Traversal Protection](security/zip-slip-protection.md)**: Canonical path verification and Zip bomb decompression guards.
- **[Server-Side Request Forgery (SSRF) Defense](security/ssrf-defense.md)**: RFC 1918 / RFC 3927 metadata blocking and DNS rebinding IP pinning.
- **[Process Sandboxing & Container Isolation](security/sandboxing-mechanisms.md)**: Seccomp-BPF profiles, cgroups v2, noexec staging, and k8s hardening.
- **[Software Supply Chain Security, SBOM & SLSA](security/supply-chain-security.md)**: SLSA Level 3 matrix, CycloneDX/SPDX SBOMs, and Sigstore Cosign signing.
- **[Structured Audit Logging & SIEM Compliance](security/audit-logging-standards.md)**: ECS JSON log format, in-flight secret scrubbing, and WORM storage.
- **[GDPR & Data Privacy Compliance Guide](security/gdpr-compliance-guide.md)**: Privacy by Design (Art. 25), PII stripping, and Article 17 erasure cascade.
- **[In-Flight Secret Masking & Shannon Entropy Redaction](security/secret-masking-specs.md)**: Automated token redactor preventing credential leakage to AI APIs.
- **[Defensive AST Traversal & ReDoS Mitigation](security/ast-sanitization-guide.md)**: Regular expression timeouts and recursion depth bounds.
- **[Defensive HTTP Headers Matrix (CSP, COOP, HSTS)](security/defensive-headers-matrix.md)**: Complete security response header configurations.
- **[Threat Model & Security Boundary Analysis](security/threat-model.md)**: STRIDE threat modeling across all platform entrypoints.
- **[MITRE CWE Top 25 Coverage Matrix](security/cwe-top25-mapping.md)**: Exhaustive 25-rank coverage table with AST detection heuristics.
- **[Cryptographic Baselines & Encryption Standards](security/cryptographic-standards.md)**: NIST SP 800-131A, TLS 1.3 cipher suites, and Argon2id KDF parameters.
- **[Security Incident Response & Triage Runbook](security/incident-response-runbook.md)**: CVSS v3.1 severity classification, 5-phase lifecycle, and forensic SOPs.

---

## 📡 5. API Reference & Webhooks
- **[OpenAPI 3.0 Specifications](api/openapi-specs.md)**: Interactive Swagger/OpenAPI schema documentation.
- **[Rate Limiting & Token Bucket Algorithms](api/rate-limiting-guide.md)**: Bounded sliding window rate limiters per IP.
- **[Batch Analysis & Multi-Repository API](api/batch-analysis-api.md)**: Concurrently submitting and orchestrating multi-repo scans.
- **[Server-Sent Events (SSE) Real-Time Streaming](api/streaming-events-sse.md)**: Real-time progress updates during analysis execution.
- **[Outbound Webhooks & HMAC-SHA256 Signatures](api/webhooks-guide.md)**: Event notifications with cryptographic payload verification.
- **[API Pagination & Query Filtering Specifications](api/pagination-specs.md)**: Spring Data Pageable contracts, keyset seek, and performance benchmarks.
- **[Authentication Tokens, API Keys & Lifecycle Architecture](api/authentication-tokens.md)**: Token taxonomy, zero-plaintext storage, and emergency revocation.
- **[API Versioning Policy & Backward Compatibility](api/versioning-policy.md)**: URI versioning, additive schema rules, and RFC 8594 sunset deprecations.
- **[Multi-Language SDK Quickstart (Node, Python, cURL)](api/sdk-quickstart.md)**: Idiomatic client integration libraries.

---

## ⚡ 6. Benchmarks & Real-World Studies
- **[Empirical Accuracy & False-Positive Elimination Study](benchmarks/accuracy-and-false-positives.md)**: Empirical precision benchmarks and 0.0% false-positive achievement.
- **[Enterprise Monorepo Scaling & 100k-File Tuning](benchmarks/monorepo-scaling-guide.md)**: Memory footprint and throughput tuning for 100,000-file repositories.
- **[Production Benchmark Suite & Case Studies](BENCHMARKS.md)**: Real-world benchmark runs and SmartLot audit case study.

---

## 🚀 7. CI/CD & Enterprise Integrations
- **[OpenRouter AI Gateway Setup & Cascade](integrations/openrouter-setup.md)**: API key provisioning, model hierarchy, and air-gapped guidelines.
- **[GitHub Actions PR Gating Workflow](GITHUB_ACTIONS.md)**: Automated PR security gating workflow.
- **[GitLab CI Pipeline & Security Dashboard](GITLAB_CI.md)**: GitLab CI/CD integration with security dashboards.
- **[Azure DevOps Pipelines Integration](integrations/azure-devops-pipeline.md)**: Complete azure-pipelines.yml configuration.
- **[SonarQube & SonarCloud SARIF Integration](integrations/sonarqube-integration.md)**: SARIF v2.1.0 import and Quality Gate mapping.
- **[SARIF v2.1.0 Integration Guide](SARIF_INTEGRATION.md)**: Consuming SARIF in GitHub Code Scanning.

---

## 🚢 8. Production Deployment, Clustering & Monitoring
- **[Kubernetes & Helm Deployment Architecture Guide](deployment/kubernetes-helm-guide.md)**: Multi-replica cluster architecture, production values.yaml, and HPA.
- **[Observability: Prometheus, Grafana & Micrometer Metrics](deployment/monitoring-prometheus-grafana.md)**: Metric taxonomy, scrape configs, and Alertmanager rules.
- **[High Availability & Multi-Node Clustering Architecture](deployment/high-availability-guide.md)**: Active-Active stateless clustering, ShedLock, and HikariCP pooling.
- **[AWS ECS Fargate Production Deployment Guide](deployment/aws-ecs-fargate.md)**: Serverless container deployment on AWS ECS with Fargate and ALB.
- **[Google Cloud Run Serverless Deployment Guide](deployment/gcp-cloud-run.md)**: Knative service spec, Cloud SQL proxy, and Secret Manager.
- **[Linux Systemd Service Setup & Hardening](deployment/systemd-service-setup.md)**: Sandboxed systemd unit, dedicated user, and journald logging.
- **[Database Backup, Disaster Recovery & PITR Runbook](deployment/backup-restore-runbook.md)**: PostgreSQL parallel dumps, SQLite WAL snapshots, and restore checklists.
- **[Docker Setup & Production Images](deployment/docker-setup.md)**: Multi-stage container builds and non-root execution.
- **[NGINX Reverse Proxy Configuration](deployment/reverse-proxy-nginx.md)**: SSL termination and proxy buffering.

---

## 🔧 9. Operations & Forensic Troubleshooting
- **[SQLite Corruption & WAL Contention Recovery](troubleshooting/corrupt-sqlite-recovery.md)**: Diagnostic commands, .recover salvage, and WAL checkpoint forcing.
- **[JVM OutOfMemoryError & Heap Dump Forensics](troubleshooting/oom-heap-dumps.md)**: Capturing and analyzing JVM heap dumps under large repository scans.
- **[Common Operational Issues & Solutions](troubleshooting/common-issues.md)**: Quick resolution matrix for common production deployment questions.

---

## 🤝 10. Contributing & Engineering Standards
- **[Engineering Coding Standards & Guidelines](contributing/coding-standards.md)**: Java 21 idioms, rule authoring, error handling, and Conventional Commits.
- **[Backend Testing Conventions & Quality Gates](contributing/unit-testing-standards.md)**: Testing pyramid SLAs, positive/negative fixtures, and JaCoCo gates.
- **[Frontend Component Architecture & Design System](contributing/frontend-component-guidelines.md)**: Monochromatic luxury design, cn helper, and Framer Motion.
- **[Release Engineering & Versioning Checklist](contributing/release-process-checklist.md)**: SemVer 2.0.0, 4-phase quality gates, Sigstore signing, and hotfixes.
- **[Rule Development Tutorial](contributing/rule-development-tutorial.md)**: Step-by-step tutorial for building and testing custom static analysis rules.

---

## 🔮 11. Roadmap & Future Scope
- **[Codexa v2.0 Architecture Vision & RFC](roadmap/v2-vision-and-rfc.md)**: Language Server Protocol (LSP) IDE integration, enterprise rule DSL, and hybrid DAST fuzzing.
- **[Milestone Matrix & Release Schedule](roadmap/milestone-matrix.md)**: Multi-quarter engineering delivery roadmap.
