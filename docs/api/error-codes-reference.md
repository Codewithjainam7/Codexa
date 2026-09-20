# Codexa Standard Error Codes Reference

Codexa returns machine-readable, structured error payloads adhering to RFC-7807 Problem Details for HTTP APIs whenever an ingestion or processing request fails.

---

## 1. Standard Error Response Envelope

```json
{
  "status": 400,
  "errorCode": "INVALID_GITHUB_URL",
  "message": "Must be a valid public GitHub HTTPS repository URL (e.g. https://github.com/owner/repo)",
  "path": "/api/v1/analyses/github",
  "timestamp": "2026-09-20T12:00:00Z"
}
```

---

## 2. Comprehensive Error Catalog

| Error Code | HTTP Status | Trigger Condition | Recommended Client Recovery |
| :--- | :--- | :--- | :--- |
| `INVALID_GITHUB_URL` | `400 Bad Request` | Supplied URL is not a valid HTTPS public GitHub repository URL. | Verify URL format: `https://github.com/{owner}/{repo}` without auth tokens. |
| `SSRF_BLOCKED` | `400 Bad Request` | Target hostname resolves to private, loopback (`127.0.0.1`), or cloud metadata IPs. | Only public internet hostnames are allowed. |
| `EMPTY_FILE` | `400 Bad Request` | Uploaded ZIP archive contains 0 bytes or zero recognized files. | Ensure ZIP archive is non-empty and contains valid source files. |
| `INVALID_FILE_TYPE` | `400 Bad Request` | File content is not a valid PK-header ZIP stream. | Upload a standard ZIP archive compressed with DEFLATE or STORE. |
| `ARCHIVE_TOO_LARGE` | `413 Payload Too Large` | Decompressed size exceeds 3.0 GB or file count exceeds 50,000 files. | Exclude build artifacts (`node_modules/`, `target/`, `.git/`) and re-compress. |
| `ZIP_SLIP_ATTEMPT` | `400 Bad Request` | Archive entry contains relative path traversal tokens (`../`). | Re-package archive using standard non-malicious archiving utilities. |
| `ZIP_BOMB_DETECTED` | `400 Bad Request` | Compression ratio exceeds 100:1 (decompression expansion attack). | Remove deeply compressed sparse files. |
| `RATE_LIMIT_EXCEEDED`| `429 Too Many Requests` | IP address exceeded sliding window token bucket rate limit. | Check `Retry-After` header and backoff requests. |
| `JOB_NOT_FOUND` | `404 Not Found` | Requested analysis UUID does not exist or expired from storage. | Confirm analysis UUID or re-submit analysis job. |
| `INTERNAL_ERROR` | `500 Internal Server Error` | Unexpected runtime error during analysis execution. | Check server logs or submit bug report with Job UUID. |
