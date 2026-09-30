# Multi-Engine Threat Modeling & Surface Discovery

## 1. Overview
Codexa integrates two complementary security analysis approaches:
1. **White-Box AST Static Analysis**: Inspects internal code structures, data-flow sinks, exception blocks, and cryptographic usage.
2. **Black-Box Surface & Endpoint Discovery**: Dissects public perimeter ingress, HTTP controller routing annotations (`@GetMapping`, `@PostMapping`), authentication filters, and exposed service ports.

By synthesizing both views, Codexa produces an accurate **Attack Surface Threat Model** without requiring active network penetration testing or runtime deployment.

---

## 2. Endpoint Discovery Pipeline

The discovery engine parses controller classes to extract:
* **HTTP Method**: GET, POST, PUT, DELETE, PATCH
* **URI Path**: Normalized path including class-level and method-level mappings (e.g. `/api/v1/users/{id}`)
* **Authentication Requirement**: Evaluates if the method or class is guarded by `@PreAuthorize`, `@Secured`, or falls under open public ingress
* **Attack Surface Risk Score**: Computed based on whether input parameters accept complex JSON payloads and whether state-mutating actions require administrative privileges

---

## 3. Cross-Correlation with AST Violations
When an AST violation (such as an unsanitized SQL query or insecure deserialization call) occurs inside a method linked to a **Public Ingress (Unauthenticated)** endpoint, the scoring formula applies an **Exposed Perimeter Multiplier ($W_e = 1.5$)**, elevating the priority of the finding for immediate remediation.
