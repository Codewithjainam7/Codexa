# Rule: CR-SEC-008 - Hardcoded Cryptographic Keys & Salts

## Metadata
- **Severity**: HIGH
- **Category**: Security / Cryptography
- **CWE**: CWE-321 (Use of Hard-coded Cryptographic Key)
- **Languages**: Java, TypeScript, Go, Python

## Vulnerability Description
Hardcoding secret encryption keys, HMAC secrets, or password salts directly within source code repositories exposes them to extraction via decompilation, Git history inspection, or repository leaks.

## Vulnerable Example
```typescript
// VIOLATION: Static key embedded in source
const JWT_SECRET = "super-secret-key-12345";
const token = jwt.sign(payload, JWT_SECRET);
```

## Remediated Example
```typescript
// SAFE: Load key dynamically from secure environment
const jwtSecret = process.env.JWT_SIGNING_KEY;
if (!jwtSecret) throw new Error("JWT_SIGNING_KEY missing");
const token = jwt.sign(payload, jwtSecret);
```
