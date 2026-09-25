# Codexa Authentication Tokens, API Keys & Lifecycle Architecture

This specification defines the token issuance standards, role-based access control (RBAC) scopes, cryptographic hashing, and lifecycle revocation protocols utilized by the **Codexa API**.

---

## 1. Token Taxonomy & Cryptographic Profiles

Codexa employs three distinct token classes tailored for distinct operational environments:

```
+-----------------------------------------------------------------------------------+
|                            Codexa Token Taxonomy                                  |
+-------------------+-------------------------------+-------------------------------+
|                   |                               |                               |
v                   v                               v
Personal Access (PAT)    Machine Service Account (SAT)      Ephemeral Session (JWT)
Prefix: `cdx_pat_`       Prefix: `cdx_svc_`                 Format: RFC 7519 Compact JWT
Scope: User/Developer    Scope: CI/CD Pipeline Runners       Scope: Interactive UI Session
TTL: 30 - 365 Days       TTL: 90 - 180 Days (Auto-rotate)   TTL: 15 Mins (Access) / 7 Days (Refresh)
```

### Prefix & Entropy Standards
All opaque tokens utilize cryptographic prefixes for instant identification and secret-scanning detection:
- **Prefix**: `cdx_pat_` (Personal Access Token) or `cdx_svc_` (Service Account Token).
- **Entropy Payload**: 32 bytes of cryptographically secure random bytes generated via `java.security.SecureRandom`, encoded as 64 hexadecimal characters.
- **Example**: `cdx_pat_7e9a2b5f8c1d3e4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a`

---

## 2. Storage Security: Zero-Plaintext Storage

To eliminate risk from database breaches, **plain-text tokens are displayed exactly once at creation time and never stored in persistent storage.**

Codexa stores only cryptographic digests:

$$\text{StoredHash} = \text{SHA-256}\Big(\text{Salt} \mathbin{\Vert} \text{RawToken}\Big)$$

- **Salt**: 16 bytes of cryptographically random salt per token.
- **Lookup Optimization**: A deterministic 8-character prefix index (`key_prefix = substring(token, 0, 16)`) enables $O(1)$ database index lookups before the full salted SHA-256 comparison.

---

## 3. RBAC Scopes & Permissions Matrix

Tokens are assigned granular permissions to enforce the principle of least privilege:

| Scope Identifier | Permitted Operations | Typical Role Assignment |
|:---|:---|:---|
| `analysis:create` | Submit zip archives and GitHub URLs for security scans (`POST /api/v1/analyses/*`). | CI/CD Runner (`cdx_svc_*`) |
| `analysis:read` | Inspect job status, progress percentage, and score summaries (`GET /api/v1/analyses/{id}`). | CI/CD Runner, Developer |
| `findings:read` | Read detailed finding AST snippets, line numbers, and CWE references. | Security Auditor, Developer |
| `report:export` | Download full SARIF v2.1.0, HTML, PDF, and Markdown audit reports. | CI Gating Script, Auditor |
| `remediation:ai` | Request contextual LLM refactoring patches and diff solutions. | Developer, IDE Extension |
| `admin:rules` | Enable/disable custom rules, register SPI plugins, adjust scoring weights. | Enterprise SecOps Admin |
| `admin:prune` | Trigger storage reclamation sweeps and database archival policies. | Systems Administrator |

---

## 4. Header Extraction & Authentication Protocol

Clients must pass tokens in standard HTTP headers:

### Primary Standard: Bearer Authentication
```http
GET /api/v1/analyses/ddaf0239-eee4-4032-80a0-60c9e4fe7078 HTTP/1.1
Host: api.codexa.dev
Authorization: Bearer cdx_pat_7e9a2b5f8c1d3e4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a
Accept: application/json
```

### Secondary Alternative: Custom Header (CLI / Webhooks)
```http
X-Codexa-Api-Key: cdx_svc_112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00
```

### Spring Security Filter Processing Order:
1. `TokenAuthenticationFilter` extracts token from `Authorization` or `X-Codexa-Api-Key`.
2. Computes SHA-256 against index `key_prefix`.
3. Validates expiration timestamp (`expires_at > Instant.now()`).
4. Checks Revocation Bloom Filter (`revocationCache.contains(tokenId)`).
5. Populates Spring `SecurityContextHolder` with `CodexaUserPrincipal` and assigned `GrantedAuthority` scopes.

---

## 5. Token Lifecycle & Emergency Revocation

### Token Creation Request (`POST /api/v1/auth/tokens`)
```http
POST /api/v1/auth/tokens HTTP/1.1
Host: api.codexa.dev
Authorization: Bearer <EXISTING_ADMIN_OR_USER_TOKEN>
Content-Type: application/json

{
  "name": "GitHub Actions Main CI Runner",
  "tokenType": "SERVICE_ACCOUNT",
  "scopes": ["analysis:create", "analysis:read", "report:export"],
  "expiresInDays": 90
}
```

#### Response (`HTTP 201 Created` - Token shown ONCE)
```json
{
  "tokenId": "tok_99120485",
  "name": "GitHub Actions Main CI Runner",
  "token": "cdx_svc_a8f9c1d2e3b4a5c6d7e8f90123456789abcdef0123456789abcdef0123456789",
  "scopes": ["analysis:create", "analysis:read", "report:export"],
  "expiresAt": "2026-12-24T23:59:59Z",
  "createdAt": "2026-09-25T17:50:00Z"
}
```

### Emergency Revocation (`DELETE /api/v1/auth/tokens/{tokenId}`)
```http
DELETE /api/v1/auth/tokens/tok_99120485 HTTP/1.1
Host: api.codexa.dev
Authorization: Bearer <ADMIN_TOKEN>
```

- When revoked, the `tokenId` is immediately added to the in-memory Guava/Redis Bloom Filter.
- Any subsequent request bearing the revoked token is rejected within $< 0.1\text{ ms}$ with `HTTP 401 UNAUTHORIZED: TOKEN_REVOKED`.

---

## 6. Audit Logging & Compliance Record

Every authentication event emits an immutable structured audit log:
```json
{
  "timestamp": "2026-09-25T17:50:12.418Z",
  "event": "API_AUTHENTICATION_SUCCESS",
  "tokenId": "tok_99120485",
  "tokenType": "SERVICE_ACCOUNT",
  "clientIp": "198.51.100.42",
  "userAgent": "Codexa-CLI/1.3.0",
  "endpoint": "/api/v1/analyses/zip",
  "method": "POST",
  "responseCode": 202
}
```
