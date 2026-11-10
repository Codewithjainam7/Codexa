# ENISA Cloud Security & EUCS Compliance Matrix

The **European Union Agency for Cybersecurity (ENISA)** Guidelines for Cloud Security and the draft **European Cybersecurity Certification Scheme for Cloud Services (EUCS)** define mandatory and recommended requirements for cloud service providers (CSPs) and enterprise tenants handling European data.

---

## ENISA Security Objectives & Codexa Automated Rules

| ENISA Cloud Domain | Security Objective | Objective Details | Codexa Rule Mapping |
| :--- | :--- | :--- | :--- |
| **IAM: Identity & Access Management** | IAM-01 | Strong Authentication & Federation | `CR-OAUTH-001` (OAuth2 PKCE, redirect URI protection) |
| | IAM-04 | Cross-Origin Request Boundaries | `CR-CORS-001`, `CR-CORS-002` (Credential origin reflection) |
| | IAM-06 | Least Privilege & Role Separation | `CR-AUTH-001`, `CR-RLS-001`, `CR-RSC-001` |
| **CRY: Cryptography & Key Management** | CRY-01 | Cryptographic Ciphers & Modes | `CR-CRYPTO-001` (AES-GCM, IV reuse prevention), `CR-RAND-001` |
| | CRY-02 | Encryption Key Lifecycle & Rotation | `CR-IAC-010` (KMS Customer Managed Key rotation) |
| | CRY-03 | Transit Transport Encryption | `CR-SEC-010`, `CR-IAC-011` (CloudFront HTTPS, S3 access) |
| **TEN: Multi-Tenancy & Isolation** | TEN-01 | Tenant Memory & Context Isolation | `CR-THREAD-001` (ThreadLocal context leaks in thread pools) |
| | TEN-02 | Container Isolation & Breakout Prevention | `CR-IAC-009` (Dockerfile SUID/sudo), `CR-K8S-001` (hostPath) |
| | TEN-04 | Container Secret Storage | `CR-K8S-002` (Kubernetes secret env variable leakage) |
| **DEV: Secure Development & CI/CD** | DEV-01 | Automated SAST Gating in Pipelines | `CR-CICD-001` (Workflow script injection), Codexa CLI |
| | DEV-02 | Input Sanitization & Parameterization | `CR-SQL-001`, `CR-NOSQL-001`, `CR-SSTI-001`, `CR-XXE-001` |
| | DEV-04 | Safe Deserialization & Memory Integrity | `CR-DESER-001`, `CR-DESER-002`, `CR-PROTO-001` |
| | DEV-06 | Architectural Clean Boundaries | `CR-ARCH-001`, `CR-ARCH-002`, `FanInFanOutCalculator` |
| **SUP: Supply Chain & Dependencies** | SUP-01 | Software Bill of Materials (SBOM) | CycloneDX v1.5 JSON Generator, `CR-DEP-001` |
| | SUP-03 | Open-Source License Governance | `LicenseAuditor` (GPL/AGPL copyleft enforcement) |
| | SUP-04 | Dependency Confusion & Hijack Protection | `CR-SUPPLY-001` (PyPI/npm repository proxy scoping) |
| **LOG: Logging & Auditing** | LOG-01 | Sensitive Log Scrubbing | `CR-LOG-001` (Log injection & secret redaction) |
