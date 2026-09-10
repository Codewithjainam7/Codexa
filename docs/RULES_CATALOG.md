# Codexa AST Security & Quality Rule Catalog

## 1. Cryptographic Security Rules
- **CR-SEC-001:** Hardcoded Secrets & API Keys (Entropy > 4.5 & Pattern Match)
- **CR-SEC-002:** Weak Cryptography (DES, Triple-DES, Blowfish)
- **CR-SEC-003:** Insecure Random Number Generation (`java.util.Random`, `Math.random()`)
- **CR-SEC-004:** Hardcoded Internal IP Addresses (RFC 1918 Private Ranges)
- **CR-SEC-009:** Weak Hash Algorithms (MD5 / SHA-1)
- **CR-SEC-010:** Disabled SSL/TLS Certificate Validation (TrustManager Bypasses)

## 2. Injection & Memory Safety Rules
- **CR-SQL-001:** SQL Injection via String Concatenation in JDBC/JPA
- **CR-CMD-001:** Command Injection via `Runtime.getRuntime().exec` / `ProcessBuilder`
- **CR-XSS-001:** Reflected / Stored Cross-Site Scripting
- **CR-PATH-001:** Path Traversal & Arbitrary File Access

## 3. Reliability & Operational Rules
- **CR-OPS-001:** Missing Circuit Breakers & Timeout Handlers
- **CR-OPS-002:** Insecure Thread Pool Configuration
- **CR-OPS-003:** Unbounded Cached Thread Pool Creation (`newCachedThreadPool`)
- **CR-QUAL-006:** Empty Catch Block & Exception Swallowing
- **CR-PERF-001:** String Concatenation in Loops (O(N^2) Allocations)
