# Codexa Security Defense-in-Depth Guide

## Threat Model & Mitigations
| Threat Vector | Mitigation Strategy | Implementation |
| :--- | :--- | :--- |
| **Zip Slip Traversal** | Canonical path validation against staging root | `SecureZipExtractor` |
| **Denial of Service (Zip Bomb)** | Max uncompressed file count (10,000) & file depth (15) | `SecureZipExtractor` |
| **SSRF (Internal Network Probe)** | Reject RFC 1918, RFC 3927, loopback addresses | `SsrfProtection` |
| **Credential Leakage in Reports** | High-entropy secret regex masking | `SecretMaskingSecurity` |
| **Cross-Site Scripting (XSS)** | Strict Content Security Policy & input sanitization | `SecurityHeadersFilter` |
| **Rate-Limit Evasion** | Sliding-window in-memory IP bucket limiter | `RateLimitingFilter` |
