# Content Security Policy (CSP) Level 3 Implementation Guide

## 1. Overview
Content Security Policy (CSP) is an HTTP response header that restricts the resources (scripts, images, stylesheets, fonts) that a web browser is allowed to load for a given page, mitigating Cross-Site Scripting (XSS) and data injection.

---

## 2. Production Header Configuration

```http
Content-Security-Policy: default-src 'self'; script-src 'self' 'nonce-{RANDOM}' 'strict-dynamic'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:; connect-src 'self' https://codexa-ye85.onrender.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self';
```

---

## 3. Key Directives Explained
* **`default-src 'self'`**: Fallback restriction limiting unlisted directives to the origin server.
* **`frame-ancestors 'none'`**: Completely prohibits embedding the application in `<iframe>` elements, eliminating Clickjacking (`CWE-1021`).
* **`strict-dynamic`**: Enables trust propagation from verified nonce scripts to dynamically loaded modules.
