# Codexa GDPR & Data Privacy Compliance Architecture Guide

This guide details how **Codexa** adheres to the European Union General Data Protection Regulation (GDPR, Regulation (EU) 2016/679), implementing Privacy by Design (Art. 25), Data Minimization (Art. 5), Right to Erasure (Art. 17), and Data Portability (Art. 20).

---

## 1. Governance & Data Classification

Codexa acts primarily as a **Data Processor** when processing proprietary source code repositories on behalf of corporate customers. In processing source code, the system encounters two data classifications:

1. **First-Party Intellectual Property**: Source code files, architecture configurations, dependency manifests, and vulnerability reports.
2. **Personally Identifiable Information (PII)**: Developer commit author names, corporate email addresses, and potential PII accidentally embedded in source code comments or test fixtures.

---

## 2. Core GDPR Principles Implementation Matrix

| GDPR Article | Statutory Principle | Codexa Implementation Architecture | Status |
|:---|:---|:---|:---:|
| **Art. 5(1)(c)** | **Data Minimization** | Full source files are never stored in databases; only AST findings and structural diff snippets are persisted. Developer emails are hashed or omitted. | COMPLIANT |
| **Art. 5(1)(e)** | **Storage Limitation** | Staging directories are deleted immediately post-analysis in Java `finally` blocks. Automated orphan reapers sweep temporary directories hourly. | COMPLIANT |
| **Art. 17** | **Right to Erasure** | Dedicated REST deletion endpoint (`DELETE /api/v1/analyses/{id}`) cascades across database records, in-memory caches, and filesystem storage. | COMPLIANT |
| **Art. 20** | **Data Portability** | Customers can export 100% of their vulnerability data, metrics, and remediation history in standard OASIS SARIF v2.1.0 or open JSON formats. | COMPLIANT |
| **Art. 25** | **Privacy by Design** | External AI model routing is disabled by default until an explicit API key is configured. In-flight secret sanitization redacts credentials pre-flight. | COMPLIANT |
| **Art. 32** | **Security of Processing** | AES-256-GCM encryption at rest, TLS 1.3 in transit, unprivileged non-root container sandboxing, and Seccomp syscall filtering. | COMPLIANT |

---

## 3. Data Minimization: PII Redaction in Code Scans

When scanning Git repositories, Git commit metadata often contains personal identifiers:
- Committer full names (`John Doe`)
- Committer email addresses (`john.doe@company.com`)
- Author timestamps and GPG key IDs

### Codexa PII Stripping Pipeline:
During repository ingestion, `GitHubIngestionService` and `SecureZipExtractor` isolate source code files exclusively. Commit metadata, author logs, and user identity tables are discarded unless the customer explicitly enables Git blame correlation.

When Git blame is enabled:
- Email addresses are pseudonymized via salted SHA-256 hashes (`user_9b8f2c...`).
- Authorship mappings are stored ephemerally in RAM and wiped upon report export.

---

## 4. Right to Erasure Execution Protocol (`DELETE /api/v1/analyses/{id}`)

To satisfy Article 17 requirements, deleting an analysis job or project repository executes an atomic cascade across all storage tiers:

```
[ Administrative Delete Request: DELETE /api/v1/analyses/{jobId} ]
                               │
                               ▼
┌──────────────────────────────┴──────────────────────────────┐
│                  Stage 1: Relational Persistence            │
│  - DELETE FROM analyses WHERE id = :jobId                   │
│  - DELETE FROM findings WHERE job_id = :jobId               │
│  - DELETE FROM project_diagnostics WHERE job_id = :jobId    │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────┴──────────────────────────────┐
│                  Stage 2: In-Memory L1 Cache                │
│  - Evict cached AST CompilationUnits from Caffeine cache    │
│  - Invalidate progress SSE session buffers                  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────┴──────────────────────────────┐
│                  Stage 3: Ephemeral File Storage            │
│  - StagingManagerService.cleanDirectory(jobStagingDir)      │
│  - Recursive depth-first file unlinking                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Data Sovereignty & Cross-Border Transfer Safeguards

Under GDPR Chapter V (Articles 44–50), transferring personal or proprietary data outside the European Economic Area (EEA) requires strict adequacy decisions or Standard Contractual Clauses (SCCs).

### Self-Hosted Air-Gapped Deployment
Codexa is distributed as a self-contained Docker container image and Helm chart capable of operating completely on-premise:
- **Zero External Egress**: With `CODEXA_AI_ENABLED=false`, zero network packets exit the customer's VPC or private datacenter.
- **Local AI Inferences**: Customers can point the AI remediation endpoint to an internal, self-hosted LLM proxy (e.g. vLLM, Ollama, LocalAI) within their own EU sovereign datacenter.

---

## 6. Data Protection Impact Assessment (DPIA) Template

Enterprise compliance teams deploying Codexa can utilize this pre-completed DPIA summary:

- **Data Types Processed**: Static source code, software architecture AST nodes, compiler warnings, security defect snippets.
- **Storage Location**: Customer-controlled PostgreSQL database; temporary RAM/disk volumes.
- **Third-Party Data Processors**: None by default. If OpenRouter is configured, data transfers are governed by OpenRouter's zero-retention enterprise DPA with in-flight credential masking enforced by Codexa.
- **Risk Assessment**: Residual risk to data subjects classified as **LOW** due to deterministic ephemeral staging disposal and zero persistent source retention.
