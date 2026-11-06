# CR-JWT-001: Insecure JWT Validation & Algorithm Confusion

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-JWT-001` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-347: Improper Verification of Cryptographic Signature](https://cwe.mitre.org/data/definitions/347.html) |
| **OWASP Top 10** | A02:2021 – Cryptographic Failures |
| **Languages** | Java, Python, JavaScript, TypeScript, Go |

---

## Vulnerability Overview

JSON Web Tokens (JWT) are widely adopted for stateless authorization. However, flawed verification implementations can result in complete authentication bypass:
1. **Unsigned Parsing (`none` algorithm)**: Accepting tokens with `{"alg": "none"}` where the signature is omitted or ignored.
2. **Missing Signature Check**: Calling token decoding methods (e.g. `jwt.decode` in Python or `parseClaimsJwt` in Java JJWT) that merely read the payload without checking the HMAC or RSA signature.
3. **Key Confusion**: Verifying an RSA/ECDSA token using an HMAC algorithm, treating the public key PEM file string as an HMAC symmetric secret.

---

## Vulnerable Code Patterns

```python
# Vulnerable PyJWT:
import jwt
decoded = jwt.decode(token, options={"verify_signature": False})
```

```java
// Vulnerable JJWT:
Claims claims = Jwts.parserBuilder()
    .build()
    .parseClaimsJwt(token) // Does not check signature!
    .getBody();
```

---

## Secure Remediation

```java
// Secure JJWT:
Claims claims = Jwts.parserBuilder()
    .setSigningKey(secretKey)
    .build()
    .parseClaimsJws(token) // Validates signature against secretKey
    .getBody();
```
