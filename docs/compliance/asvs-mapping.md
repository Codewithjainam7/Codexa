# OWASP Application Security Verification Standard (ASVS v4.0) Mapping

The **OWASP Application Security Verification Standard (ASVS) v4.0** defines a framework of security requirements for designing, building, and testing modern web applications and web services.

---

## ASVS v4.0 Level 1, 2 & 3 Compliance Matrix

| ASVS Chapter | Requirement Title | ASVS Level | Codexa Automated Rule Mapping |
| :--- | :--- | :---: | :--- |
| **V1: Architecture** | V1.4.1 Architectural Boundary Controls | L1 | `CR-ARCH-001`, `CR-QUAL-005` |
| **V2: Authentication** | V2.1.1 Password Storage & Hash Salt | L1 | `CR-PASS-001`, `CR-SEC-009` |
| | V2.7.1 Out-of-band Authenticator Secrets | L2 | `CR-SECRET-001`, `CR-SECRET-002`, `CR-JWT-001` |
| **V3: Session Management** | V3.2.1 Cryptographic Session Tokens | L1 | `CR-RAND-001` (SecureRandom verification) |
| | V3.5.3 Cookie Security Flags | L2 | `CR-CONFIG-001` (CORS and cookie attributes) |
| **V4: Access Control** | V4.1.1 Principle of Least Privilege | L1 | `CR-AUTH-001`, `CR-RLS-001`, `CR-RSC-001` |
| | V4.2.1 Operation Level Authorization (IDOR) | L1 | `CR-PARAM-002` (Tenant ownership checks) |
| **V5: Validation & Encoding** | V5.1.1 Structured Input Validation | L1 | `CR-OPS-004`, `CR-PARAM-001` (Mass Assignment) |
| | V5.2.4 Command Injection Prevention | L1 | `CR-CMD-001` (Process execution guards) |
| | V5.3.1 SQL Injection Prevention | L1 | `CR-SQL-001`, `CR-NOSQL-001` |
| | V5.3.3 Server-Side Template Injection | L1 | `CR-SSTI-001` (Template context model) |
| | V5.5.2 XML External Entity Resolution | L1 | `CR-XXE-001` (Disallow-doctype-decl) |
| **V6: Stored Cryptography** | V6.2.1 Robust Cipher Algorithm & Mode | L2 | `CR-CRYPTO-001`, `CR-CRYPTO-002` (AES-GCM / IV) |
| **V9: Communications** | V9.1.1 TLS Enforcement Across All Services | L1 | `CR-SEC-010`, `CR-GRPC-001` (TLS credentials) |
| **V14: Configuration** | V14.2.1 Third-Party Dependency Manifests | L1 | `CR-DEP-001`, `CR-SUPPLY-001` |
| | V14.5.1 Restrict Cloud Storage Buckets | L1 | `CR-IAC-008` (S3 Public Access Block) |
