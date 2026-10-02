# Rate Limiting & Denial-of-Service Defense Architecture

## 1. Overview
To prevent brute-force attacks, API scraping, and resource exhaustion, Codexa enforces tiered rate limiting across all ingress endpoints.

---

## 2. Algorithms Implemented

### A. Token Bucket Algorithm
* **Characteristics**: Permits bursts of requests up to bucket capacity $B$, while replenishing tokens at a steady rate $R$ per second.
* **Use Case**: General API endpoints where occasional user bursts are natural (e.g. browsing findings).

### B. Leaky Bucket / Fixed Window
* **Characteristics**: Enforces a strict maximum number of operations per fixed time window.
* **Use Case**: High-cost asynchronous operations (e.g., ZIP archive unpacking, deep AST scans).

---

## 3. In-Memory Filter Configuration

The [`RateLimitingFilter`](file:///F:/Codexa/backend/src/main/java/com/codexa/security/filter/RateLimitingFilter.java) applies per-IP buckets:
* `/api/v1/analyses/zip`: Maximum 5 uploads per minute per IP.
* `/api/v1/analyses/github`: Maximum 10 repository clones per minute per IP.
* `/api/v1/analyses/{jobId}/findings`: Maximum 120 queries per minute per IP.

---

## 4. Response Headers
When rate limits are approached or exceeded, the server returns:
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1727914200
```
