# REST API Error Code Reference

Standard error response structures emitted by the Codexa API Gateway.

## Error Response Format

```json
{
  "timestamp": "2026-09-11T17:30:00Z",
  "status": 404,
  "errorCode": "CDX-404-001",
  "message": "Analysis session expired or job not found.",
  "path": "/api/v1/jobs/00000000-0000-0000-0000-000000000000"
}
```

## Standard Error Code Catalog

| HTTP Status | Error Code | Description |
| :--- | :--- | :--- |
| `400 Bad Request` | `CDX-400-001` | Invalid repository URL format or unsupported protocol |
| `400 Bad Request` | `CDX-400-002` | Uploaded archive exceeds maximum size limit (500 MB) |
| `404 Not Found` | `CDX-404-001` | Job ID not found or purged by retention policy |
| `429 Too Many Requests` | `CDX-429-001` | Rate limit exceeded (default: 60 requests/minute) |
| `500 Internal Error` | `CDX-500-001` | Uncaught pipeline execution exception |

---

## Codexa CLI Exit Codes & CI/CD Status Signals

Codexa CLI communicates scan status and gate policies to CI/CD orchestrators (GitHub Actions, GitLab CI, Jenkins, CircleCI) via standard POSIX exit status codes:

| Exit Code | Signal / Name | Meaning & Trigger Condition | Action for CI Runner |
| :---: | :--- | :--- | :--- |
| **`0`** | `SUCCESS` | Scan succeeded. All security policy thresholds and score minimums satisfied. | Allow build / Merge pull request. |
| **`1`** | `GATE_FAILURE` | Security gate threshold breached (e.g. findings detected matching `--fail-on critical` or readiness score below `--min-score`). | Block merge / Fail pipeline stage. |
| **`2`** | `USAGE_ERROR` | Command-line syntax error, unknown command, or invalid option flag arguments. | Fix CI workflow invocation step. |
| **`127`** | `NOT_FOUND` | Missing command dependency (Node.js runtime or Git binary not found in `$PATH`). | Install Node.js runtime environment. |
| **`130`** | `SIGINT` | Process terminated prematurely by user interrupt (`Ctrl+C`). | Job cancelled by user. |

