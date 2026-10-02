# FedRAMP Moderate Security Baseline Technical Control Mapping

## 1. Overview
The Federal Risk and Authorization Management Program (FedRAMP) establishes standardized security requirements for cloud services used by US Federal agencies.

---

## 2. NIST SP 800-53 Rev. 5 Control Mapping

| FedRAMP Control | Name | Codexa Automated Verification |
| :--- | :--- | :--- |
| **AC-3** | Access Enforcement | Method-level authorization check on API endpoints (`CR-API-003`) |
| **IA-2** | Identification and Authentication | Enforces multifactor session verification and OAuth2 PKCE |
| **SC-8** | Transmission Confidentiality | Enforces TLS 1.3 encryption across all network boundaries (`CR-SEC-008`) |
| **SC-28** | Protection of Information at Rest | Enforces AES-256 storage volume encryption (`CR-IAC-006`) |
| **SI-2** | Flaw Remediation | Automated vulnerability triage and patch recommendation |
