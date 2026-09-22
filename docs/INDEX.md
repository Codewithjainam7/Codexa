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

## 🛡️ 1. Static Rule Specifications (OWASP Top 10 & CWE)
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

## 🏗️ 2. Architecture & Engine Internals
- [High-Level Architecture & Pipeline Stages](architecture/pipeline-stages.md)
- [Multi-Language AST Engine](architecture/multi-language-engine.md)
- [Scoring Mathematics & Asymptotic Penalty Formulas](architecture/scoring-formula.md)
- [Java Concurrency & Managed ForkJoinPool Work-Stealing](architecture/concurrency-model.md)
- [JVM Memory & 64KB Streaming Buffer Architecture](architecture/memory-management.md)
- [Multi-Tier Caching & Content-Addressable LLM Cache](architecture/caching-strategy.md)
- [Incremental Delta Analysis & PR Diff Scanning](architecture/delta-analysis.md)
- [Fault-Tolerant Resilience Patterns & Circuit Breakers](architecture/resilience-patterns.md)
- [Custom Rule Provider Plugin Architecture](architecture/plugin-architecture.md)
- [SARIF v2.1.0 Schema Mapping](architecture/sarif-v2-mapping.md)
- [Data Retention & Staging Cleanup Policies](architecture/data-retention-policy.md)

---

## 🔒 3. Security, Hardening & Compliance
- [Zero-Trust Code Ingestion Architecture](security/zero-trust-architecture.md)
- [Zip Slip Traversal Protection](security/zip-slip-protection.md)
- [SSRF Defense & IP Whitelisting Architecture](security/ssrf-defense.md)
- [In-Flight Secret Masking & Shannon Entropy Redaction](security/secret-masking-specs.md)
- [Defensive AST Traversal & ReDoS Mitigation](security/ast-sanitization-guide.md)
- [Defensive HTTP Headers Matrix (CSP, COOP, HSTS)](security/defensive-headers-matrix.md)
- [Threat Model & Security Boundary Analysis](security/threat-model.md)
- [CWE Top 25 Coverage Matrix](security/cwe-top25-mapping.md)
- [Cryptographic Baselines & Standards](security/cryptographic-standards.md)
- [Incident Response Runbook](security/incident-response-runbook.md)

---

## 📡 4. API Reference & Webhooks
- [OpenAPI 3.0 Specifications](api/openapi-specs.md)
- [Rate Limiting & Token Bucket Algorithms](api/rate-limiting-guide.md)
- [Batch Analysis & Multi-Repository API](api/batch-analysis-api.md)
- [Server-Sent Events (SSE) Real-Time Streaming](api/streaming-events-sse.md)
- [Outbound Webhooks & HMAC-SHA256 Signatures](api/webhooks-guide.md)
- [Pagination & Sorting Specifications](api/pagination-specs.md)
- [Authentication Tokens & API Key Lifecycle](api/authentication-tokens.md)
- [API Versioning Policy](api/versioning-policy.md)
- [Multi-Language SDK Quickstart (Node, Python, cURL)](api/sdk-quickstart.md)

---

## ⚡ 5. Benchmarks & Real-World Studies
- [Empirical Accuracy & False-Positive Elimination Study](benchmarks/accuracy-and-false-positives.md)
- [Enterprise Monorepo Scaling & 100k-File Tuning](benchmarks/monorepo-scaling-guide.md)
- [Production Benchmark Suite & Case Studies](BENCHMARKS.md)

---

## 🚀 6. CI/CD & Enterprise Integrations
- [GitHub Actions PR Gating Workflow](GITHUB_ACTIONS.md)
- [GitLab CI Pipeline & Security Dashboard](GITLAB_CI.md)
- [Azure DevOps Pipelines Integration](integrations/azure-devops-pipeline.md)
- [SonarQube & SonarCloud SARIF Integration](integrations/sonarqube-integration.md)
- [SARIF v2.1.0 Integration Guide](SARIF_INTEGRATION.md)

---

## 🔮 7. Roadmap & Future Scope
- [Codexa v2.0 Architecture Vision & RFC](roadmap/v2-vision-and-rfc.md)
