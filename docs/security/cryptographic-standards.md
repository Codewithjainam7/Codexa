# Codexa Cryptographic Baselines & Encryption Standards

## Overview
Codexa enforces strict modern cryptographic configurations across network transit and persistent data storage.

## In-Transit Encryption (TLS 1.3)
- TLS 1.0 and 1.1 are completely disabled.
- Minimum accepted protocol is TLS 1.2 with PFS (Perfect Forward Secrecy).
- Supported Cipher Suites:
  - `TLS_AES_256_GCM_SHA384`
  - `TLS_CHACHA20_POLY1305_SHA256`
  - `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`

## Data-at-Rest Encryption
- SQLite / PostgreSQL databases: AES-256-GCM table and column-level encryption for sensitive secrets and webhook tokens.
- Key Derivation Function (KDF): Argon2id (memory: 64 MB, iterations: 3, parallelism: 4).
- Ephemeral Cache: In-memory buffers are scrubbed (`Arrays.fill(bytes, (byte) 0)`) immediately following cryptographic operations.
