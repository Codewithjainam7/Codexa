# Codexa Authentication Tokens & Lifecycle Guide

## Overview
Codexa supports multiple authentication models to accommodate CI/CD automation, CLI tooling, and web dashboards.

## Supported Token Types

### 1. Personal Access Tokens (PAT)
- Long-lived bearer tokens scoped to specific user permissions.
- Prefix format: `cdx_pat_<64_hex_chars>`.
- Use Case: Developer workstations, IDE plugins, and terminal scripts.

### 2. Machine Service Accounts
- Dedicated credentials for automated CI/CD runners (GitHub Actions, GitLab CI, Jenkins).
- Scoped strictly to read/write scan results for designated repositories.
- Prefix format: `cdx_svc_<64_hex_chars>`.

### 3. Ephemeral JWT Sessions
- Short-lived JSON Web Tokens (15-minute expiration) used by the web frontend.
- Signed via HMAC-SHA256 or RS256 with key rotation.

## Header Usage
Include the token in the `Authorization` HTTP header with the `Bearer` scheme:

```http
GET /api/v1/analysis/history HTTP/1.1
Host: codexa.local
Authorization: Bearer cdx_pat_9a4f210e7b...
```

## Token Rotation & Revocation
- Tokens can be instantly revoked through `/api/v1/auth/tokens/{tokenId}/revoke`.
- All revoked tokens are stored in the bloom-filter revocation cache with zero look-aside latency.
