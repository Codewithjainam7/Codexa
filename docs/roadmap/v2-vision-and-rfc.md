# Codexa v2.0 Architecture Vision & RFC Specification

This Request for Comments (RFC) outlines the strategic roadmap, architectural evolution, and technical vision for **Codexa v2.0**.

---

## 1. Executive Vision

While Codexa v1.x establishes an autonomous pre-deployment static analysis and production readiness gatekeeper, **Codexa v2.0** transitions the platform toward a **continuous, developer-native AppSec companion**.

---

## 2. Core Pillars of Codexa v2.0

### A. IDE Language Server Protocol (LSP) Extensions
- Build unified Language Server Protocol (LSP) bindings in Rust / TypeScript.
- Provide real-time squiggly-line warnings in **VS Code**, **Cursor**, and **JetBrains** IDEs as developers write code.
- One-click "Apply Codexa AI Patch" diff insertion directly in editor buffers before commits occur.

### B. Declarative Enterprise Rule DSL
- Provide a lightweight, declarative YAML/JSON rule authoring syntax:
  ```yaml
  rule:
    id: ORG-SEC-001
    name: Mandatory Encryption in S3 Buckets
    severity: HIGH
    match:
      language: terraform
      pattern:
        resource: "aws_s3_bucket"
        lacks: "server_side_encryption_configuration"
  ```
- Empowers enterprise DevSecOps teams to enforce custom compliance standards without modifying JavaParser visitor classes.

### C. Hybrid DAST & Fuzzing Correlation
- Cross-correlate static Black-Box route discovery with automated dynamic API contract fuzzing against staging environments.
- Verify whether statically detected unauthenticated routes (`CR-AUTH-001`) or SQL parameters (`CR-SQL-001`) are exploitable over active HTTP wire protocols.
