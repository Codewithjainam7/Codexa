# CR-DEAD-001: Dead Code & Unreachable Branch Detection

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-DEAD-001` |
| **Severity** | `LOW` |
| **Category** | `QUALITY` |
| **CWE ID** | [CWE-561: Dead Code](https://cwe.mitre.org/data/definitions/561.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | Java, JavaScript, TypeScript, Python, Go |

---

## Vulnerability Overview

Dead code consists of functions or statements that cannot be executed because no control flow path can reach them (e.g. statements after unconditional returns, `if (false)` clauses, or private methods that are never called). Dead code inflates compilation artifacts, complicates static analysis, and obscures true execution behavior.

---

## Secure Remediation

Remove dead code statements and unused private helpers:

```java
// Remove unreachable statements following returns:
public String formatName(String name) {
    if (name == null) return "";
    return name.trim();
    // Dead statements deleted here
}
```
