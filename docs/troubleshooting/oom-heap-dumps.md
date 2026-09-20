# Troubleshooting Guide: JVM Memory Tuning, Heap Dumps & OOM Recovery

Static analysis engines parse extensive syntactic graphs into memory. This operational runbook outlines JVM memory configurations, garbage collection parameters, and dump analysis techniques to maintain production stability.

---

## 1. Recommended JVM Memory Profiles

| Workload Tier | Monorepo Size | Files | Recommended RAM | JVM Flags |
| :--- | :--- | :--- | :--- | :--- |
| **Small / Microservices** | $< 50$ MB | $< 500$ | 1.5 GB | `-Xms512m -Xmx1024m` |
| **Standard Enterprise** | $50$–$500$ MB | $500$–$5,000$ | 2.5 GB | `-Xms1024m -Xmx2048m` |
| **High-Scale Monorepos** | $500$ MB – $3.0$ GB | $5,000$–$50,000$ | 4.0–8.0 GB | `-Xms2048m -Xmx4096m -XX:+UseG1GC` |

---

## 2. Production JVM Flags & Garbage Collection Tuning

Add these production flags to `JAVA_OPTS` in your Docker or systemd configuration:

```bash
JAVA_OPTS=" \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:G1ReservePercent=15 \
  -XX:+ExitOnOutOfMemoryError \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/codexa/heapdump-%p-%t.hprof"
```

### Rationale:
- **`G1GC`**: Prevents long full-GC stop-the-world pauses when discarding large AST syntax trees.
- **`ExitOnOutOfMemoryError`**: Fails fast so container orchestrators (Kubernetes / Render) can immediately spin up a fresh pod rather than leaving a zombie process.
- **`HeapDumpOnOutOfMemoryError`**: Captures an immutable binary `.hprof` snapshot at the exact moment heap allocation fails.

---

## 3. Live JVM Diagnostic Commands

### 3.1 Check Live Heap Residency with `jstat`
```bash
# Monitor GC cycles every 1000ms for PID 1
jstat -gcutil 1 1000
```
Inspect **O** (Old generation occupancy) and **YGC/FGC** (Young/Full GC counts). If Old generation remains $> 90\%$ after full GCs, heap headroom is exhausted.

### 3.2 Trigger On-Demand Heap Dump with `jcmd`
```bash
jcmd 1 GC.heap_dump /tmp/manual-dump.hprof
```

---

## 4. Post-Mortem Analysis via Eclipse MAT

1. **Load Dump**: Open the `.hprof` file in Eclipse Memory Analyzer Tool (MAT).
2. **Leak Suspects Report**: Inspect the automatic pie chart identifying dominating object clusters.
3. **Common Retained Heap Culprits**:
   - `com.github.javaparser.ast.CompilationUnit`: Indicates AST nodes are being retained after pipeline stages. (Verify that AST references are cleared upon job completion).
   - `java.util.zip.ZipInputStream`: Indicates large in-memory buffers instead of disk streaming. (Verify streaming buffer is 64KB).
   - `java.util.regex.Matcher`: Unbounded pattern cache growth.
