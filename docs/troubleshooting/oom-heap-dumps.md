# Troubleshooting Guide: JVM Heap Dumps & Out-of-Memory Errors

## Automatic Heap Dump Configuration
Add these JVM flags to trigger automated crash snapshots:

```bash
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/var/log/codexa/heapdump-%p-%t.hprof
```

## Step-by-Step Dump Analysis via Eclipse MAT

1. **Load Dump**: Open the `.hprof` file in Eclipse Memory Analyzer Tool (MAT).
2. **Leak Suspects Report**: Inspect the automatic Leak Suspects pie chart.
3. **Dominator Tree**: Sort objects by **Retained Heap** size to identify which component holds memory.
4. **Common Culprits**:
   - `Caffeine Cache`: High concurrency eviction backlog. Fix: Lower `-Dcodexa.cache.ast.max-size`.
   - `ZipArchiveInputStream`: Decompressing uncompressed tarballs in-memory. Fix: Ensure disk streaming is active.
