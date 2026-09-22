# Rule Specification: CR-JWT-001 (JWT Verification Flaws & Algorithm Bypass)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-JWT-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-347: Improper Verification of Cryptographic Signature](https://cwe.mitre.org/data/definitions/347.html) |
| **OWASP Top 10** | A07:2021 — Identification and Authentication Failures |
| **Target Scope** | Java AST (JJWT, Nimbus-JOSE), Node.js (jsonwebtoken), Python (PyJWT) |

---

## 1. Vulnerability Overview

JSON Web Tokens (JWT) are widely used for stateless identity and authorization. However, improper token validation libraries or insecure parsing implementations introduce severe authentication bypasses:
1. **Algorithm 'none' Acceptance**: If the parser accepts tokens with `"alg": "none"`, an attacker can forge any payload without providing a valid signature.
2. **Missing Signature Verification**: Parsing token payloads using decode-only methods (e.g. `jwt.decode(token)` in Python or decoding base64 payload segments directly) without invoking cryptographic signature checks.
3. **Key Confusion / Algorithm Switching**: An application using asymmetric RSA keys accepts an HMAC-signed token where the server's public key is treated as an HMAC symmetric secret.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**: Usages of `Jwts.parserBuilder()` without `.setSigningKey(...)`, or custom token parsing where signature verification is bypassed.
- **Python**: Usages of `jwt.decode(..., options={"verify_signature": False})`.
- **Node.js**: Usages of `jwt.decode(token)` used for authorization decisions instead of `jwt.verify(token, secret)`.

---

## 3. Vulnerable Code Examples

```python
# VULNERABLE: Disabling signature validation in PyJWT allows forged claims
payload = jwt.decode(token, options={"verify_signature": False})
user_id = payload.get("user_id")
```

```javascript
// VULNERABLE: jwt.decode only deserializes base64 payload without verifying signature
const decoded = jwt.decode(req.headers.authorization);
if (decoded.role === 'admin') {
    // Arbitrary privilege escalation!
}
```

---

## 4. Secure Remediation

Always enforce cryptographic verification with explicit permitted algorithm whitelists:

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;

public class TokenValidator {

    private final SecretKey signingKey;

    public TokenValidator(SecretKey signingKey) {
        this.signingKey = signingKey;
    }

    // SECURE: Enforces HMAC-SHA256 signature validation with configured secret key
    public Claims parseAndVerifyClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

### Whitelisting Guidelines:
- Explicitly whitelist expected algorithms (e.g. `algorithms=['HS256']` or `['RS256']`).
- Reject `"none"`, `"HS256"` when public RSA keys are loaded, and enforce token expiration (`exp`) and clock skew bounds.
