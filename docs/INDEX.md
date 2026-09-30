# Codexa Comprehensive Documentation Index

Welcome to the definitive documentation portal for **Codexa: Deterministic AST Code Review & Production Readiness Platform**.

---

## 1. Features & User Experience
* [Security Playground & Code Sandbox](features/security-playground.md)
* [Interactive Visual SBOM & Blast-Radius Graph](features/sbom-blast-radius-graph.md)
* [Attack Surface Threat Map & Architectural Perimeter](features/attack-surface-threat-map.md)
* [Executive Dynamic Vector Trust Badges](features/executive-trust-badges.md)

---

## 2. Developer Extensions & Plugins
* [Chrome & Chromium Browser Extension](integrations/chrome-extension.md)
* [Visual Studio Code Extension](integrations/vscode-extension.md)
* [JetBrains IntelliJ IDEA Plugin](integrations/intellij-plugin.md)

---

## 3. Rules Catalog & Standards
### Infrastructure-as-Code (IaC)
* [CR-IAC-001: Dockerfile Root Execution](rules/CR-IAC-001.md)
* [CR-IAC-002: Dockerfile Untagged Base Image (:latest)](rules/CR-IAC-002.md)
* [CR-IAC-003: Kubernetes Privileged Container Escalation](rules/CR-IAC-003.md)
* [CR-IAC-005: Terraform Unrestricted Ingress (0.0.0.0/0)](rules/CR-IAC-005.md)
* [CR-IAC-006: Terraform Unencrypted Storage Volumes](rules/CR-IAC-006.md)

### OWASP API Security Top 10 (2023)
* [CR-API-001: Mass Assignment & BOPLA](rules/CR-API-001.md)
* [CR-API-002: Unrestricted Resource Consumption & Pagination](rules/CR-API-002.md)
* [CR-API-003: Broken Function Level Authorization (BFLA)](rules/CR-API-003.md)

### OWASP Top 10 for Large Language Models (LLMs)
* [CR-LLM-001: Direct Prompt Injection](rules/CR-LLM-001.md)
* [CR-LLM-002: Insecure Output Handling & Sinks](rules/CR-LLM-002.md)

### Open-Source Legal Compliance
* [CR-LIC-001: Viral Copyleft License Detection (GPL/AGPL)](rules/CR-LIC-001.md)

---

## 4. Enterprise Regulatory Compliance
* [SOC 2 Type II Audit Readiness Guide](compliance/soc2-type2-audit-guide.md)
* [ISO/IEC 27001:2022 Annex A Controls Mapping](compliance/iso27001-annex-a.md)
* [PCI-DSS v4.0 Secure Software Lifecycle Mapping](compliance/pci-dss-v4.md)
* [Comprehensive Enterprise Compliance Matrix](compliance/compliance-matrix.md)

---

## 5. Architecture & Internal Systems
* [Deterministic AST Parser Engine & Rule Lifecycle](architecture/ast-parser-engine.md)
* [Multi-Engine Threat Modeling & Surface Discovery](architecture/multi-engine-threat-modeling.md)
* [Production Readiness Scoring Formula](architecture/scoring-formula.md)
* [SBOM & CVE Dependency Scanner](architecture/sbom-cve-scanner.md)

---

## 6. Operations & Cloud Deployment
* [Render Production Operations Runbook](operations/render-production-runbook.md)
* [Docker Container Setup Guide](deployment/docker-setup.md)
* [Production Deployment Checklist](deployment/production-checklist.md)
