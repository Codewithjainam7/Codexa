# CR-SEC-004: Server-Side Request Forgery (SSRF)

### Overview
Detects outbound network requests constructed with dynamic user parameters without target domain allow-listing or IP address validation.

### Classification
- **Category:** SECURITY
- **Severity:** HIGH
- **OWASP Top 10:** A10:2021-Server-Side Request Forgery (SSRF)
- **CWE:** CWE-918 (Server-Side Request Forgery)

### AST Pattern Detection Details
The rule inspects:
1. `ObjectCreationExpr` instantiating `URL` with dynamic string concatenation.
2. `MethodCallExpr` targeting HTTP client methods (`getForObject`, `postForObject`, `exchange`, `send`, `uri`) where the URL argument is dynamically evaluated.

### Remediation
Validate outbound URLs against an explicit allow-list of permitted domains and reject connections to loopback (127.0.0.1) or cloud metadata services (169.254.169.254):
```java
URI uri = URI.create(targetUrl);
if (!ALLOWED_DOMAINS.contains(uri.getHost())) {
    throw new SecurityException("Untrusted outbound destination: " + uri.getHost());
}
```
