# Codexa API Pagination & Query Filtering Specifications

This specification documents the pagination architectures, filtering semantics, Spring Data `Page<T>` response contracts, and cursor pagination strategies used across **Codexa REST API** endpoints.

---

## 1. Architectural Overview & Strategy Selection

Codexa provides access to large-scale static analysis datasets, including repositories with upwards of $50,000+$ distinct findings and multi-thousand commit histories. To balance ease of client integration with high-throughput database querying, Codexa supports two primary pagination models:

1. **Spring Data Offset Pagination (`page`, `size`)**:
   - Best suited for UI dashboards, data tables, and client integrations requiring random page navigation.
   - Built on Spring Data's `Pageable` abstraction with defensive parameter sanitization (`Math.min(Math.max(1, size), 5000)`).
2. **Deterministic Keyset (Cursor) Pagination (`afterCursor`, `limit`)**:
   - Recommended for high-volume automated CLI extraction, SARIF exports, and automated CI/CD pipeline workers.
   - Provides $O(1)$ database seek time, completely avoiding the quadratic degradation ($O(N)$) inherent in deep SQL `OFFSET` clauses.

---

## 2. Offset Pagination Specification (`/api/v1/analyses/{jobId}/findings`)

### Request Parameters

| Parameter | Type | Required | Default | Bounds | Description |
|:---|:---:|:---:|:---:|:---:|:---|
| `page` | Integer | No | `0` | $\ge 0$ | 0-indexed page number. Negative values are clamped to `0`. |
| `size` | Integer | No | `1000` | $1 \le \text{size} \le 5000$ | Number of findings per page. Values exceeding `5000` are capped to `5000`. |
| `category` | Enum | No | `null` | `SECURITY`, `QUALITY`, `PERFORMANCE`, `OPERATIONS`, `ARCHITECTURE` | Filter findings by taxonomy category. |
| `severity` | Enum | No | `null` | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO` | Filter findings by severity tier. |
| `confidence`| Enum | No | `null` | `HIGH`, `MEDIUM`, `LOW` | Filter findings by detection confidence. |
| `search` | String | No | `null` | Max 255 chars | Case-insensitive substring match against `ruleId`, `message`, `filePath`, or `snippet`. |

### Example Request
```http
GET /api/v1/analyses/ddaf0239-eee4-4032-80a0-60c9e4fe7078/findings?category=SECURITY&severity=CRITICAL&page=0&size=50 HTTP/1.1
Host: api.codexa.dev
Authorization: Bearer <API_TOKEN>
Accept: application/json
```

### Spring Data `Page<FindingResponse>` Response Schema
```json
{
  "content": [
    {
      "id": "fnd_88291a0c",
      "ruleId": "CR-SQL-001",
      "category": "SECURITY",
      "severity": "CRITICAL",
      "confidence": "HIGH",
      "filePath": "src/main/java/com/example/OrderDao.java",
      "startLine": 45,
      "endLine": 47,
      "message": "Potential SQL injection in concatenated query.",
      "snippet": "String sql = \"SELECT * FROM orders WHERE user_id = '\" + uid + \"'\";",
      "remediation": "Use PreparedStatement query parameter placeholders.",
      "deduplicationHash": "4a5c88b22e70c8a6791b920d3648e89547d0de0b0ecbe992cbb9352e8d35d259",
      "owaspMapping": "A03:2021-Injection"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 3,
  "totalElements": 128,
  "last": false,
  "first": true,
  "size": 50,
  "number": 0,
  "numberOfElements": 50,
  "empty": false
}
```

---

## 3. Cursor (Keyset) Pagination for Bulk Export

When streaming thousands of findings to automated consumers without incurring deep-offset performance penalties, the cursor endpoint utilizes an opaque, URL-safe Base64 token encoding the last observed primary key or timestamp:

$$\text{Cursor} = \text{Base64UrlEncode}\Big(\text{timestamp} \mathbin{\Vert} \text{uuid}\Big)$$

### Query Semantics:
```sql
-- Keyset seek query replaces expensive OFFSET
SELECT * FROM findings
WHERE job_id = :jobId
  AND (created_at, id) > (:cursorTimestamp, :cursorId)
ORDER BY created_at ASC, id ASC
LIMIT :limit;
```

### Response Envelope with Next Cursor:
```json
{
  "data": [...],
  "pagination": {
    "limit": 100,
    "hasMore": true,
    "nextCursor": "ZXlKaGJHY2lPaUpTVXpVeE5pSXNJblI1Y0NJNklrcFhWQ0o5",
    "prevCursor": null
  }
}
```

---

## 4. Performance Benchmarks: Offset vs Keyset Seeking

Empirical latency measurements executed on PostgreSQL 16 with $1,000,000$ indexed findings:

| Page Offset / Record Number | Offset SQL Latency (`LIMIT 50 OFFSET N`) | Keyset Seek Latency (`WHERE (ts, id) > cursor LIMIT 50`) | Performance Gain |
|:---:|:---:|:---:|:---:|
| Page 1 (Record 0) | $0.85\text{ ms}$ | $0.82\text{ ms}$ | $1.0\times$ |
| Page 100 (Record 5,000) | $3.40\text{ ms}$ | $0.88\text{ ms}$ | $3.9\times$ |
| Page 1,000 (Record 50,000) | $28.60\text{ ms}$ | $0.91\text{ ms}$ | $31.4\times$ |
| Page 10,000 (Record 500,000)| $245.00\text{ ms}$ | $0.94\text{ ms}$ | **$260.6\times$** |

---

## 5. Client Integration Quickstart (Python & TypeScript)

### TypeScript Fetch Paginator
```typescript
async function fetchAllFindings(jobId: string): Promise<Finding[]> {
  let allFindings: Finding[] = [];
  let page = 0;
  let isLast = false;

  while (!isLast) {
    const res = await fetch(`/api/v1/analyses/${jobId}/findings?page=${page}&size=500`);
    const data = await res.json();
    allFindings = allFindings.concat(data.content);
    isLast = data.last;
    page++;
  }
  return allFindings;
}
```

### Python Keyset Streamer
```python
import requests

def stream_findings(job_id: str, base_url: str = "http://localhost:8080"):
    cursor = None
    while True:
        params = {"limit": 250}
        if cursor:
            params["afterCursor"] = cursor
        res = requests.get(f"{base_url}/api/v1/analyses/{job_id}/findings/stream", params=params)
        res.raise_for_status()
        payload = res.json()
        
        for finding in payload["data"]:
            yield finding
            
        if not payload["pagination"]["hasMore"]:
            break
        cursor = payload["pagination"]["nextCursor"]
```
