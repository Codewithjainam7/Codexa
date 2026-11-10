# CR-CORS-002: Insecure CORS Policy & Credential Leakage

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-CORS-002` |
| **Category** | Security |
| **Severity** | High |
| **Confidence** | High |
| **CWE** | [CWE-942: Permissive Cross-Domain Policy with Untrusted Domains](https://cwe.mitre.org/data/definitions/942.html) |
| **OWASP Top 10** | A01:2021 - Broken Access Control |

---

## Description
Cross-Origin Resource Sharing (CORS) relaxes the browser Same-Origin Policy (SOP). When an application dynamically reflects untrusted incoming `Origin` headers into `Access-Control-Allow-Origin` while simultaneously enabling `Access-Control-Allow-Credentials: true`, malicious websites can forge cross-origin HTTP requests and read sensitive authenticated responses (such as CSRF tokens, session IDs, and PII). Additionally, weak regex matching (e.g. `origin.endsWith("example.com")`) can be bypassed by attacker domains such as `evilexample.com`.

---

## Non-Compliant Code Example
```javascript
// Express.js vulnerable CORS origin reflection with credentials
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", req.headers.origin);
  res.header("Access-Control-Allow-Credentials", "true");
  next();
});
```

---

## Compliant Solution
```javascript
const ALLOWED_ORIGINS = new Set([
  "https://app.codexa.dev",
  "https://admin.codexa.dev"
]);

app.use((req, res, next) => {
  const origin = req.headers.origin;
  if (ALLOWED_ORIGINS.has(origin)) {
    res.header("Access-Control-Allow-Origin", origin);
    res.header("Access-Control-Allow-Credentials", "true");
  }
  next();
});
```
