# Codexa AST Security Isolation & OWASP Rules Architecture

Codexa implements a zero-bytecode execution model. Codebases are parsed deterministically into Abstract Syntax Trees (AST) using JavaParser and multi-language lexical pattern analyzers without executing untrusted classes, shell scripts, or compilation hooks.

---

## 1. 19+ Deterministic Static Rules Catalog

| Rule ID | Rule Name | Category | Severity | OWASP Mapping | Engine |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-SQL-001` | SQL Injection via String Concatenation | SECURITY | CRITICAL | OWASP A03:2021 | JavaParser AST |
| `CR-CMD-001` | Command Injection via ProcessBuilder / Runtime.exec | SECURITY | CRITICAL | OWASP A03:2021 | JavaParser AST |
| `CR-XSS-001` | Cross-Site Scripting (Unescaped Output) | SECURITY | HIGH | OWASP A03:2021 | JavaParser AST |
| `CR-SEC-001` | Hardcoded Credentials & API Secrets | SECURITY | HIGH | CWE-798 | Regex + Shannon Entropy |
| `CR-CRYP-001`| Weak Cryptographic Algorithm (MD5 / SHA-1 / DES) | SECURITY | HIGH | OWASP A02:2021 | JavaParser AST |
| `CR-PASS-001`| Insecure Password Storage (Plaintext / Simple Hash) | SECURITY | HIGH | OWASP A07:2021 | JavaParser AST |
| `CR-AUTH-001`| Missing Access Control on Controller Endpoints | SECURITY | HIGH | OWASP A01:2021 | JavaParser AST |
| `CR-LOG-001` | Sensitive Data Exposure in Logs (PII / Passwords) | SECURITY | MEDIUM | OWASP A09:2021 | JavaParser AST |
| `CR-CONF-001`| Insecure Spring Boot Configuration (Actuator / CORS) | SECURITY | MEDIUM | OWASP A05:2021 | Config Analyzer |
| `CR-DEPR-001`| Vulnerable Dependency / Outdated Library Usage | SECURITY | MEDIUM | OWASP A06:2021 | Pom / Gradle Parser |
| `CR-SSRF-001`| Server-Side Request Forgery via Unvalidated URL | SECURITY | HIGH | OWASP A10:2021 | JavaParser AST |
| `CR-XXE-001` | XML External Entity Injection (Insecure SAX Parser) | SECURITY | HIGH | OWASP A05:2021 | JavaParser AST |
| `CR-DESER-001`| Insecure Object Deserialization (`readObject()`) | SECURITY | CRITICAL | OWASP A08:2021 | JavaParser AST |
| `CR-PATH-001`| Path Traversal / File Disclosure via User Input | SECURITY | HIGH | OWASP A01:2021 | JavaParser AST |
| `CR-ARCH-001`| Architectural Separation / Repository Leak in Controller | QUALITY | HIGH | Clean Architecture | JavaParser AST |
| `CR-COMP-001`| Excessive Cyclomatic Complexity (> 15) | QUALITY | MEDIUM | Clean Code | AST Complexity |
| `CR-NEST-001`| Deep Control Flow Nesting (> 4 Levels) | QUALITY | MEDIUM | Clean Code | AST Depth |
| `CR-ERR-001`  | Swallowed / Empty Catch Blocks | QUALITY | MEDIUM | Robustness | JavaParser AST |
| `CR-OPS-001`  | Missing Health Checks / Actuator Hardening | OPERATIONS | MEDIUM | Production Ops | Config Analyzer |

---

## 2. Ingestion Defense & Quota Guard

1. **Zip Slip Defense:** Validates target extraction canonical path starts with the sandboxed base directory before creating any file.
2. **Zip Bomb Protection:** Limits extraction to **1,000 MB** total and **500 MB** compressed archive size.
3. **File Count Boundary:** Hard limit of **20,000 files** per audit job.
4. **Single File Threshold:** Hard limit of **100 MB** per individual file.
5. **Directory Traversal Quota:** Max recursion depth capped at **30 levels**.

---

## 3. Secret Masking

All code evidence extracted by the scanner passes through the Shannon Entropy & Key Redaction pipeline:
- API keys, AWS tokens, private keys, and passwords are masked before being displayed or passed to AI models:
  ```
  const apiKey = "AKIAIOSFODNN7EXAMPLE";  -->  const apiKey = "[REDACTED_SECRET]";
  ```
