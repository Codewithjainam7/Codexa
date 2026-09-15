# Codexa JVM Heap Sizing & Memory Management

## Architecture Principles
Codexa parses hundreds of source files into Abstract Syntax Trees (ASTs) concurrently. Unchecked memory consumption during recursive AST traversal can cause excessive GC pauses. Codexa employs explicit memory management safeguards:

## Recommended JVM Flags

```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1ReservePercent=15 \
     -XX:InitiatingHeapOccupancyPercent=45 \
     -Dfile.encoding=UTF-8 \
     -jar codexa-backend.jar
```

## Heap Allocations by Subsystem

```
Total Heap (4 GB Allocation)
├── 45% (1.8 GB): Short-lived AST & Token Buffers (Young Gen)
├── 30% (1.2 GB): L1 In-Memory Caffeine Rules & Model Cache (Tenured)
├── 15% (600 MB): SQLite Off-Heap Page Cache Buffers
└── 10% (400 MB): Operating Headroom & Thread Stacks
```

## Protection Against Out-Of-Memory (OOM)
- **Max Archive File Size**: Default capped at 50 MB compressed / 250 MB uncompressed.
- **Max File Token Depth**: Files with AST depth exceeding 10,000 nodes are safely short-circuited with `DEPTH_EXCEEDED` warning findings rather than crashing the worker thread.
