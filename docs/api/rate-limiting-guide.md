# Codexa API Rate Limiting Guide

Codexa protects backend compute resources, AST parser memory pools, and external AI APIs by enforcing defensive rate limits across all ingress REST endpoints.

---

## 1. Rate Limit Architecture & Algorithms

Codexa implements a sliding-window token bucket algorithm managed by `RateLimitingFilter.java`:
- **Granularity**: Client IP address (`X-Forwarded-For` with trusted reverse proxy CIDR validation or remote socket address).
- **Backend Storage**: High-concurrency in-memory bucket store with automatic TTL eviction.
- **Fair-Share Queueing**: Prevents noisy-neighbor starvation by throttling bursty ZIP archive submissions.

---

## 2. Endpoint Rate Limit Tiers

| Tier | Matching Endpoints | Request Limit | Burst Allowance |
| :--- | :--- | :--- | :--- |
| **System & Health** | `/actuator/health`, `/api/v1/health` | 120 req/min | 20 req |
| **Archive Ingestion (ZIP)** | `POST /api/v1/analyses/zip` | 10 uploads/min | 2 concurrent |
| **GitHub Ingestion** | `POST /api/v1/analyses/github` | 15 repos/min | 3 concurrent |
| **Status Polling & Metrics** | `GET /api/v1/analyses/{jobId}` | 300 req/min | 50 req |
| **Report Export Downloads** | `GET /api/v1/analyses/{jobId}/reports/*` | 60 req/min | 10 req |

---

## 3. Standard HTTP Response Headers

All API responses include RFC-compliant rate limiting telemetry:

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 294
X-RateLimit-Reset: 1726876800
```

When quota is exhausted, Codexa responds with `429 Too Many Requests` along with a `Retry-After` header:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/problem+json
Retry-After: 15

{
  "status": 429,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded for IP: 198.51.100.42 on endpoint: /api/v1/analyses/zip",
  "retryAfterSeconds": 15,
  "timestamp": "2026-09-20T12:00:00Z"
}
```

---

## 4. Client Implementation: Jittered Exponential Backoff

### Node.js (Fetch)

```javascript
async function fetchWithBackoff(url, options = {}, retries = 3) {
  for (let i = 0; i < retries; i++) {
    const res = await fetch(url, options);
    if (res.status !== 429) return res;
    
    const retryAfter = parseInt(res.headers.get("Retry-After") || "5", 10);
    const jitter = Math.random() * 1000;
    const delay = (retryAfter * 1000) + jitter;
    
    console.warn(`[Codexa] Rate limited (429). Retrying in ${delay.toFixed(0)}ms...`);
    await new Promise(r => setTimeout(r, delay));
  }
  throw new Error("Max retries exceeded due to rate limiting.");
}
```

### Python (Requests)

```python
import time
import random
import requests

def request_with_backoff(url, max_retries=3, **kwargs):
    for attempt in range(max_retries):
        resp = requests.get(url, **kwargs)
        if resp.status_code != 429:
            return resp
        retry_after = int(resp.headers.get("Retry-After", 5))
        sleep_duration = retry_after + random.uniform(0.1, 1.0)
        time.sleep(sleep_duration)
    raise RuntimeError("Exceeded maximum rate limit retries.")
```
