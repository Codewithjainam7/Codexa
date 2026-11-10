# CR-HTTP-001: HTTP Request Smuggling & Hop-by-Hop Header Desync

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-HTTP-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-444: Inconsistent Interpretation of HTTP Requests](https://cwe.mitre.org/data/definitions/444.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | Java, JavaScript, TypeScript, NGINX Configuration |

---

## Vulnerability Overview

HTTP Request Smuggling occurs when the front-end reverse proxy and back-end server interpret request message boundaries differently (e.g. `Content-Length` vs `Transfer-Encoding: chunked`). Manually setting transfer headers or forwarding client-controlled `Connection` hop-by-hop headers allows an attacker to "smuggle" an arbitrary request into the backend pipeline.

---

## Secure Remediation

1. Enforce **HTTP/2** between front-end reverse proxies and backend microservices.
2. Normalize incoming HTTP headers and drop ambiguous requests carrying both `Content-Length` and `Transfer-Encoding`.
3. Never manually set `Transfer-Encoding` in application response headers.
