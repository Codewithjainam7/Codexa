# CR-LIC-001: Viral Copyleft Open-Source License Detection (GPL / AGPL)

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-LIC-001` |
| **Category** | `OPERATIONS` / `LEGAL_RISK` |
| **Default Severity** | `HIGH` |
| **Legal Classification** | Strong Copyleft / Network Copyleft |
| **Target Licenses** | `GPL-2.0`, `GPL-3.0`, `AGPL-3.0`, `SSPL-1.0` |
| **Scanner Target** | `pom.xml`, `package.json`, `go.mod`, CycloneDX SBOM |

---

## 1. Compliance Risk Summary
Strong Copyleft licenses (such as GNU General Public License `GPL-2.0` / `GPL-3.0`) and Network Copyleft licenses (such as GNU Affero General Public License `AGPL-3.0`) mandate that any derivative work or linked application that incorporates their code must also be licensed under the exact same open-source terms. 

For commercial enterprises developing proprietary Software-as-a-Service (SaaS) or distributed enterprise software, introducing an AGPL or GPL dependency creates severe legal liability:
1. **Source Code Disclosure Requirement**: The company may be legally required to release its complete proprietary backend source code to the public.
2. **Loss of Intellectual Property (IP)**: Core proprietary algorithms and business logic lose patent and trade secret protections.
3. **M&A and Due Diligence Blockers**: Open-source license audits during acquisition or venture financing rounds flag copyleft licenses as high-severity legal liabilities.

---

## 2. Insecure Dependency Declaration

```xml
<!-- pom.xml VIOLATION: Incorporating an AGPL-3.0 library into a proprietary SaaS backend -->
<dependency>
    <groupId>org.gnu.example</groupId>
    <artifactId>agpl-reporting-core</artifactId>
    <version>2.4.1</version>
    <!-- License declared: GNU Affero General Public License v3.0 -->
</dependency>
```

---

## 3. Permissive Remediation Pattern

Replace strong copyleft dependencies with enterprise-friendly, permissive-licensed alternatives (such as **Apache-2.0**, **MIT**, **BSD-2-Clause**, **BSD-3-Clause**, or **ISC**):

```xml
<!-- REMEDIATION: Using an Apache 2.0 / MIT licensed alternative -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>1.11.0</version>
    <!-- License: Apache License, Version 2.0 (Permissive) -->
</dependency>
```

Alternatively, isolate the copyleft component into an independent external microservice communicating strictly across a generic network protocol (REST / gRPC), keeping proprietary code decoupled from the licensed process.

---

## 4. Permissive vs Copyleft Matrix

| License | Type | Commercial SaaS Friendly? |
| :--- | :--- | :--- |
| **MIT** | Permissive | ✅ Yes |
| **Apache 2.0** | Permissive (w/ Patent Grant) | ✅ Yes |
| **BSD-2 / BSD-3** | Permissive | ✅ Yes |
| **LGPL-3.0** | Weak Copyleft (Dynamic link OK) | ⚠️ Conditional |
| **GPL-3.0** | Strong Copyleft | ❌ Risk (Distributed Apps) |
| **AGPL-3.0** | Network Copyleft | ❌ Critical Risk (SaaS Backends) |

---

## 5. Verification Checklist
- [ ] No direct or transitive dependencies in CycloneDX SBOM resolve to `AGPL-3.0` or `GPL-3.0`.
- [ ] CI pipeline fails if a pull request introduces an unapproved copyleft license.
- [ ] Legal approval is required for any `LGPL` or dual-licensed dependency.
