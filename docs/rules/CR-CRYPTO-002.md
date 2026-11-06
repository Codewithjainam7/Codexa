# CR-CRYPTO-002: Insecure Cipher Mode (ECB), Static IV & Weak Key Size

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-CRYPTO-002` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-326: Inadequate Encryption Strength](https://cwe.mitre.org/data/definitions/326.html) / [CWE-329: Generation of Predictable IV with CBC Mode](https://cwe.mitre.org/data/definitions/329.html) |
| **OWASP Top 10** | A02:2021 – Cryptographic Failures |
| **Languages** | Java, Python, Go, Node.js |

---

## Vulnerability Overview

1. **Electronic Codebook (ECB) Mode**: Under ECB, identical blocks of 16-byte plaintext produce identical ciphertext blocks. This leaks structural patterns (such as bitmaps, database column duplicates, or structured headers).
2. **Static Initialization Vector (IV)**: Reusing an IV with modes like CBC or GCM allows eavesdroppers to perform XOR differential attacks, recovering plaintexts or computing forged message authentication tags.
3. **Short RSA Key Sizes**: 512-bit and 1024-bit RSA keys can be factored with modern distributed computing resources.

---

## Vulnerable Code Patterns

```java
// Vulnerable: ECB mode leaks structure
Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
cipher.init(Cipher.ENCRYPT_MODE, key);

// Vulnerable: Constant static IV
byte[] staticIv = new byte[16]; // All zeros!
IvParameterSpec ivSpec = new IvParameterSpec(staticIv);
```

---

## Secure Remediation

Use Authenticated Encryption with Associated Data (AEAD), specifically **AES-GCM**, with a randomized 12-byte initialization vector:

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
byte[] iv = new byte[12];
new SecureRandom().nextBytes(iv); // Cryptographically secure random IV
GCMParameterSpec spec = new GCMParameterSpec(128, iv);
cipher.init(Cipher.ENCRYPT_MODE, key, spec);
```
