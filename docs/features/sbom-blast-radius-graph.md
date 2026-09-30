# Interactive Visual SBOM & Blast-Radius Dependency Graph

## 1. Overview
Modern cloud-native applications frequently consist of 80% to 90% open-source third-party dependencies. When a vulnerability like **Log4Shell (CVE-2021-44228)** or an insecure deserializer like **Jackson (CVE-2020-36518)** is announced, engineering leadership needs immediate visibility into:
1. Is this package present in our direct or transitive dependency tree?
2. Which upstream application modules and API endpoints are compromised?
3. What is the downstream **blast radius** of the vulnerability?
4. What is the minimum safe upgrade version that resolves the flaw without introducing breaking changes?

Codexa provides an **Interactive Visual Dependency & Blast-Radius Graph** (`SbomDependencyGraph.jsx`) integrated directly into the analysis results dashboard.

---

## 2. Blast Radius Calculation Formula

The platform computes the **Blast Radius Exposure Score ($B_r$)** for each evaluated package using a deterministic graph traversal metric:

$$B_r = \min\left(100, \; \text{CVSS} \times 5.0 + N_m \times 12.0 + T_w \times 10.0\right)$$

Where:
* $\text{CVSS}$ = Common Vulnerability Scoring System base score ($0.0 \dots 10.0$)
* $N_m$ = Number of direct upstream application classes/modules importing or calling the package
* $T_w$ = Direct vs. Transitive dependency weight ($T_w = 1.0$ for direct imports, $0.5$ for transitive imports)

---

## 3. Interactive Graph Capabilities

```
                  ┌──────────────────────┐
                  │ Target Ingestion App │
                  └──────────┬───────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   [spring-boot-starter-web]           [log4j-core 2.14.1]
   Severity: SAFE                      Severity: CRITICAL (95% Blast)
   License: Apache-2.0                 CVE-2021-44228
                                       Target: Upgrade to 2.22.1
```

* **Dynamic Risk Filter**: Filter packages by `All`, `Vulnerabilities Only`, `License Risks`, or `Clean / Secure`.
* **Instant Text Search**: Filter packages by name, group ID, or license string.
* **Blast Radius Gauge**: Visual exposure bar reflecting the proportion of affected application modules.
* **Remediation Target Recommendation**: Direct recommendation of the minimum secure release version.
* **Machine-Readable Export**: Generates compliant **CycloneDX v1.5** and **SPDX v2.3** JSON formats.
