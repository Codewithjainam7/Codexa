# Codexa 🛡️
> **AI-Assisted Code Review, Static Security Auditing & Production-Readiness Platform**

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vite + React](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite%205-blue.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-3.4-38bdf8.svg)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-76%20Passing%20(100%25)-emerald.svg)]()
[![Scale](https://img.shields.io/badge/Ingestion-3%20GB%20%7C%2050%2C000%20Files-blue.svg)]()

---

## 📌 Product Vision
The rapid adoption of AI coding assistants (GitHub Copilot, Cursor, Claude Code, etc.) has enabled high-velocity "vibe-coding" — codebases generated quickly that appear functional on the surface but often contain subtle OWASP Top 10 vulnerabilities, severe architectural debt, missing access controls, hardcoded secrets, and zero operational hardening.

**Codexa** answers the vital question: **"Can this code safely move toward production?"**

Users submit codebases via **ZIP archive** (scaled up to **3 GB** archives and **50,000 files**) or a **public GitHub repository URL**. Codexa safely stages untrusted code in an unprivileged container, executes deterministic Abstract Syntax Tree (AST) static analysis, computes comprehensive **White-Box AST structural complexity** and **Black-Box API attack surface mappings**, prioritizes issues with an explainable formula ($P = W_s \times W_c \times W_e \times W_i$), computes an explainable **Production Readiness Score (0–100)** across 5 dimensions, and delivers deep interactive visual diagnostics with downloadable executive audit reports in PDF, HTML, Markdown, and JSON.

> **⚠️ Advisory Disclaimer**: Codexa is an advisory static analysis and education platform, not a formal security certification. A clean scan does not guarantee the absence of all vulnerabilities. Suggested remediations must undergo human review and automated testing before production deployment.

---

## 🏗️ Architecture & Dual-Spectrum Inspection Engine

Codexa processes untrusted codebases through a multi-stage sequential pipeline with zero code execution and deterministic fallback guarantees:

```mermaid
flowchart TD
    A[Public GitHub URL / ZIP Upload (Up to 3GB)] --> B[Ingestion & Quota Guard Stage]
    B --> C[Parallel AST Parsing Stage (JavaParser 3.26+)]
    C --> D[Multi-Domain Deterministic Rule Engine (23+ Rules)]
    D --> E[Deep Project Diagnostics Collector (White-Box & Black-Box)]
    E --> F[Priority & 5-Dimension Readiness Scoring]
    F --> G[AI Remediation Engine (NVIDIA Nemotron / Offline Templates)]
    G --> H[Dual-Profile JPA Persistence & Flyway Migrations]
    H --> I[5-Tab Interactive React Dashboard & Executive Exporters]
```

### Key Architectural Tenets:
1. **Strict Zero Code Execution**: User-submitted code is never compiled, never dynamically executed, and never loaded into runtime classloaders. Analysis is 100% lexical and AST-based.
2. **High-Scale Ingestion Boundaries**:
   - **Upload Capacity**: Supports archives up to **3,072 MB (3.0 GB)** upload size, **3,500 MB** request payload, and **4,000 MB (4.0 GB)** uncompressed extracted capacity.
   - **File Scaling**: Analyzes up to **50,000 files** per repository with a 30-minute processing timeout.
   - **Zip Slip Defense**: Enforces canonical destination validation within isolated UUID staging directories.
   - **SSRF Prevention**: HTTPS-only transport, strict host allow-listing (`github.com`, `api.github.com`), redirect limits, and internal RFC 1918 / localhost / cloud metadata (169.254.169.254) blocking.
   - **DoS Rate Limiting**: In-memory token bucket sliding window per client IP address with trusted proxy validation.
   - **OWASP Security Response Headers**: Content-Security-Policy, X-Content-Type-Options: nosniff, X-Frame-Options: DENY, Referrer-Policy: strict-origin-when-cross-origin.
3. **Dual-Spectrum Testing Diagnostics**:
   - **White-Box AST Code Audit**: AST traversal, cyclomatic complexity calculations per method, maximum nesting depth, and top complex files refactoring leaderboard.
   - **Black-Box Attack Surface Simulation**: Automated discovery of exposed HTTP endpoints (`@GetMapping`, `@PostMapping`, Express, FastAPI), access control boundary check, and perimeter defense validation.
4. **Secret Redaction by Design**: Real tokens (AWS keys, GitHub tokens, OpenAI keys, JWTs, DB passwords) are masked in-flight before storage, display, or prompt transmission to LLMs.
5. **Hardened Production Profile**:
   - Non-root user execution (`USER codexa`) in Docker runtime.
   - Strict CORS origin allow-list scoped to `/api/**`.
   - Actuator locked down to `health,info` with authorized detail access.
   - Fail-fast production credentials with Flyway baseline migrations (`V1__baseline.sql`).
   - Constant-time API Key authentication filter (`ApiKeyAuthFilter`).

---

## 📊 Detailed Project Analysis Dashboard

The Codexa inspection interface provides an interactive, 5-tab deep diagnostic suite:
- **Executive Overview**: High-level readiness verdict, 5-dimension scorecard, stacked code composition progress bar (Java, TypeScript, Python, SQL, Config), and pre-deployment readiness checklist.
- **Findings & Triage**: Severity and category filter bar, repository file tree explorer, expandable finding cards with masked evidence, side-by-side remediation code diffs, and 1-click copy patch button.
- **White-Box AST Code Audit**: Average and peak cyclomatic complexity meters, max AST nesting depth, structural declaration tallies, and top complex files refactoring leaderboard.
- **Black-Box Attack Surface**: Complete inventory of exposed HTTP routes (method, route path, controller class, auth boundary check, and attack surface risk rating) alongside perimeter status checks.
- **OWASP & Compliance Matrix**: Visual distribution across OWASP Top 10 (2021) categories, Common Weakness Enumerations (CWE), and regulatory alignment.

---

## 📐 Scoring Formula & Readiness Verdicts

### Issue Priority Score ($P$)
Every detected finding receives an explainable priority score:
$$\text{Priority Score } (P) = W_{\text{severity}} \times W_{\text{confidence}} \times W_{\text{exploitability}} \times W_{\text{impact}}$$

* **Severity Weights ($W_s$)**: `CRITICAL: 1.0`, `HIGH: 0.8`, `MEDIUM: 0.5`, `LOW: 0.2`
* **Confidence Weights ($W_c$)**: `CONFIRMED: 1.0`, `HIGH: 0.8`, `MEDIUM: 0.6`, `SUSPECTED: 0.4`
* **Exploitability ($W_e$)**: Scaled $0.1 - 1.0$ based on user input reachability.
* **Impact ($W_i$)**: Scaled $0.1 - 1.0$ based on blast radius (RCE/data exfiltration vs stylistic).

### 5-Dimensional Production Readiness Score (0–100)
$$\text{Readiness Score} = (\text{Security} \times 0.60) + (\text{Quality} \times 0.25) + (\text{Operations} \times 0.15)$$
- **Maintainability Index**: Refactoring overhead derived from cyclomatic complexity and nesting depth.
- **Architectural Health**: Separation of concerns, controller isolation, and boundary integrity.

### Hard Verdict Overrides:
* Any **CRITICAL** finding $\implies$ Verdict capped at **`NOT_READY`** (Score $\le 40$).
* Any **HIGH** finding $\implies$ Verdict capped at **`NEEDS_URGENT_FIXES`** (Score $\le 65$).
* Clean scans $\implies$ **`REVIEW_COMPLETE`** ($80 - 100$).

---

## 🛡️ Static Rule Catalog (23+ Rules)

| Rule ID | Name | Category | Severity | OWASP Top 10 |
| :--- | :--- | :--- | :--- | :--- |
| `CR-SQL-001` | SQL Injection via Unsanitized Concatenation | SECURITY | CRITICAL | A03:2021-Injection |
| `CR-CMD-001` | Command Injection & Process Execution | SECURITY | CRITICAL | A03:2021-Injection |
| `CR-SEC-005` | Insecure Object Deserialization (ObjectInputStream) | SECURITY | CRITICAL | A08:2021-Software and Data Integrity |
| `CR-SEC-001` | Hardcoded Cryptographic Secrets & API Keys | SECURITY | HIGH | A07:2021-Identification & Auth Failures |
| `CR-AUTH-001`| Missing Access Control on Endpoints | SECURITY | HIGH | A01:2021-Broken Access Control |
| `CR-XSS-001` | Cross-Site Scripting (XSS) in Controllers | SECURITY | HIGH | A03:2021-Injection |
| `CR-SEC-003` | Path Traversal & Arbitrary File Access | SECURITY | HIGH | A01:2021-Broken Access Control |
| `CR-SEC-004` | Server-Side Request Forgery (SSRF) | SECURITY | HIGH | A10:2021-Server-Side Request Forgery |
| `CR-SEC-006` | CSRF & State Mutation in Safe GET Endpoints | SECURITY | HIGH | A01:2021-Broken Access Control |
| `CR-PASS-001`| Weak Password Hashing (MD5 / SHA-1) | SECURITY | HIGH | A02:2021-Cryptographic Failures |
| `CR-CRYPTO-001`| Weak Cryptography (ECB / Insecure PRNG) | SECURITY | MEDIUM | A02:2021-Cryptographic Failures |
| `CR-LOG-001` | Sensitive Data Logging & Token Leakage | SECURITY | MEDIUM | A09:2021-Security Logging & Monitoring |
| `CR-CONFIG-001`| Insecure CORS & Permissive Configuration | SECURITY | MEDIUM | A05:2021-Security Misconfiguration |
| `CR-DEP-001` | Supply Chain & Outdated Dependency Risk | SECURITY | MEDIUM | A06:2021-Vulnerable Components |
| `CR-QUAL-001`| High Cyclomatic Complexity ($CC > 15$) | QUALITY | MEDIUM | Maintainability |
| `CR-QUAL-002`| Long Method Code Smell ($> 50$ lines) | QUALITY | LOW | Clean Architecture |
| `CR-QUAL-003`| Deep Block Nesting ($> 4$ levels) | QUALITY | LOW | Code Readability |
| `CR-QUAL-004`| Duplicate Logic / Copy-Paste Blocks | QUALITY | LOW | DRY Principle |
| `CR-QUAL-005`| Direct Controller Persistence Access | QUALITY | MEDIUM | Layered Architecture |
| `CR-QUAL-006`| Swallowed / Broad Exception Handling | QUALITY | MEDIUM | Robust Error Handling |
| `CR-OPS-002` | Unstructured / Missing Request Logging | OPERATIONS | LOW | Observability |
| `CR-OPS-004` | Missing Input Validation on `@RequestBody` | OPERATIONS | MEDIUM | Defensive Coding |
| `CR-MULTI-001`| Universal Multi-Language Security & Quality | UNIVERSAL | HIGH | Multi-Language Scan |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java**: OpenJDK 17 or 21
- **Node.js**: 18+ or 20+ (with npm)
- **Maven**: 3.9+

### 1. Build and Test Backend
```bash
cd backend
mvn clean test
mvn spring-boot:run
```

### 2. Build and Run Frontend
```bash
cd frontend
npm install
npm run dev
```

Visit `http://localhost:5173` to access the Codexa dashboard.

---

## 📄 License & Governance
Codexa is licensed under the [Apache License 2.0](LICENSE).  
For vulnerability disclosures, see [SECURITY.md](SECURITY.md).  
For developer contributions, see [CONTRIBUTING.md](CONTRIBUTING.md).
