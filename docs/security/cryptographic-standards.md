# Codexa Cryptographic Baselines & Encryption Standards

This specification details the cryptographic controls, cipher suite baselines, key derivation standards, and random number generation policies implemented in **Codexa** and audited by its static analysis engine.

---

## 1. Compliance Alignment & Regulatory Baselines

Codexa's cryptographic architecture adheres to the following industry and government standards:
- **NIST SP 800-131A Rev. 2**: Transitioning the Use of Cryptographic Algorithms and Key Lengths.
- **NIST SP 800-52 Rev. 2**: Guidelines for the Selection, Configuration, and Use of TLS Implementations.
- **FIPS 140-3**: Security Requirements for Cryptographic Modules.
- **OWASP Top 10 (A02:2021)**: Cryptographic Failures.
- **PCI-DSS v4.0 Requirement 3**: Protect Stored Account Data & Strong Cryptography in Transit.

---

## 2. In-Transit Encryption Standards (TLS Policy)

All external and internal network communications (REST API endpoints, webhook deliveries, database links, and remote repository downloads) require modern Transport Layer Security (TLS).

### Protocol Configuration
- **Allowed Protocols**: TLS 1.3 (Default/Preferred), TLS 1.2 (Minimum acceptable fallback).
- **Prohibited Protocols**: SSL 2.0, SSL 3.0, TLS 1.0, TLS 1.1 are unconditionally disabled at the embedded Tomcat / Netty layer.

### Approved Cipher Suites
Codexa mandates Authenticated Encryption with Associated Data (AEAD) cipher suites providing Perfect Forward Secrecy (PFS):

#### TLS 1.3 Cipher Suites:
1. `TLS_AES_256_GCM_SHA384` (Default)
2. `TLS_CHACHA20_POLY1305_SHA256`
3. `TLS_AES_128_GCM_SHA256`

#### TLS 1.2 Cipher Suites:
1. `TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384`
2. `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`
3. `TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256`
4. `TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256`

### Disallowed Cipher Primitives:
- **CBC Mode**: All cipher block chaining suites (`*CBC*`) are prohibited to prevent Padding Oracle attacks (e.g. Lucky Thirteen, POODLE).
- **Static Key Exchange**: Plain RSA key transport (`TLS_RSA_*`) without ephemeral Diffie-Hellman is blocked.
- **Legacy Digests**: Any suite utilizing MD5 or SHA-1 for HMAC or integrity is strictly rejected.

---

## 3. Data-at-Rest Encryption & Secret Storage

Codexa encrypts sensitive configuration variables, API keys, webhook secrets, and persistent credentials using envelope encryption:

### Algorithm Specification:
- **Symmetric Cipher**: `AES-256-GCM` (Galois/Counter Mode) with 128-bit authentication tags.
- **Initialization Vector (IV)**: 96-bit cryptographically random IV generated via `SecureRandom` per encryption operation. Re-use of an IV with the same key is strictly prohibited.
- **Key Rotation**: Data encryption keys (DEKs) rotate every 90 days. Master Key Encryption Keys (KEKs) are supplied via environment variables (`CODEXA_MASTER_ENCRYPTION_KEY`) or cloud KMS (AWS KMS, GCP KMS, HashiCorp Vault).

### Ephemeral Memory Zeroization
To protect against memory dumps and Cold Boot attacks, sensitive cryptographic keys and plain-text byte arrays are cleared immediately after use:

```java
byte[] secretBytes = ...;
try {
    // Perform cryptographic operation
    cipher.doFinal(secretBytes);
} finally {
    // Deterministic memory zeroization
    Arrays.fill(secretBytes, (byte) 0);
}
```

---

## 4. Key Derivation & Password Storage Functions (KDF)

When hashing passwords or generating symmetric keys from passphrases, Codexa enforces adaptive, memory-hard Key Derivation Functions:

### Primary Standard: Argon2id (RFC 9106)
- **Algorithm Variant**: `Argon2id` (hybrid offering resistance against side-channel and GPU cracking attacks).
- **Memory Cost ($m$)**: $65,536\text{ KiB}$ ($64\text{ MB}$).
- **Time Cost / Iterations ($t$)**: $3\text{ passes}$.
- **Parallelism Degree ($p$)**: $4\text{ threads}$.
- **Tag Length**: $32\text{ bytes}$ ($256\text{ bits}$).
- **Salt Length**: $16\text{ bytes}$ ($128\text{ bits}$) cryptographically random salt.

### Secondary Fallback Standard: BCrypt (Spring Security)
- **Algorithm**: `BCryptPasswordEncoder`.
- **Work Factor (Cost)**: Minimum `12` rounds (4,096 iterations). Cost factors $< 10$ trigger static warning `CR-PASS-001`.

---

## 5. Webhook Signature Verification (HMAC-SHA256)

Outbound and inbound webhook notifications are cryptographically signed to verify payload integrity and authenticity:

$$\text{Signature} = \text{HMAC-SHA256}(\text{PayloadBytes}, \text{SecretKey})$$

### Constant-Time Equality Verification
To prevent timing side-channel attacks during signature verification, Codexa uses `MessageDigest.isEqual()`:

```java
public boolean verifyWebhookSignature(byte[] payload, String providedSignatureHex, byte[] secretKey) {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
    byte[] expectedHash = mac.doFinal(payload);
    byte[] providedHash = HexFormat.of().parseHex(providedSignatureHex);

    // Constant-time byte comparison prevents timing oracle attacks
    return MessageDigest.isEqual(expectedHash, providedHash);
}
```

---

## 6. Static Analysis Rules Reference (`CR-CRYPTO-001`, `CR-HASH-001`, `CR-RAND-001`)

Codexa's static engine actively inspects customer code against these cryptographic baselines:

| Codexa Rule | Target Invariant | Prohibited Patterns | Approved Modern Alternatives |
|:---|:---|:---|:---|
| `CR-CRYPTO-001` | Strong Ciphers & Modes | `DES`, `DESede`, `Blowfish`, `RC4`, `AES/ECB/*` | `AES/GCM/NoPadding`, `ChaCha20-Poly1305` |
| `CR-HASH-001` | Collision-Resistant Hashes | `MD2`, `MD4`, `MD5`, `SHA-1` | `SHA-256`, `SHA-384`, `SHA-512`, `SHA3-256` |
| `CR-RAND-001` | Cryptographic PRNG | `java.util.Random`, `Math.random()`, `rand.Intn` | `java.security.SecureRandom`, `crypto/rand` |
| `CR-PASS-001` | Adaptive Password KDF | `md5(pass)`, `sha256(pass)` | `Argon2id`, `BCrypt (cost >= 12)`, `PBKDF2` |
| `CR-CONFIG-001`| Hostname & TLS Validation | `TrustAllCertificates`, `AllowAllHostnameVerifier` | Standard PKI TrustStores with CA validation |
