# ISO/IEC 27001:2022 Annex A Controls Verification

## 1. Overview
ISO/IEC 27001:2022 defines international standards for Information Security Management Systems (ISMS). Annex A contains 93 categorized information security controls across 4 domains: Organizational, People, Physical, and Technological.

Codexa specifically validates and provides automated compliance evidence for **Domain 8: Technological Controls**.

---

## 2. ISO 27001:2022 Technological Controls Mapping

| Control ID | Control Name | Codexa Static Audit Target |
| :--- | :--- | :--- |
| **A.8.8** | Management of Technical Vulnerabilities | Scans third-party packages for CVSS vulnerabilities and blast radius exposure (`SbomDependencyService`) |
| **A.8.9** | Configuration Management | Flags insecure container setups, untagged images, and root execution (`CR-IAC-001`, `CR-IAC-002`) |
| **A.8.20** | Network Security | Verifies that ingress ports are restricted and internal services are unexposed (`CR-IAC-005`) |
| **A.8.24** | Use of Cryptography | Audits deprecated algorithms (MD5, SHA-1, DES) and enforces TLS 1.3 / AES-256 (`CR-CRYPTO-001`, `CR-HASH-001`) |
| **A.8.28** | Secure Coding | Deterministic AST scanning for SQLi, XSS, SSRF, Deserialization, and Prompt Injection (`CR-SQL-001`, `CR-LLM-001`) |

---

## 3. Extracting the ISO 27001 Compliance Packet

```http
GET /api/v1/analyses/{jobId}/compliance?standard=iso27001
```

The resulting JSON report satisfies ISO certification audit requirements for automated continuous integration static testing.
