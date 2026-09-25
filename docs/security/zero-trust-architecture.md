# Codexa Zero-Trust Security Architecture for Code Ingestion

This document details the Zero-Trust Architecture (ZTA), defense-in-depth isolation layers, hostile payload neutralization techniques, and sandboxing guarantees implemented in **Codexa** to safely process untrusted source code.

---

## 1. Threat Model & Untrusted Ingestion Assumption

Codexa is designed to ingest source code repositories from public GitHub URLs and multi-megabyte user-uploaded ZIP archives. In a multi-tenant or enterprise environment, **uploaded source code, build scripts, configuration files, and archive metadata must be treated as actively adversarial and potentially malicious.**

### Core Security Invariants:
1. **Never Execute Untrusted Code**: Codexa is purely a Static Application Security Testing (SAST) and Abstract Syntax Tree (AST) engine. It **never** invokes compilers, runtime environments (`javac`, `python`, `node`, `gcc`), build systems (`gradle`, `mvn`, `npm`), or script interpreters against uploaded source code.
2. **Zero Classpath Contamination**: Untrusted classes and dependencies are never loaded into the host JVM's `ClassLoader`.
3. **Strict Egress Filtering**: Ingestion workers cannot connect to internal cloud infrastructure or link-local metadata services.
4. **Least-Privilege Isolation**: The Codexa container operates as an unprivileged user with read-only root filesystems and all Linux capabilities dropped.

---

## 2. Multi-Layer Defense-in-Depth Model

```
+-----------------------------------------------------------------------------------+
|                           Layer 1: Network Perimeter                              |
|   - Strict Rate Limiting (Token Bucket per IP)                                     |
|   - Defensive HTTP Security Headers (CSP, HSTS, X-Content-Type-Options: nosniff)  |
|   - SSRF Interceptor: Block private IP ranges (RFC 1918, RFC 3927 metadata)       |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                       Layer 2: Ingestion & Archive Sandbox                        |
|   - Zip Slip Traversal Neutralization (Canonical path boundary checks)            |
|   - Zip Bomb & Compression Ratio Guards (Threshold: 100:1, Max Bytes: 500MB)      |
|   - Directory Depth Clamping (Max depth: 15 levels)                               |
|   - Max File Count Threshold (Max files: 10,000)                                  |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                        Layer 3: File Filtering & Exclusion                        |
|   - FileFilterService: Skip binaries, .class, .jar, .so, media, and build caches  |
|   - Max per-file size bounding (Max: 5MB per source file)                         |
|   - UTF-8 validation and null-byte injection rejection                            |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                    Layer 4: Static AST Parsing & Sanitization                     |
|   - JavaParser / Tree-sitter in static parse-only mode                            |
|   - In-flight secret sanitization & Shannon entropy masking before AI dispatch    |
|   - ReDoS-safe regular expressions with pre-compiled timeouts                     |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                    Layer 5: Transient Workspace Destruction                       |
|   - Deterministic staging deletion in Java `finally` block                        |
|   - Hourly background reaper sweeps abandoned directories                         |
+-----------------------------------------------------------------------------------+
```

---

## 3. Layer 1: Ingestion Network Defense & SSRF Neutralization

When a user requests analysis via a remote repository URL (e.g. `https://github.com/org/repo`), `SsrfProtectionService` intercepts and validates the URL before any HTTP connection is established:

1. **Protocol Restriction**: Only `https://` (and explicitly permitted `http://` in dev) schemes are allowed. Schemes like `file://`, `ftp://`, `gopher://`, `jar://`, and `data://` are rejected with `HTTP 400 INVALID_PROTOCOL`.
2. **DNS Resolution & Private IP Blacklisting**:
   The hostname is resolved to all associated IPv4 and IPv6 addresses. If any resolved IP falls within reserved or private address spaces, the request is immediately aborted with `SSRF_ATTEMPT_DETECTED`:
   - `10.0.0.0/8` (RFC 1918 Private Network)
   - `172.16.0.0/12` (RFC 1918 Private Network)
   - `192.168.0.0/16` (RFC 1918 Private Network)
   - `127.0.0.0/8` (Loopback)
   - `169.254.0.0/16` (RFC 3927 Link-Local / AWS/GCP Instance Metadata Service `169.254.169.254`)
   - `::1` (IPv6 Loopback)
   - `fe80::/10` (IPv6 Link-Local)
   - `fd00::/8` (IPv6 Unique Local)
3. **DNS Rebinding Defense**: The resolved IP is pinned for the actual HTTP payload download, preventing Time-of-Check to Time-of-Use (TOCTOU) DNS rebinding attacks.

---

## 4. Layer 2: Decompression Sandboxing (`SecureZipExtractor`)

Archives submitted via multipart file upload or Git ZIP downloads are processed through `SecureZipExtractor` using streaming 64KB buffers:

### Security Boundaries:
- **Zip Slip Traversal Protection**:
  ```java
  Path entryDestination = normalizedTargetDir.resolve(entryName).normalize();
  if (!entryDestination.startsWith(normalizedTargetDir)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ZIP_SLIP_DETECTED",
          "Malicious zip entry attempting directory traversal: " + entryName);
  }
  ```
- **Zip Bomb Decompression Ratio Defense**:
  Tracks the ratio between uncompressed bytes and compressed entry size. If the decompression ratio exceeds $100:1$ and total uncompressed bytes exceed $50\text{ MB}$, the stream is aborted with `ZIP_BOMB_DETECTED`.
- **Absolute Limits**:
  - Maximum archive size: $100\text{ MB}$ compressed.
  - Maximum extracted size: $500\text{ MB}$ uncompressed.
  - Maximum total files: $10,000$ files.
  - Maximum directory depth: $15$ directory levels.

---

## 5. Layer 3: File Filtering & Exclusion Boundary

Before passing staged files to AST parsers, `FileFilterService` filters out non-source artifacts to prevent denial of service and memory exhaustion:

```java
public boolean isCandidateSourceFile(Path path) {
    String filename = path.getFileName().toString().toLowerCase();
    
    // Ignore build output and package caches
    if (path.toString().contains("/target/") || 
        path.toString().contains("/build/") || 
        path.toString().contains("/node_modules/") || 
        path.toString().contains("/.git/")) {
        return false;
    }
    
    // Ignore binary executables, archives, and compiled bytecode
    if (filename.endsWith(".class") || filename.endsWith(".jar") || 
        filename.endsWith(".exe") || filename.endsWith(".dll") || 
        filename.endsWith(".so") || filename.endsWith(".dylib")) {
        return false;
    }
    
    // Whitelisted extensions: .java, .js, .jsx, .ts, .tsx, .py, .go, .sql, .yaml, .json, .dockerfile
    return hasSupportedExtension(filename);
}
```

---

## 6. Layer 4: Host & Container Hardening Guarantees

In containerized deployments (Docker / Kubernetes), Codexa enforces strict host-isolation parameters:

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 10001
  runAsGroup: 10001
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
```

- **Ephemeral Volume Mount**: `/tmp/codexa/staging` is mounted as an isolated `emptyDir` memory or scratch volume with `noexec,nosuid,nodev` flags, ensuring no extracted script or binary can ever be executed at the OS level.
- **Seccomp Profile**: Default Docker seccomp profile active, blocking dangerous syscalls (`ptrace`, `sys_chroot`, `kexec_load`).
- **Resource Constraints**: Cgroups memory limit ($2.0\text{ GB}$) and CPU quota ($2.0\text{ cores}$) prevent resource starvation of co-located services.

---

## 7. Security Invariant Verification Matrix

| Threat Vector | Mitigation Mechanism | Verification Test Suite |
|:---|:---|:---|
| **Arbitrary Code Execution** | Non-executable filesystem, static AST only | `JavaAstParserServiceTest` |
| **Path Traversal / Overwrite** | Canonical path validation in `SecureZipExtractor` | `SecureZipExtractorTest.shouldDetectZipSlip` |
| **Zip Bomb Decompression DoS** | 100:1 ratio & 500MB threshold guards | `SecureZipExtractorTest.shouldDetectZipBomb` |
| **SSRF Cloud Metadata Stealing** | `SsrfProtectionService` IP & DNS filtering | `SsrfProtectionTest` (16 test cases) |
| **Credential Exfiltration to AI** | Shannon entropy & regex redaction pre-flight | `SecretMaskingSecurityTest` (8 test cases) |
| **Monorepo Memory Exhaustion** | 64KB streaming buffers & file size bounds | `ScannerFalsePositiveRegressionTest` |
