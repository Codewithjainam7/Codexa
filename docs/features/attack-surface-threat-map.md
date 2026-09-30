# Attack Surface Threat Map & Architectural Perimeter

## 1. Overview
Traditional static analysis reports list findings in isolation as detached file paths and line numbers. The **Attack Surface Threat Map** (`AttackSurfaceMap.jsx`) contextualizes vulnerabilities into a coherent, multi-tier architectural flow diagram that reflects real-world threat modeling:

$$\text{Tier 1: Perimeter} \longrightarrow \text{Tier 2: Ingress API} \longrightarrow \text{Tier 3: Core Logic & AI} \longrightarrow \text{Tier 4: Data Sinks}$$

This allows security teams and architects to understand where the system's defenses fail across the request lifecycle.

---

## 2. Four Architectural Tiers

### Tier 1: Ingress & Perimeter
* **Components**: DNS, TLS termination, CDN, reverse proxies, and webhook receivers.
* **Overlaid Rules**: Disabled TLS certificate validation (`CR-SEC-008`), CORS wildcard misconfigurations (`CR-CORS-001`), and hardcoded internal IP addresses (`CR-SEC-009`).

### Tier 2: API Gateway & Controllers
* **Components**: Spring `@RestController`, JAX-RS resources, routing filters, and authentication gates.
* **Overlaid Rules**: Missing access control (`CR-AUTH-001`), Broken Function Level Authorization (`CR-API-003`), CSRF disabling (`CR-CSRF-001`), and BOPLA Mass Assignment (`CR-API-001`).

### Tier 3: Application Core & AI Logic
* **Components**: Service beans, business logic, asynchronous task executors, and LLM integrations.
* **Overlaid Rules**: Command Injection (`CR-CMD-001`), Insecure Deserialization (`CR-DESER-001`), LLM Prompt Injection (`CR-LLM-001`), and Unbounded Thread Pools (`CR-PERF-001`).

### Tier 4: Data Sinks & Cloud Resources
* **Components**: Relational databases (PostgreSQL/MySQL), Redis caches, AWS S3 buckets, and Terraform/Docker definitions.
* **Overlaid Rules**: SQL Injection (`CR-SQL-001`), Hardcoded AWS Secrets (`CR-SEC-007`), SSRF Sinks (`CR-SSRF-001`), and IaC Docker Root execution (`CR-IAC-001`).

---

## 3. Interactive Threat Inspection
Clicking any tier in the UI filters the vulnerability list to show only the findings relevant to that architectural layer, complete with file location, line number, and mitigation guidance.
