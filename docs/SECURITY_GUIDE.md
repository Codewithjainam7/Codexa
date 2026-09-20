# Codexa Security Defense-in-Depth Guide

Codexa processes untrusted source code submitted by external users and automated pipelines. To guarantee that analyzing hostile repositories never compromises the host infrastructure or leaks sensitive data, Codexa implements multi-layered security controls.

---

## 1. Threat Vectors & Defense Matrix

| Threat Vector | Mitigation Strategy | Enforcement Component |
| :--- | :--- | :--- |
| **Zip Slip Traversal** | Canonical path validation against staging root sandbox | `SecureZipExtractor.java` |
| **Zip Bomb Decompression DoS** | Enforce 50,000 file ceiling, 3GB size cap, and 100:1 ratio cap | `SecureZipExtractor.java` |
| **SSRF (Internal Network Probe)** | Reject RFC 1918, RFC 3927, loopback addresses & follow-redirect bans | `SsrfProtection.java` |
| **Secret Leakage in Audit Reports** | Redact high-entropy keys, passwords, and tokens before persistence | `SecretMasker.java` |
| **XSS & Injection in Web UI** | Strict Content Security Policy, zero raw HTML injection | `SecurityHeadersFilter.java` |
| **Denial of Service / API Flood** | Sliding-window token bucket IP rate limiting | `RateLimitingFilter.java` |
| **Host System Execution** | Zero code compilation, zero dynamic user execution | AST Static Analysis Engine |

---

## 2. Ingestion Sandboxing & Storage Boundaries

### 2.1 Randomized Sandbox Directories
Each analysis job allocates an isolated temporary filesystem hierarchy:
- Path pattern: `.staging/{uuid}/`
- Permissions: Owned by non-root application user (`chmod 700`)
- Lifetime: Automatically deleted upon completion or failure in a `finally` block.

### 2.2 Zip Slip & File Count Defenses
- Entries containing `..` or leading `/` are immediately rejected.
- Paths exceeding depth 15 are discarded with diagnostic warnings.
- Maximum decompressed size is capped at 3.0 GB across all archive members.

---

## 3. Server-Side Request Forgery (SSRF) Guard

When ingesting public repositories via URL:
1. **URL Scheme Enforcement**: Only `https://` URLs pointing to `github.com` are permitted.
2. **DNS & IP Filtering**: Resolves all IPv4 and IPv6 target addresses and blocks:
   - Loopback (`127.0.0.0/8`, `::1`)
   - Private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`)
   - Link-local and cloud metadata (`169.254.169.254`, `fe80::/10`)
   - Carrier-grade NAT (`100.64.0.0/10`)
3. **Redirect Disablement**: Disables automatic HTTP redirects to prevent open-redirect SSRF pivoting.

---

## 4. Evidence Secret Masking

Finding snippets and code evidence lines are passed through `SecretMasker.maskSecrets()` prior to saving:
- Hardcoded API keys (AWS, GitHub, Slack, OpenAI) are redacted as `[REDACTED_SECRET]`.
- Bearer tokens, passwords, and private keys are never stored in plain text or rendered in executive reports.

---

## 5. HTTP Security Headers

The `SecurityHeadersFilter` attaches hardened defensive headers to all HTTP responses:

```http
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Strict-Transport-Security: max-age=31536000; includeSubDomains
```
