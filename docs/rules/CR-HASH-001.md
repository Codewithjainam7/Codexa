# Rule Specification: CR-HASH-001 (Insecure Hash Algorithms - MD5 & SHA-1)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-HASH-001` |
| **Category** | Security |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-328: Use of Weak Hash](https://cwe.mitre.org/data/definitions/328.html) |
| **OWASP Top 10** | A02:2021 — Cryptographic Failures |
| **Target Scope** | Java AST, Python, Node.js, Go, Rust Hashing APIs |

---

## 1. Vulnerability Overview

Cryptographic hash functions such as **MD5** and **SHA-1** have known theoretical and practical collision vulnerabilities. Attackers can generate distinct plaintexts that produce identical hash digests (e.g. Flame malware MD5 collision, SHAttered SHA-1 collision), undermining signature integrity, certificate validation, and digital tamper-proofing.

*Note*: Using MD5 or SHA-1 strictly for non-cryptographic checksums (e.g. Git object IDs or file deduplication shingles) is benign, but using them for security tokens, integrity verification, password storage, or digital signatures poses severe security risks.

---

## 2. Detection Logic & AST Patterns

Codexa checks for invocations of message digest factories across languages:
- **Java**: `MessageDigest.getInstance("MD5")`, `MessageDigest.getInstance("SHA-1")`, `DigestUtils.md5Hex(...)`, `DigestUtils.sha1Hex(...)`.
- **Python**: `hashlib.md5(...)`, `hashlib.sha1(...)`.
- **Node.js**: `crypto.createHash('md5')`, `crypto.createHash('sha1')`.
- Distinguishes non-security uses (e.g. line normalization, shingle hashing) from security tokens and password hashes.

---

## 3. Vulnerable Code Example

```java
import java.security.MessageDigest;

public class TokenGenerator {
    // VULNERABLE: MD5 is susceptible to rapid collision and length-extension attacks
    public static byte[] generateSecurityToken(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
}
```

---

## 4. Secure Remediation

Migrate to secure cryptographic hash algorithms from the **SHA-2** (SHA-256, SHA-512) or **SHA-3** families:

```java
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class TokenGenerator {
    // SECURE: SHA-256 provides 128-bit collision resistance and 256-bit preimage resistance
    public static byte[] generateSecurityToken(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
}
```

*For Password Storage*: Do not use general-purpose hash algorithms (even SHA-256). Always use salted, memory-hard key derivation functions like **Argon2id** or **Bcrypt** (see `CR-PASS-001`).
