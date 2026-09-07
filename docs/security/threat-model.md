# Codexa Threat Model

Covers asset inventory, trust boundaries, entry points (ZIP archives, GitHub webhooks), and defense-in-depth mitigations.


### OWASP Top 10 Coverage Mapping

- **A01: Broken Access Control**: Detects hardcoded role bypasses and unauthenticated endpoints.
- **A02: Cryptographic Failures**: Flags weak hashing (MD5, SHA1) and unencrypted transit.
- **A03: Injection**: AST inspection flags unparameterized SQL, command execution, and LDAP queries.
- **A05: Security Misconfiguration**: Verifies secure defaults and disabled debug endpoints.
