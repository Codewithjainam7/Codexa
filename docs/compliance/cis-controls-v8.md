# Center for Internet Security (CIS) Controls v8 Mapping Matrix

The **CIS Critical Security Controls (CIS Controls v8)** provides a prioritized set of prescriptive cybersecurity safeguards to defend enterprise IT systems and applications against prevailing cyber threats across Implementation Groups (**IG1, IG2, IG3**).

---

## CIS Controls v8 Application & Infrastructure Verification

| CIS Control | Safeguard ID | Safeguard Description | IG | Codexa Automated Rule Mapping |
| :--- | :--- | :--- | :---: | :--- |
| **02: Inventory of Software Assets** | 2.1 | Maintain detailed software inventory & manifests | IG1 | `CR-DEP-001`, `CR-SUPPLY-001`, CycloneDX SBOM Generator |
| | 2.7 | Detect and isolate unauthorized third-party libraries | IG2 | `LicenseAuditor`, `CR-SUPPLY-001` |
| **03: Data Protection** | 3.3 | Enforce data protection in transit (TLS/HTTPS) | IG1 | `CR-SEC-010`, `CR-IAC-011`, `CR-GRPC-001` |
| | 3.11 | Encrypt sensitive data at rest (Databases/Buckets) | IG2 | `CR-IAC-008`, `CR-IAC-010` (RDS & KMS rotation) |
| | 3.14 | Segment sensitive tenant data in memory | IG3 | `CR-THREAD-001` (ThreadLocal context isolation) |
| **04: Secure Configuration** | 4.1 | Enforce baseline configuration templates (IaC) | IG1 | `CR-IAC-001` through `CR-IAC-011` |
| | 4.8 | Disallow default passwords & hardcoded tokens | IG1 | `CR-SECRET-001`, `CR-SECRET-002`, `CR-HELM-001` |
| | 4.12 | Restrict root and container elevated privileges | IG2 | `CR-IAC-009` (Dockerfile SUID & Sudo), `CR-K8S-001` |
| **05: Account Management** | 5.2 | Enforce unique passwords & secure salt storage | IG1 | `CR-PASS-001`, `CR-SEC-009` |
| **06: Access Control** | 6.5 | Require Multi-Factor Authentication (MFA) & PKCE | IG1 | `CR-OAUTH-001` (OAuth2 PKCE & state parameter) |
| | 6.8 | Implement Role-Based Access Controls (RBAC) | IG2 | `CR-AUTH-001`, `CR-RLS-001`, `CR-RSC-001` |
| **07: Vulnerability Management** | 7.4 | Remediate critical detected software weaknesses | IG1 | `CR-SQL-001`, `CR-CMD-001`, `CR-XXE-001`, `CR-SSTI-001` |
| | 7.7 | Remediate open-source supply chain vulnerabilities | IG2 | `CR-DEP-001`, `CR-SUPPLY-001` |
| **16: Application Software Security** | 16.2 | Establish secure coding standards & linter rules | IG1 | All Codexa 65+ Static Rules Catalog |
| | 16.3 | Vet and scan external dependencies before release | IG1 | CycloneDX v1.5 SBOM, `LicenseAuditor` |
| | 16.4 | Enforce secure parsing of untrusted data | IG2 | `CR-DESER-001`, `CR-DESER-002`, `CR-PROTO-001` |
| | 16.7 | Perform static analysis (SAST) in CI/CD pipelines | IG2 | Codexa CLI GitHub Actions Integration (`CR-CICD-001`) |
| | 16.8 | Separate and isolate multi-tier architectural layers | IG3 | `CR-ARCH-001`, `CR-ARCH-002`, `FanInFanOutCalculator` |
