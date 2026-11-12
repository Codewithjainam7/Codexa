# CR-HTTP-001: HTTP Request Smuggling & Hop-by-Hop Header Desync

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-HTTP-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-444: Inconsistent Interpretation of HTTP Requests](https://cwe.mitre.org/data/definitions/444.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | Java, JavaScript, TypeScript, Go, Rust, NGINX Configuration |

---

## Vulnerability Overview

HTTP Request Smuggling occurs when the front-end reverse proxy and back-end server interpret request message boundaries differently (e.g. `Content-Length` vs `Transfer-Encoding: chunked`). Manually setting transfer headers or forwarding client-controlled `Connection` hop-by-hop headers allows an attacker to "smuggle" an arbitrary request into the backend pipeline.

### Attack Impact
- **Session Hijacking**: Intercept unauthenticated credentials from other clients sharing the connection pool.
- **Cache Poisoning**: Inject malicious payloads directly into edge reverse-proxy caches.
- **WAF Bypass**: Route unauthorized admin endpoints past perimeter inspection filters.

---

## Non-Compliant Examples

### 1. Insecure Express / Node.js Reverse Proxy
```javascript
// Vulnerable: Forwarding raw hop-by-hop headers from client request
app.use((req, res, next) => {
  res.setHeader("Transfer-Encoding", req.headers["transfer-encoding"]);
  res.setHeader("Connection", req.headers["connection"]);
  next();
});
```

### 2. Insecure NGINX Configuration
```nginx
# Vulnerable: Reflecting dynamic client Connection header to upstream
location /api/ {
    proxy_pass http://backend_pool;
    proxy_set_header Connection $http_connection;
}
```

---

## Secure Remediation

### 1. Hardened NGINX Upstream Configuration
```nginx
location /api/ {
    proxy_pass http://backend_pool;
    proxy_http_version 1.1;
    proxy_set_header Connection ""; # Strip hop-by-hop connection tokens
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 2. Secure Application Proxying
1. Enforce **HTTP/2** between front-end reverse proxies and backend microservices.
2. Drop ambiguous incoming requests carrying both `Content-Length` and `Transfer-Encoding`.
3. Never manually inject or manipulate `Transfer-Encoding` in application code.
