# Content-Addressable Storage (CAS) & AST Hash Caching

## 1. Overview
In continuous integration (CI) workflows, subsequent commits often modify only 1% to 5% of repository files. Re-parsing thousands of untouched source files wastes CPU cycles and memory.

Codexa implements a **Content-Addressable AST Cache (CAS)** indexed by the cryptographic SHA-256 hash of the normalized file contents.

---

## 2. Cache Architecture

```
Source File Content ──> SHA-256 Hash ──> [In-Memory Caffeine / Disk Cache]
                                                 │
                                 ┌───────────────┴───────────────┐
                                 ▼ (Hit)                         ▼ (Miss)
                          [Cached Findings]               [Parse AST & Store]
```

---

## 3. Normalization Rules
Before computing the SHA-256 digest, the content normalizer:
1. Standardizes line breaks to Unix LF (`\n`).
2. Strips trailing whitespace per line.
3. Preserves internal tokens and comments to ensure line-number accuracy.
