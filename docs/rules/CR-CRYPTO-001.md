# Rule Specification: CR-CRYPTO-001 (Broken Cryptography & Deprecated Ciphers)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-CRYPTO-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-327: Use of a Broken or Risky Cryptographic Algorithm](https://cwe.mitre.org/data/definitions/327.html) |
| **OWASP Top 10** | A02:2021 — Cryptographic Failures |
| **Target Scope** | Java AST, Python, Node.js, Go, C# Cryptographic APIs |

---

## 1. Vulnerability Overview

Using legacy, deprecated, or mathematically broken cryptographic ciphers (such as DES, Triple-DES / 3DES, RC2, RC4, or Blowfish with small block sizes) allows attackers to decrypt confidential data through known cryptanalytic attacks, collision discovery, or brute-force key search.

Furthermore, utilizing block ciphers like AES in **Electronic Codebook (ECB)** mode (`AES/ECB/PKCS5Padding`) is insecure because identical plaintext blocks produce identical ciphertext blocks, leaking structural patterns without requiring key recovery.

---

## 2. Detection Logic & AST Patterns

Codexa analyzes `Cipher.getInstance(...)`, Python `cryptography.hazmat`, Node.js `crypto.createCipheriv`, and equivalent primitives:
1. Matches cipher transformation strings:
   - `DES`, `DESede`, `TripleDES`
   - `RC2`, `RC4`, `ARCFOUR`
   - Any cipher configured with `/ECB/` mode (e.g. `AES/ECB/PKCS5Padding`)
2. Verifies whether authenticated encryption (e.g. `GCM`, `CCM`, `ChaCha20-Poly1305`) is used in place of unauthenticated block modes (`CBC` without HMAC).

---

## 3. Vulnerable Code Examples

```java
// VULNERABLE: Using obsolete DES cipher with insufficient 56-bit key length
Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

// VULNERABLE: Using AES in ECB mode leaks plaintext pattern distribution
Cipher ecbCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
```

---

## 4. Secure Remediation

Migrate to modern authenticated symmetric encryption using **AES-256 in Galois/Counter Mode (GCM)** with a unique 96-bit Initialization Vector (IV) generated per encryption operation:

```java
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV to ciphertext for transport/storage
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return combined;
    }
}
```

---

## 5. Compliance & Cryptographic Standards

- **NIST SP 800-131A Rev. 2**: Formally disallows DES and 3DES for all federal and enterprise systems.
- **PCI DSS 4.0 (Req 3.4)**: Mandates strong cryptography (AES-128 minimum, AES-256 recommended) for cardholder data protection.
