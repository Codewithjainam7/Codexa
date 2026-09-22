# Rule Specification: CR-PASS-001 (Plaintext Password Storage & Weak Hashing)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-PASS-001` |
| **Category** | Security |
| **Default Severity** | `CRITICAL` |
| **CWE Mapping** | [CWE-916: Use of Password Hash With Insufficient Computational Effort](https://cwe.mitre.org/data/definitions/916.html) / [CWE-256: Plaintext Storage of a Password](https://cwe.mitre.org/data/definitions/256.html) |
| **OWASP Top 10** | A07:2021 — Identification and Authentication Failures |
| **Target Scope** | Java AST, Python, Node.js Authentication & Persistence Modules |

---

## 1. Vulnerability Overview

Storing user passwords in plaintext or hashing them with general-purpose digest algorithms (MD5, SHA-1, SHA-256) without memory-hard work factors makes them trivially crackable via offline precomputed rainbow tables or GPU hash clusters (capable of billions of guesses per second).

Authentication systems must store passwords using adaptive, salted, memory-hard Key Derivation Functions (KDFs) such as **Argon2id**, **Bcrypt**, or **PBKDF2**.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
1. Direct assignment of incoming password fields to persistent database entities or SQL statements without invocation of a recognized password encoder.
2. Invocations of `MessageDigest.getInstance(...)` or `DigestUtils.sha256Hex(...)` where argument names contain `password`, `pwd`, `passphrase`, or `secret`.
3. In Spring Security, flags usages of `NoOpPasswordEncoder.getInstance()`.

---

## 3. Vulnerable Code Examples

```java
// VULNERABLE: Direct plaintext password persistence
public User registerUser(String username, String password) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(password); // Plaintext in database!
    return userRepository.save(user);
}

// VULNERABLE: General-purpose SHA-256 is vulnerable to GPU cracking
public User registerUserWithWeakHash(String username, String password) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(DigestUtils.sha256Hex(password)); // No salt, no work factor!
    return userRepository.save(user);
}
```

---

## 4. Secure Remediation

Utilize Spring Security's `PasswordEncoder` interface backed by **BCrypt** (cost factor &ge; 12) or **Argon2id**:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RegistrationService {

    // SECURE: BCrypt with work factor 12 (4096 rounds of Blowfish key expansion)
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public User registerUser(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}
```

---

## 5. Security Thresholds & Guidelines

- **Argon2id (Recommended)**: Memory: 64 MB (`65536 KiB`), Iterations: 3, Parallelism: 4.
- **BCrypt**: Minimum cost parameter `12` (calibrated to ~250ms per check on server hardware).
- **Salt Generation**: Automatically managed per-hash by the KDF (minimum 128-bit random salt).
