# OWASP Top 10 (2021) Compliance & Rule Mapping Matrix

This document provides a comprehensive cross-reference mapping of Codexa static analysis rules against the **OWASP Top 10:2021** standard, documenting specific vulnerability identifiers, corresponding CWEs, and detection severities.

---

## 1. Compliance Matrix

| OWASP Category | Codexa Rule ID | Vulnerability Title | CWE Mapping | Default Severity |
| :--- | :--- | :--- | :--- | :--- |
| **A01:2021 — Broken Access Control** | `CR-PATH-001` | Path Traversal & Zip Slip Ingestion | [CWE-22](https://cwe.mitre.org/data/definitions/22.html) | `CRITICAL` |
| | `CR-AUTH-001` | Unauthenticated Controller Endpoint | [CWE-306](https://cwe.mitre.org/data/definitions/306.html) | `HIGH` |
| | `CR-RLS-001` | Missing Row-Level Security / Direct Object Reference | [CWE-639](https://cwe.mitre.org/data/definitions/639.html) | `HIGH` |
| | `CR-CORS-001` | Permissive Wildcard CORS with Credentials | [CWE-942](https://cwe.mitre.org/data/definitions/942.html) | `HIGH` |
| **A02:2021 — Cryptographic Failures** | `CR-SECRET-001` | Hardcoded API Keys & Passwords | [CWE-798](https://cwe.mitre.org/data/definitions/798.html) | `CRITICAL` |
| | `CR-CRYPTO-001`| Weak Cryptographic Algorithm (DES / 3DES / RC4) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) | `HIGH` |
| | `CR-HASH-001` | Insecure Hash Algorithm (MD5 / SHA-1) | [CWE-328](https://cwe.mitre.org/data/definitions/328.html) | `MEDIUM` |
| | `CR-RAND-001` | Insecure Pseudorandom Number Generator | [CWE-338](https://cwe.mitre.org/data/definitions/338.html) | `MEDIUM` |
| **A03:2021 — Injection** | `CR-SQL-001` | SQL Injection via String Concatenation | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) | `CRITICAL` |
| | `CR-CMD-001` | OS Command Injection via Shell Execution | [CWE-78](https://cwe.mitre.org/data/definitions/78.html) | `CRITICAL` |
| | `CR-XSS-001` | Cross-Site Scripting (Unescaped Reflected Output) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) | `HIGH` |
| | `CR-PARAM-001`| Prototype Pollution via Parameter Merging | [CWE-1321](https://cwe.mitre.org/data/definitions/1321.html) | `HIGH` |
| **A04:2021 — Insecure Design** | `CR-QUAL-001` | High Cyclomatic Complexity (> 25) | [CWE-1074](https://cwe.mitre.org/data/definitions/1074.html) | `LOW` / `MEDIUM` |
| | `CR-QUAL-003` | Deep Statement Nesting (> 6 Levels) | [CWE-1075](https://cwe.mitre.org/data/definitions/1075.html) | `LOW` |
| | `CR-PERF-001` | Quadratic String Concatenation in Loop via `+=` | [CWE-400](https://cwe.mitre.org/data/definitions/400.html) | `LOW` |
| **A05:2021 — Security Misconfiguration**| `CR-CONFIG-001`| Disabled TLS Certificate / Hostname Validation | [CWE-295](https://cwe.mitre.org/data/definitions/295.html) | `CRITICAL` |
| | `CR-CSRF-001` | Disabled CSRF Protection on Mutating Routes | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) | `HIGH` |
| | `CR-DEBUG-001` | Exposed Actuator / Debug Endpoints | [CWE-489](https://cwe.mitre.org/data/definitions/489.html) | `MEDIUM` |
| **A07:2021 — Identification & Auth** | `CR-PASS-001` | Plaintext Password Storage / Weak Hashing | [CWE-916](https://cwe.mitre.org/data/definitions/916.html) | `CRITICAL` |
| | `CR-JWT-001` | Permissive JWT Verification (Algorithm 'none') | [CWE-347](https://cwe.mitre.org/data/definitions/347.html) | `HIGH` |
| **A08:2021 — Software & Data Integrity**| `CR-DESER-001`| Unsafe Object Deserialization | [CWE-502](https://cwe.mitre.org/data/definitions/502.html) | `CRITICAL` |
| | `CR-QUAL-004` | Duplicated Code Blocks (Shingles $\ge 15$) | [CWE-1041](https://cwe.mitre.org/data/definitions/1041.html) | `LOW` |
| **A09:2021 — Security Logging Failures** | `CR-QUAL-006` | Swallowed Exception in Empty Catch Block | [CWE-390](https://cwe.mitre.org/data/definitions/390.html) | `MEDIUM` |
| | `CR-LOG-001` | Sensitive Data (PII/Tokens) Written to Logs | [CWE-532](https://cwe.mitre.org/data/definitions/532.html) | `MEDIUM` |
| **A10:2021 — Server-Side Request Forgery**| `CR-SSRF-001` | Outbound Request to Unvalidated Target URL | [CWE-918](https://cwe.mitre.org/data/definitions/918.html) | `CRITICAL` |

---

## 2. Compliance Auditing & Export Capabilities

All scan findings can be exported to standard compliance formats:
- **SARIF v2.1.0**: Directly importable into GitHub Code Scanning, GitLab Security Dashboard, and SonarQube.
- **Executive Audit JSON**: Full programmatic access with CWE numbers, file coordinates, and remediation diffs.
