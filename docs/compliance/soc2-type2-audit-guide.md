# Enterprise SOC 2 Type II Compliance & Audit Readiness Guide

## 1. Overview
SOC 2 (Service Organization Control 2) Type II evaluates an organization's operational and technical controls over an extended audit window (typically 3 to 12 months) according to the AICPA Trust Services Criteria (TSC).

Codexa automates the technical evidence collection and static analysis verification for the **Security (Common Criteria)**, **Confidentiality**, and **Availability** trust principles.

---

## 2. AICPA Trust Services Criteria Mapping

| TSC ID | Control Principle | Codexa Automated Verification | Relevant Rules |
| :--- | :--- | :--- | :--- |
| **CC6.1** | Logical Access Controls & Identity Management | Enforces authentication and authorization gates on all state-mutating endpoints | `CR-AUTH-001`, `CR-API-003`, `CR-CSRF-001` |
| **CC6.6** | Boundary Protection & Network Segmentation | Validates that sensitive services and databases are not exposed to public CIDRs | `CR-IAC-005`, `CR-SEC-009`, `CR-SSRF-001` |
| **CC6.7** | Transmission & Rest Encryption | Verifies that all external and internal communications require TLS and storage is encrypted | `CR-SEC-008`, `CR-IAC-006`, `CR-CRYPTO-001` |
| **CC7.1** | Vulnerability Identification & Patch Management | Scans direct and transitive dependencies against known CVE databases | `CR-SEC-002`, `CR-IAC-002` |
| **CC7.2** | Software Development Life Cycle (SDLC) Controls | Detects dangerous sinks (SQLi, Command Injection, Prompt Injection) before merge | `CR-SQL-001`, `CR-CMD-001`, `CR-LLM-001` |

---

## 3. Generating the Audit Attestation Packet
Auditors require formal, immutable evidence of automated static checks. Codexa generates an audit packet via the REST API:

```http
GET /api/v1/analyses/{jobId}/compliance?standard=soc2
```

The returned packet includes:
* Overall compliance verdict (`AUDIT_READY`, `CONDITIONAL_PASS`, `AUDIT_BLOCKED`)
* Total controls evaluated and pass percentage
* Itemized violation list linked to source files and git commit hashes
* Cryptographic timestamp of audit execution
