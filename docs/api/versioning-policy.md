# Codexa API Versioning Policy, Backward Compatibility & Deprecation Standards

This document establishes the official API versioning framework, backward compatibility guarantees, schema evolution rules, and RFC 8594 deprecation lifecycles for the **Codexa Platform**.

---

## 1. Versioning Scheme & URI Hierarchy

Codexa employs **URI Path Semantic Versioning** for all public REST API endpoints:

```
https://api.codexa.dev/api/v{MAJOR}/{resource}
```

- **Current Stable Version**: **`v1`** (`/api/v1/analyses`, `/api/v1/auth`, `/api/v1/config/limits`).
- **Internal / Preview Version**: **`v2-alpha`** (gated for experimental features such as continuous LSP streams and AST graph diffs).

### Architectural Rationale for URI Versioning:
1. **Explicit Infrastructure Routing**: Reverse proxies (NGINX, Envoy, AWS ALB) and API gateways can deterministically route traffic to distinct container worker pools based on path prefixes without inspecting request bodies or headers.
2. **Deterministic Caching**: Edge CDN caches and HTTP forward caches can key responses without relying on complex `Vary: Accept-Version` negotiation.
3. **Seamless Client Tooling**: cURL commands, CI/CD shell scripts, and third-party webhooks remain resilient and easy to audit.

---

## 2. Backward Compatibility Guarantees (`v1` Lifecycle)

Codexa guarantees that within a major release family (`v1.x.x`), all API updates remain strictly backward compatible with existing client code.

### Non-Breaking Changes (Permitted in Minor/Patch Updates):
- **Additive Fields**: Adding new fields to response JSON payloads. (Clients are expected to ignore unknown properties).
- **Optional Request Parameters**: Introducing optional query parameters or request body attributes with safe defaults.
- **New Endpoints**: Introducing brand new API routes (e.g., adding `/api/v1/analyses/{id}/metrics`).
- **Taxonomy Additions**: Adding new static analysis rules (`CR-NEW-001`) or non-destructive metadata properties.
- **Performance Optimizations**: Internal algorithmic enhancements that reduce latency without altering JSON schemas.

### Breaking Changes (Strictly Forbidden in `v1`):
- Removing or renaming existing JSON fields in responses.
- Changing the primitive type of a field (e.g. integer ID changed to UUID string).
- Changing the HTTP response status code for existing success scenarios (e.g. changing `HTTP 200 OK` to `HTTP 204 No Content`).
- Converting an optional request parameter or body property into a mandatory required field.
- Removing or altering the endpoint URI path.

---

## 3. Client Schema Resilience Best Practices

To insulate clients from non-breaking additive updates, all official SDKs and client applications must configure JSON deserializers to ignore unrecognized properties:

### Java / Jackson
```java
ObjectMapper mapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

### TypeScript / Zod
```typescript
import { z } from 'zod';

export const FindingSchema = z.object({
  ruleId: z.string(),
  severity: z.string(),
  filePath: z.string(),
  message: z.string(),
}).passthrough(); // Allow unknown additive fields without failing validation
```

### Python / Pydantic
```python
from pydantic import BaseModel, ConfigDict

class FindingResponse(BaseModel):
    model_config = ConfigDict(extra='ignore') # Ignore additive properties
    rule_id: str
    severity: str
    file_path: str
```

---

## 4. Formal Deprecation Protocol (RFC 8594 Standards)

When an endpoint, parameter, or response format is scheduled for eventual retirement, Codexa follows a strict four-stage deprecation process over a minimum **12-month notice window**:

```
+-----------------------------------------------------------------------------------+
|               Stage 1: RFC Announcement & Documentation Notice                    |
|   - Publish deprecation notice in CHANGELOG.md and Developer Portal               |
|   - Provide 1:1 migration guide to successor API format                           |
+------------------------------------------+----------------------------------------+
                                           | (Month 0)
                                           v
+-----------------------------------------------------------------------------------+
|               Stage 2: HTTP Deprecation & Sunset Header Emission                  |
|   - RFC 8594 standard headers injected into all responses on deprecated route     |
|   - Deprecation: @<timestamp>                                                     |
|   - Sunset: <RFC 1123 HTTP-date>                                                  |
|   - Link: </docs/api/migrations/v2>; rel="sunset"                                 |
+------------------------------------------+----------------------------------------+
                                           | (Month 6)
                                           v
+-----------------------------------------------------------------------------------+
|               Stage 3: Brownout Testing & Operational Warnings                    |
|   - 24-hour synthetic rate-limiting / warning headers on staging environments     |
|   - Direct outreach to customers generating traffic on deprecated endpoints        |
+------------------------------------------+----------------------------------------+
                                           | (Month 12)
                                           v
+-----------------------------------------------------------------------------------+
|               Stage 4: End-of-Life (EOL) & Sunset Removal                         |
|   - Deprecated route returns HTTP 410 GONE with JSON migration payload            |
|   - Endpoint removed in subsequent major release (v2.0.0)                         |
+-----------------------------------------------------------------------------------+
```

### RFC 8594 Standard Headers Example
```http
HTTP/1.1 200 OK
Content-Type: application/json
Deprecation: @1727289600
Sunset: Wed, 30 Sep 2026 23:59:59 GMT
Link: <https://codexa.dev/docs/api/v2-migration>; rel="sunset"; type="text/html"
Warning: 299 - "This endpoint is deprecated and will be removed on 2026-09-30. Please migrate to /api/v2/analyses."
```

### EOL Response (`HTTP 410 Gone`)
```json
{
  "errorCode": "API_ENDPOINT_RETIRED",
  "message": "The requested endpoint '/api/v1/legacy/jobs' was sunset on 2026-09-30 and is no longer available.",
  "migrationGuideUrl": "https://codexa.dev/docs/api/v2-migration",
  "successorEndpoint": "/api/v2/analyses"
}
```

---

## 5. Major Version Migration Matrix (`v1` to `v2` Roadmap)

| Feature Dimension | `v1` (Current Stable) | `v2` (Planned Architecture) | Migration Strategy |
|:---|:---|:---|:---|
| **Analysis Ingestion** | Full Archive ZIP / Remote Git clone | Incremental Git delta & Tree-sitter commit streaming | Support both endpoints in parallel for 12 months. |
| **Progress Streaming** | Polling & Server-Sent Events (SSE) | WebSocket duplex & gRPC Bidirectional Streams | SSE fallback provided for web browsers. |
| **Pagination Contract** | Spring Data Page (`page`, `size`) | Pure Keyset Cursor (`afterCursor`, `limit`) | Deprecation headers on numeric `page` parameter. |
| **Report Export** | Query parameter (`?format=sarif`) | Distinct sub-resources (`/reports/sarif`, `/reports/pdf`) | Transparent 301 Permanent Redirect on old URL. |
