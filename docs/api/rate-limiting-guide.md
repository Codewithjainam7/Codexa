# Codexa API Rate Limiting Guide

## Overview
Codexa protects internal and external compute resources by enforcing tiered rate limits across REST endpoints. Rate limits are computed using an in-memory sliding window counter backed by Caffeine cache (or Redis in clustered mode).

## Rate Limit Tiers

| Tier | Endpoints | Default Window | Request Quota | Burst Allowance |
| :--- | :--- | :--- | :--- | :--- |
| **Public Unauthenticated** | `/api/v1/health`, `/api/v1/meta` | 1 minute | 60 req/min | 10 req |
| **Authenticated Standard** | `/api/v1/analysis/*` | 1 minute | 300 req/min | 30 req |
| **Archive Ingestion** | `/api/v1/analysis/upload-zip` | 1 hour | 20 uploads/hr | 2 uploads |
| **AI Fallback Routing** | `/api/v1/analysis/ai-review` | 1 minute | 60 req/min | 5 req |

## HTTP Response Headers
When rate limits are engaged, all responses include standard RFC 6585 rate limiting headers:

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 284
X-RateLimit-Reset: 1726058400
```

When quota is exceeded, the server immediately returns `429 Too Many Requests`:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Quota exceeded for endpoint /api/v1/analysis. Reset in 42 seconds.",
  "retryAfterSeconds": 42,
  "timestamp": "2026-09-15T17:45:00Z"
}
```

## Client Implementation Best Practices
1. **Exponential Backoff**: When encountering `429`, honor the `Retry-After` header with jitter.
2. **Batch Ingestion**: Consolidate multi-file scans into single zip archives rather than parallel file uploads.
3. **Webhook Notifications**: Prefer asynchronous webhook callbacks over high-frequency status polling.
