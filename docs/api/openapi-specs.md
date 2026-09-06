# Codexa REST API & OpenAPI Specifications

Codexa provides high-performance REST APIs for continuous static security analysis, real-time pipeline monitoring, and multi-format report exports.

---

## 1. Primary Analysis Endpoints

### 1.1 Ingest ZIP Archive
- **Endpoint:** `POST /api/v1/analysis/zip`
- **Content-Type:** `multipart/form-data`
- **Parameters:** `file` (Binary `.zip` archive, max 500 MB)
- **Response:** `202 Accepted`
  ```json
  {
    "id": "7b0d9124-7f13-432d-9477-7ff7b12ec099",
    "status": "QUEUED",
    "sourceType": "ZIP",
    "sourceIdentifier": "my-service.zip",
    "createdAt": "2026-09-07T00:00:00Z"
  }
  ```

### 1.2 Ingest Public GitHub Repository
- **Endpoint:** `POST /api/v1/analysis/github`
- **Content-Type:** `application/json`
- **Body:**
  ```json
  {
    "url": "https://github.com/owner/repository"
  }
  ```
- **Response:** `202 Accepted`

### 1.3 Poll Analysis Job Status
- **Endpoint:** `GET /api/v1/analysis/jobs/{jobId}`
- **Response:** `200 OK`
  ```json
  {
    "id": "7b0d9124-7f13-432d-9477-7ff7b12ec099",
    "status": "COMPLETED",
    "progressStage": "COMPLETED",
    "progressPercent": 100,
    "overallScore": 92.5,
    "verdict": "GENERALLY_PROMISING",
    "metrics": {
      "securityScore": 95.0,
      "qualityScore": 88.0,
      "operationsScore": 90.0,
      "maintainabilityScore": 91.5,
      "architecturalScore": 89.0,
      "totalFiles": 128,
      "analyzedFiles": 128,
      "criticalCount": 0,
      "highCount": 1,
      "mediumCount": 3,
      "lowCount": 5,
      "durationMs": 4200
    }
  }
  ```

### 1.4 Retrieve Full Audit Report
- **Endpoint:** `GET /api/v1/analysis/jobs/{jobId}/report`
- **Response:** `200 OK` (JSON Report Object)

---

## 2. Multi-Format Report Export Endpoints

| Format | Endpoint | Content-Type |
| :--- | :--- | :--- |
| **HTML** | `GET /api/v1/analysis/jobs/{jobId}/export/html` | `text/html; charset=UTF-8` |
| **Markdown** | `GET /api/v1/analysis/jobs/{jobId}/export/markdown` | `text/markdown; charset=UTF-8` |
| **JSON** | `GET /api/v1/analysis/jobs/{jobId}/export/json` | `application/json; charset=UTF-8` |
| **PDF** | Dedicated Client Print Dialog | `application/pdf` |

---

## 3. Limits & Platform Configuration

- **Endpoint:** `GET /api/v1/config/limits`
- **Response:**
  ```json
  {
    "maxZipSizeBytes": 524288000,
    "maxExtractedSizeBytes": 1048576000,
    "maxFileCount": 20000,
    "maxDirectoryDepth": 30,
    "maxSingleFileSizeBytes": 104857600
  }
  ```
