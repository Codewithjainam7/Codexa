# Codexa REST API & OpenAPI Specifications

Codexa provides high-performance REST APIs for continuous static security analysis, real-time pipeline monitoring, and multi-format report exports.

---

## 1. Primary Analysis Endpoints

### 1.1 Ingest Public GitHub Repository
- **Endpoint:** `POST /api/v1/analyses/github`
- **Content-Type:** `application/json`
- **Request Body:**
  ```json
  {
    "repoUrl": "https://github.com/owner/repository"
  }
  ```
- **Response:** `200 OK` / `202 Accepted`
  ```json
  {
    "id": "4fe10a8a-a1e2-4057-b7f6-f6aafa24764d",
    "sourceType": "GITHUB",
    "sourceIdentifier": "https://github.com/owner/repository",
    "status": "EXTRACTING",
    "progressStage": "QUEUED_FOR_DOWNLOAD",
    "progressPercent": 5,
    "createdAt": "2026-09-20T12:00:00Z"
  }
  ```

### 1.2 Ingest ZIP Archive
- **Endpoint:** `POST /api/v1/analyses/zip`
- **Content-Type:** `multipart/form-data`
- **Parameters:** `file` (Binary `.zip` archive, max 3.0 GB / 50,000 files)
- **Response:** `200 OK` / `202 Accepted`

### 1.3 Get Analysis Job Status & Metrics
- **Endpoint:** `GET /api/v1/analyses/{jobId}`
- **Response:** `200 OK`
  ```json
  {
    "id": "4fe10a8a-a1e2-4057-b7f6-f6aafa24764d",
    "sourceType": "GITHUB",
    "sourceIdentifier": "https://github.com/owner/repository",
    "status": "COMPLETED",
    "progressStage": "COMPLETED",
    "progressPercent": 100,
    "overallScore": 100.0,
    "verdict": "REVIEW_COMPLETE",
    "createdAt": "2026-09-20T12:00:00Z",
    "completedAt": "2026-09-20T12:00:15Z",
    "metrics": {
      "securityScore": 100.0,
      "qualityScore": 100.0,
      "operationsScore": 100.0,
      "maintainabilityScore": 100.0,
      "architecturalScore": 100.0,
      "totalFiles": 125,
      "analyzedFiles": 125,
      "criticalCount": 0,
      "highCount": 0,
      "mediumCount": 0,
      "lowCount": 0,
      "durationMs": 4850
    },
    "topFindings": []
  }
  ```

---

## 2. Findings & Export Endpoints

### 2.1 Paginated Findings Search
- **Endpoint:** `GET /api/v1/analyses/{jobId}/findings?page=0&size=20&severity=CRITICAL`
- **Query Parameters:**
  - `page`: Page index (default: `0`)
  - `size`: Items per page (default: `20`, max: `100`)
  - `severity`: Filter by `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`
  - `category`: Filter by `SECURITY`, `QUALITY`, `OPERATIONS`

### 2.2 Export Full Audit Report (JSON)
- **Endpoint:** `GET /api/v1/analyses/{jobId}/reports/json`
- **Response:** Application JSON audit report adhering to schema `https://codexa.dev/schemas/audit-report-v1.json`.

### 2.3 Export SARIF v2.1.0 (GitHub Code Scanning)
- **Endpoint:** `GET /api/v1/analyses/{jobId}/reports/sarif`
- **Content-Type:** `application/json`
- Compatible with GitHub Code Scanning alerts and SonarQube imports.

### 2.4 Export Markdown Executive Summary
- **Endpoint:** `GET /api/v1/analyses/{jobId}/reports/markdown`
- **Content-Type:** `text/markdown; charset=UTF-8`
