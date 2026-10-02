# JSON Web Token (JWT) Defense-in-Depth Specification

## 1. Overview
JSON Web Tokens (JWT / RFC 7519) represent stateless claims in distributed microservices. Misconfigurations often lead to authentication bypasses and signature spoofing.

---

## 2. Common Vulnerabilities & Hardening

| Vulnerability | Attack Mechanism | Hardening Rule |
| :--- | :--- | :--- |
| **`alg: none` Bypass** | Attacker strips signature and sets `alg: "none"` | Explicitly reject unsigned tokens in JWT parser |
| **Algorithm Confusion** | Attacker signs token with public RSA key using HMAC-SHA256 | Strictly enforce expected asymmetric algorithm in decoder |
| **Weak Secret Key** | Dictionary brute-force of HMAC secrets | Enforce minimum 256-bit high-entropy secrets for HS256 |
| **Missing Expiration** | Token remains valid indefinitely if stolen | Enforce short `exp` (<= 15 mins) and sliding refresh tokens |

---

## 3. Token Parsing Implementation

```java
public Claims parseAndValidateToken(String token, RSAPublicKey publicKey) {
    return Jwts.parser()
        .verifyWith(publicKey)
        .requireIssuer("https://auth.codexa.dev")
        .requireAudience("codexa-api")
        .build()
        .parseSignedClaims(token)
        .getPayload();
}
```

---

## 4. Revocation Strategy
Stateless JWTs cannot be revoked without state. Codexa implements a hybrid approach:
1. Fast-path in-memory check for validity.
2. Distributed Redis blocklist for revoked `jti` (JWT ID) claims on user logout or role modification.
