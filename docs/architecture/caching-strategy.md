# Codexa Multi-Tier Caching Strategy

## Overview
Re-analyzing unchanged source files across Git commits is wasteful. Codexa employs an AST content-addressable caching mechanism.

## Cache Hierarchy

| Cache Layer | Storage Mechanism | Eviction Policy | TTL | Max Size |
| :--- | :--- | :--- | :--- | :--- |
| **L1 AST Cache** | Caffeine (In-Memory Heap) | Window TinyLFU | 30 minutes | 5,000 trees |
| **L2 Rule Cache** | Caffeine (In-Memory Heap) | Read-Through | 24 hours | 1,000 rules |
| **L3 File Hash Cache** | SQLite `file_fingerprints` table | LRU / Commit-Based | 7 days | 500,000 records |

## Fingerprinting Algorithm
1. Normalize file content (strip Windows CRLF `\r\n` to POSIX `\n`).
2. Compute SHA-256 hash: `hash = SHA256(normalized_source)`.
3. Lookup `hash` + `rule_engine_version` in L1 / L3 cache.
4. If hit: Re-use cached findings and complexity metrics, bypassing AST re-parsing.
5. If miss: Parse AST, evaluate rules, and asynchronously persist result to cache.
