# CR-WEBSOCKET-001: Insecure WebSocket Origin Validation (CSWSH)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-WEBSOCKET-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-1385: Missing Origin Validation in WebSockets](https://cwe.mitre.org/data/definitions/1385.html) |
| **OWASP Top 10** | A01:2021 – Broken Access Control |
| **Languages** | JavaScript, TypeScript, Java, Go |

---

## Vulnerability Overview

WebSockets are not constrained by the Same-Origin Policy (SOP). When a browser opens a WebSocket connection, it sends cookies automatically in the HTTP upgrade request. If the server does not check the `Origin` header, a malicious site visited by an authenticated user can open a WebSocket to the target application on behalf of the user, reading messages and transmitting commands (Cross-Site WebSocket Hijacking).

---

## Secure Remediation

```typescript
// Secure WebSocket Server:
import { WebSocketServer } from 'ws';

const ALLOWED_ORIGINS = new Set(['https://app.codexa.io', 'https://codexa.io']);

const wss = new WebSocketServer({
  server,
  verifyClient: (info, callback) => {
    const origin = info.origin;
    if (!origin || !ALLOWED_ORIGINS.has(origin)) {
      return callback(false, 403, 'Forbidden Origin');
    }
    callback(true);
  }
});
```
