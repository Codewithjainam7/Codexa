# OpenAPI & Swagger API Reference Specification

Codexa provides interactive API documentation compliant with OpenAPI v3.1.0 and Swagger UI.

## Accessing Interactive Swagger UI

When running Codexa locally or in production, navigate to:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Raw OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`
- **Raw OpenAPI YAML Spec**: `http://localhost:8080/v3/api-docs.yaml`

## Core Endpoint Catalog

### 1. Ingestion & Analysis Lifecycle
- `POST /api/v1/analyze/repo`: Submit a remote Git repository URL with branch selector.
- `POST /api/v1/analyze/upload`: Upload a local `.zip` or `.tar.gz` codebase archive.
- `GET /api/v1/jobs/{jobId}`: Poll pipeline status, progress percentage, and live telemetry.
- `GET /api/v1/jobs/{jobId}/findings`: Query paginated deterministic findings with severity and category filters.

### 2. Multi-Format Report Exporter
- `GET /api/v1/jobs/{jobId}/export?format=sarif`: SARIF v2.1.0 report for GitHub Advanced Security.
- `GET /api/v1/jobs/{jobId}/export?format=pdf`: Comprehensive audit document for executive sign-off.
- `GET /api/v1/jobs/{jobId}/export?format=html`: Standalone interactive HTML report.
- `GET /api/v1/jobs/{jobId}/export?format=markdown`: Formatted PR comment summary.
- `GET /api/v1/jobs/{jobId}/export?format=json`: Complete raw inspection entity graph.
- `GET /api/v1/jobs/{jobId}/export?format=csv`: Spreadsheet export with RFC-4180 sanitization.

### 3. System Telemetry
- `GET /api/v1/health`: Cluster health check returning status, version, and active rule catalog count (30 rules).
