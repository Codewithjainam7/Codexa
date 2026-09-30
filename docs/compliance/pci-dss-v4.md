# PCI-DSS v4.0 Secure Software Lifecycle Requirements

## 1. Overview
PCI-DSS (Payment Card Industry Data Security Standard) v4.0 mandates strict software development lifecycle (SDLC) security controls for all applications that store, process, or transmit cardholder data (CHD) or sensitive authentication data (SAD).

Codexa automated rule evaluation maps directly into **PCI-DSS Requirement 6: Develop and Maintain Secure Systems and Software**.

---

## 2. Requirement 6 Control Verification

| PCI-DSS v4.0 Requirement | Specification | Codexa Automated Enforcement |
| :--- | :--- | :--- |
| **Req 6.2.4** | Software development prevents common coding vulnerabilities | Scans for SQLi, XSS, Command Injection, and Deserialization (`CR-SQL-001`, `CR-XSS-001`, `CR-CMD-001`) |
| **Req 6.3.1** | Security vulnerabilities are identified and managed | Automated SBOM cataloging and CVE vulnerability blast radius analysis (`CR-SEC-002`) |
| **Req 6.3.2** | Bespoke software is reviewed prior to release | Automated PR review gate and deterministic readiness scoring ($P \ge 75$) |
| **Req 6.4.1** | Public-facing web applications are protected against attacks | Black-box exposed attack surface mapping and authorization verification (`CR-API-003`) |

---

## 3. Extracting the PCI-DSS Audit Packet

```http
GET /api/v1/analyses/{jobId}/compliance?standard=pci-dss
```
