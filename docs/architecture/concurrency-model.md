# Codexa Concurrency Architecture: Virtual Threads & Parallelism

## Overview
Codexa leverages Java 21 Project Loom Virtual Threads (`Thread.ofVirtual()`) to provide scalable, non-blocking I/O and CPU-bound AST rule processing.

## Thread Architecture Separation

```
HTTP Client Request
      │
      ▼
Spring Boot WebLayer (Virtual Thread per Request)
      │
      ├──> I/O Bound Tasks (Zip Extraction, Network Fetch, Database Reads)
      │    └── Executed directly on lightweight Virtual Threads (~1 KB stack overhead)
      │
      └──> CPU Bound Tasks (AST Tree Construction, Regex Scanning)
           └── Dispatched to ForkJoinPool worker carrier threads sized to Runtime.getRuntime().availableProcessors()
```

## Performance Benefits
- **Zero Thread Pool Exhaustion**: Even under high concurrent load (500+ parallel requests), virtual threads do not exhaust OS native thread limits.
- **Synchronous Style, Asynchronous Scalability**: Eliminates callback hell and complex reactive state machines while retaining maximum throughput.
- **Pinning Avoidance**: All synchronized blocks in critical paths have been migrated to `ReentrantLock` to prevent Loom carrier thread pinning.
