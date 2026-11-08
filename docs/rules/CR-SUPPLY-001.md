# CR-SUPPLY-001: Dependency Confusion & Supply Chain Insecurity

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-SUPPLY-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-829: Inclusion of Functionality from Untrusted Control Sphere](https://cwe.mitre.org/data/definitions/829.html) |
| **OWASP Top 10** | A06:2021 – Vulnerable and Outdated Components |
| **Languages** | Python (pip), Node.js (npm) |

---

## Vulnerability Overview

Dependency Confusion occurs when package managers (e.g. `pip` or `npm`) query multiple package registries simultaneously. If an internal corporate library (e.g. `internal-auth-lib`) is requested, an attacker who registers `internal-auth-lib` on the public registry with version `99.99.99` will cause the client to fetch and execute malicious code during `npm install` or `pip install`.

---

## Secure Remediation

1. Scope internal packages under enterprise namespaces (e.g. `@mycompany/auth-lib`).
2. Configure private package registries as the sole upstream authority, avoiding `extra-index-url`.
