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
