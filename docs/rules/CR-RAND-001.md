# Rule Specification: CR-RAND-001 (Insecure Pseudorandom Number Generators)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-RAND-001` |
| **Category** | Security |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-338: Use of Cryptographically Weak Pseudo-Random Number Generator](https://cwe.mitre.org/data/definitions/338.html) |
| **OWASP Top 10** | A02:2021 — Cryptographic Failures |
| **Target Scope** | Java AST, Python, Node.js, Go Random Number APIs |

---

## 1. Vulnerability Overview

Standard pseudorandom number generators (PRNGs) like `java.util.Random`, `Math.random()`, Python's `random` module, and JavaScript's `Math.random()` are deterministic linear congruential generators (LCGs) or Mersenne Twisters. They are designed for statistical simulations and non-security workloads, not security boundaries.

An attacker who observes a small sequence of generated outputs can reconstruct the internal PRNG seed state and predict all past and future outputs. Using standard PRNGs to generate session IDs, password reset tokens, CSRF tokens, MFA OTP codes, or cryptographic nonces completely breaks the security model.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**: `new java.util.Random()`, `Math.random()`, `ThreadLocalRandom.current()`.
- **Python**: `import random`, `random.randint(...)`, `random.choice(...)`.
- **Node.js**: `Math.random()`.
- Heuristic Context: Flags when used in methods generating tokens, passwords, keys, salts, tickets, or IDs. Excludes test mocks, UI particle animations, and statistical benchmarks.

---

## 3. Vulnerable Code Example

```java
import java.util.Random;

public class PasswordResetService {
    private final Random random = new Random();

    // VULNERABLE: Linear Congruential Generator seed can be calculated from 2 consecutive outputs
    public String generateResetToken() {
        return Long.toHexString(random.nextLong());
    }
}
```

---

## 4. Secure Remediation

Use a Cryptographically Secure Pseudo-Random Number Generator (CSPRNG) backed by OS entropy pools (`/dev/urandom`, Windows `CryptGenRandom`):

```java
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordResetService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // SECURE: CSPRNG backed by OS kernel entropy pool
    public String generateResetToken() {
        byte[] buffer = new byte[32];
        SECURE_RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}
```

### Polyglot Equivalents:
- **Python**: `import secrets; token = secrets.token_urlsafe(32)`
- **Node.js**: `import crypto from 'crypto'; const token = crypto.randomBytes(32).toString('hex');`
