# ADR-0005: Standardization on OASIS SARIF v2.1.0 for Security Reporting

## Status
**Accepted** (2026-09-05)

## Context & Problem Statement
Security auditing tools traditionally invented proprietary JSON schemas for their finding outputs. This forced DevSecOps teams to build custom parser scripts, converters, and fragile data extractors to import results into CI/CD security dashboards (e.g. GitHub Code Scanning, GitLab Security Dashboard, SonarQube, VS Code SARIF Viewer).

Codexa required an output format that is immediately ingestible by industry-standard security platforms without bespoke middleware or glue code.

## Decision Drivers
- **Ecosystem Interoperability**: Direct integration with GitHub Security Alerts (`github/codeql-action/upload-sarif`), GitLab, and SonarQube.
- **IDE Native Support**: Allowing developers to view Codexa findings directly in VS Code, IntelliJ, and Eclipse using standard SARIF viewer extensions.
- **Specification Maturity**: Relying on an internationally recognized, formally specified OASIS open standard.

## Considered Options
1. **Custom Proprietary JSON Schema**: Simple to implement, but requires bespoke integrations and provides zero out-of-the-box CI dashboard support.
2. **SonarQube Generic Issue Format**: Compatible with SonarQube, but unsupported by GitHub Code Scanning and IDE viewers.
3. **OASIS SARIF v2.1.0 (Static Analysis Results Interchange Format)**: The universal standard adopted by GitHub, Microsoft, US DHS, and OASIS.

## Decision Outcome
Chosen option: **OASIS SARIF v2.1.0 Standard**.

Codexa implements full compliance with the SARIF v2.1.0 specification:
- **Root Object**: Adheres to schema `https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json`.
- **Tool Driver Metadata**: Emits complete rule descriptions, help URIs, and MITRE CWE relationships.
- **Location Normalization**: Strips temporary container staging paths, providing clean repository-relative URIs with exact line and column ranges.
- **Dual Export API**: Available via `GET /api/v1/analyses/{id}/report?format=sarif` and integrated into the CLI runner.

## Consequences
- **Positive**: Native, zero-configuration rendering in GitHub Code Scanning PR checks and Security tab.
- **Positive**: Direct compatibility with SonarQube 10.x SARIF import and VS Code SARIF Viewer.
- **Positive**: Eliminates maintenance overhead of developing custom plugins for every third-party dashboard.
- **Neutral**: SARIF payloads are verbose compared to minimalist proprietary JSON, mitigated via gzip compression in REST responses.
