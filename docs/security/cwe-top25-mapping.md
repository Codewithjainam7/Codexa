# Codexa CWE Top 25 (2024) Coverage Matrix

## Overview
Codexa's static analysis engine maps findings directly to the MITRE Common Weakness Enumeration (CWE) Top 25 Most Dangerous Software Weaknesses.

| Rank | CWE ID | Description | Codexa Primary Rule | Severity |
| :--- | :--- | :--- | :--- | :--- |
| **1** | CWE-79 | Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting') | `CR-XSS-001` | HIGH |
| **2** | CWE-89 | Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection') | `CR-SEC-001` | CRITICAL |
| **3** | CWE-78 | Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection') | `CR-CMD-001` | CRITICAL |
| **4** | CWE-502 | Deserialization of Untrusted Data | `CR-SEC-007` | CRITICAL |
| **5** | CWE-22 | Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal') | `CR-SEC-004` | HIGH |
| **6** | CWE-352 | Cross-Site Request Forgery (CSRF) | `CR-SEC-006` | MEDIUM |
| **7** | CWE-798 | Use of Hard-coded Credentials | `CR-SEC-002` | CRITICAL |
| **8** | CWE-918 | Server-Side Request Forgery (SSRF) | `CR-SEC-005` | HIGH |
| **9** | CWE-287 | Improper Authentication | `CR-SEC-003` | HIGH |
| **10** | CWE-321 | Use of Hard-coded Cryptographic Key | `CR-SEC-008` | HIGH |
