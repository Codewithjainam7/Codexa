# CR-OAUTH-001: OAuth2 Open Redirect & Missing PKCE

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-OAUTH-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-601: URL Redirection to Untrusted Site](https://cwe.mitre.org/data/definitions/601.html) |
| **OWASP Top 10** | A01:2021 – Broken Access Control |
| **Languages** | Java, TypeScript, JavaScript, Python |

---

## Vulnerability Overview

In OAuth 2.0 authorization code grants, if the authorization server allows arbitrary client-supplied `redirect_uri` parameters without exact string matching, an attacker can craft a link directing the victim to authorize the app while routing the resulting authorization code to an attacker-controlled endpoint.

---

## Secure Remediation

Always enforce pre-registered exact redirect URI matching and mandatory PKCE (`code_challenge` / `code_verifier` with S256).
