# PCI-DSS v4.0 Compliance Mapping Matrix

Mapping of Codexa static rules to Payment Card Industry Data Security Standard v4.0.

| Rule ID | Rule Name | PCI-DSS v4.0 Requirement | Guidance |
| :--- | :--- | :--- | :--- |
| `CR-SEC-001` | SQL Injection Detection | Req. 6.2.4 | Sanitize database queries using parameterized statements |
| `CR-SEC-002` | Command Injection Detection | Req. 6.2.4 | Prevent OS command execution with untrusted parameters |
| `CR-SEC-003` | Hardcoded Secrets Detection | Req. 3.4 | Never store API keys or passwords in source code |
| `CR-SEC-004` | Insecure Cryptographic Random | Req. 3.4 | Use CSPRNG (`SecureRandom`) for cryptographic operations |
| `CR-SEC-005` | Disabled CSRF Protection | Req. 6.2.4 | Enforce state-changing token protection |
| `CR-SEC-006` | Insecure Deserialization | Req. 6.2.4 | Validate and restrict deserialization object filters |
| `CR-SEC-007` | Missing Authorization Check | Req. 8.3.1 | Enforce method-level `@PreAuthorize` access controls |
| `CR-SEC-008` | Path Traversal Vulnerability | Req. 6.2.4 | Prevent `../` directory navigation in file I/O |
| `CR-SEC-009` | Weak Hash Algorithm (MD5/SHA-1) | Req. 3.4 | Replace obsolete algorithms with SHA-256 or SHA-3 |
| `CR-SEC-010` | Disabled SSL/TLS Validation | Req. 6.2.4 | Enforce strict certificate authority verification |
