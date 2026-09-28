# Codexa Regulatory Compliance & Audit Attestation Matrix

This document specifies the enterprise regulatory audit framework, control mapping taxonomy, and continuous compliance evaluation engine implemented in **Codexa**.

---

## 1. Executive Summary & Regulatory Scope

Modern enterprise engineering organizations are subject to mandatory regulatory oversight to achieve enterprise sales, protect customer payment data, and maintain customer trust. Codexa translates raw static analysis findings into actionable, audit-ready regulatory evidence across four premier compliance standards:

1. **SOC 2 Type II** (AICPA Trust Services Criteria: Security, Availability, and Confidentiality).
2. **ISO/IEC 27001:2022** (Information Security Management System - Annex A Controls).
3. **PCI-DSS v4.0** (Payment Card Industry Data Security Standard).
4. **OWASP Top 10:2021** (Web Application Security Risks).

---

## 2. Regulatory Control Mapping Architecture

```
[ Ingested Source Code / Repository Analysis ]
                      │
                      ▼
[ 30+ Deterministic AST Rules & Polyglot Findings ]
                      │
                      ▼
┌────────────────────────────────────────────────────────┐
│         Codexa ComplianceAuditService Engine           │
│                                                        │
│  - Extracts Rule IDs & CWE Classifications             │
│  - Correlates against Regulatory Control Taxonomy      │
│  - Evaluates Control Status: COMPLIANT / NON_COMPLIANT │
│  - Computes Audit Attestation: AUDIT_READY / BLOCKED   │
└─────────────────────┬──────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────┐
│     Automated Compliance Packet (JSON / Executive)     │
│   GET /api/v1/analyses/{jobId}/compliance?standard=... │
└────────────────────────────────────────────────────────┘
```

---

## 3. SOC 2 Type II Control Mapping (Trust Services Criteria)

| SOC 2 Criteria | Control Title | Description | Mapped Codexa Rules & CWEs |
|:---|:---|:---|:---|
| **CC6.1** | Logical Access Controls & Secrets | Prevent hardcoded credentials and broken authorization. | `CR-SECRET-001`, `CR-AUTH-001`, `CR-RLS-001` (CWE-798, CWE-306, CWE-639) |
| **CC6.6** | Perimeter Security & Injection Defenses | Prevent SQL, OS Command, and Cross-Site Scripting injections. | `CR-SQL-001`, `CR-CMD-001`, `CR-XSS-001`, `CR-SSRF-001` (CWE-89, CWE-78, CWE-79, CWE-918) |
| **CC6.7** | Data Encryption & Cryptography | Enforce robust ciphers, hashing, and TLS validation. | `CR-CRYPTO-001`, `CR-HASH-001`, `CR-CONFIG-001`, `CR-RAND-001` (CWE-327, CWE-328, CWE-295, CWE-338) |
| **CC6.8** | Malicious Software & Supply Chain | Audit third-party dependencies for CVEs and unsafe deserialization. | `CR-DEP-001`, `CR-DESER-001` (CWE-1395, CWE-502) |
| **CC7.1** | Vulnerability Detection & Governance | Maintain code quality, cyclomatic limits, and automated testing gates. | `CR-QUAL-001`, `CR-QUAL-003`, `CR-PERF-001` (CWE-1074, CWE-1075, CWE-400) |
| **CC7.2** | Security Logging & Monitoring | Record system events while preventing credential / PII leakage in logs. | `CR-LOG-001`, `CR-QUAL-006` (CWE-532, CWE-390) |

---

## 4. ISO/IEC 27001:2022 Annex A Control Mapping

| ISO 27001 Control | Control Name | Evaluated Codexa Safeguards |
|:---|:---|:---|
| **A.8.20** | Network Security | Mitigate Server-Side Request Forgery (`CR-SSRF-001`) and permissive CORS (`CR-CORS-001`). |
| **A.8.24** | Use of Cryptography | Eliminate deprecated ciphers, collision-prone MD5/SHA-1 hashes, and weak PRNGs. |
| **A.8.28** | Secure Coding Principles | Prevent SQL Injection, Command Execution, Path Traversal, and Insecure Deserialization. |
| **A.8.29** | Security Testing in Development | Enforce automated static analysis gates in continuous integration pipelines. |
| **A.8.30** | Outsourced & Component Security | Audit open-source libraries against public CVE advisories (SBOM extraction). |

---

## 5. PCI-DSS v4.0 Requirement Mapping

| PCI-DSS Req | Requirement Title | Compliance Enforced by Codexa |
|:---|:---|:---|
| **Req 3.4** | Protect Cardholder Data & Key Storage | Prohibits hardcoded API keys, private keys, or tokens in source code (`CR-SECRET-001`). |
| **Req 6.2** | Secure Software Development Lifecycle | Mitigates OWASP Top 10 vulnerabilities including SQLi, XSS, and command injection. |
| **Req 6.3** | Supply Chain Software Security | Identifies known CVEs in third-party libraries via automated SBOM scanning (`CR-DEP-001`). |
| **Req 10.2** | Audit Logging Integrity | Enforces logging best practices while redacting sensitive tokens and credentials (`CR-LOG-001`). |

---

## 6. REST API Usage & Automation

Security officers and compliance engineers can query continuous audit reports directly via the REST API:

```http
GET /api/v1/analyses/{jobId}/compliance?standard=soc2
```

### Sample Response:
```json
{
  "jobId": "4a71b12b-6893-4a18-9730-1b29a2886c91",
  "standard": "SOC2_TYPE2",
  "targetRepository": "Codewithjainam7/Codexa",
  "evaluatedAt": "2026-09-28T21:42:00Z",
  "overallStatus": "AUDIT_READY",
  "complianceScore": 100.0,
  "totalControls": 6,
  "compliantControls": 6,
  "nonCompliantControls": 0,
  "totalViolations": 0,
  "criticalViolations": 0,
  "executiveSummary": "The audited codebase satisfies SOC 2 Type II requirements with an enterprise compliance score of 100.0% (6 of 6 controls satisfied). Zero critical compliance blockers were identified across static attack surfaces."
}
```
