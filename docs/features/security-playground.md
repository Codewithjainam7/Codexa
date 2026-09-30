# Security Playground & Code Sandbox Architecture

## 1. Overview
The Codexa **Security Playground** (`/playground`) is an interactive, browser-based static analysis sandbox engineered to provide immediate feedback on vulnerable source code patterns across Java, Python, JavaScript, and Infrastructure-as-Code (Dockerfile). 

Rather than requiring a full repository upload or Git clone, developers and security engineers can test individual code snippets, observe live AST rule violations, inspect CWE taxonomy classifications, and preview deterministic 1-click AI secure refactors.

---

## 2. Architectural Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser Client (React)                   │
├──────────────────────────────┬──────────────────────────────┤
│  Monaco-Style Code Editor    │  Live AST Evaluation Panel   │
│  - Line-numbered text buffer │  - Rule ID (e.g. CR-SEC-001) │
│  - Multi-language presets    │  - CWE Taxonomy & Severity   │
│  - Reset / Copy / Modify     │  - Line-level vulnerability  │
├──────────────────────────────┴──────────────────────────────┤
│                                                             │
│                [ ✨ 1-Click AI Secure Refactor ]             │
│        (Patches left editor state with hardened code)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Preset Archetype Library
The sandbox includes pre-configured vulnerability archetypes demonstrating real-world vulnerabilities and their deterministic remediations:

| Preset Name | Language | Rule ID | CWE | Remediation Pattern |
| :--- | :--- | :--- | :--- | :--- |
| **SQL Injection** | Java | `CR-SEC-001` | CWE-89 | Parameterized `PreparedStatement` query binding |
| **Prompt Injection** | Python | `CR-LLM-001` | CWE-20 | Structural role separation (`system` vs `user` messages) |
| **SSRF Webhook** | JavaScript | `CR-SEC-004` | CWE-918 | Private IP validation & AWS metadata (`169.254.169.254`) blocking |
| **Dockerfile Privilege** | Dockerfile | `CR-IAC-001` | CWE-250 | Pinned Alpine image tag & explicit `USER node` directive |
| **Hardcoded Secrets** | Java | `CR-SEC-007` | CWE-798 | Environment variable extraction via `System.getenv(...)` |

---

## 4. 1-Click AI Secure Refactor Mechanism
When a developer clicks the **"1-Click AI Secure Refactor"** action:
1. The client-side state engine identifies the active violation and corresponding security patch template.
2. The code in the editor is immediately updated with the hardened, parameterized implementation.
3. The finding card transitions to `Secure Fix Applied!`, confirming that the vulnerability has been neutralized according to enterprise coding standards.
