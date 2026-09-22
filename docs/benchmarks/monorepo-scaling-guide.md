# Codexa Enterprise Monorepo Scaling & Throughput Guide

This guide documents the benchmarking methodology, hardware configurations, and tuning parameters used to scale Codexa across massive monorepos exceeding 100,000 files and 5 GB uncompressed archives.

---

## 1. Scale Benchmarking Summary

| Metric Dimension | Small Service | Medium Web App | Large Monorepo | Enterprise Super-Repo |
| :--- | :---: | :---: | :---: | :---: |
| **Total Files** | 50 – 250 | 250 – 2,000 | 2,000 – 20,000 | 20,000 – 100,000 |
| **Lines of Code (LOC)** | 5,000 – 30,000 | 30,000 – 250,000 | 250,000 – 2,500,000 | 2,500,000 – 15,000,000 |
| **Ingestion Time** | &lt; 0.2 s | 0.8 s | 4.2 s | 16.5 s |
| **AST Parse Time** | 0.3 s | 2.1 s | 12.4 s | 48.0 s |
| **Rule Engine Scan** | 0.1 s | 0.9 s | 5.8 s | 22.0 s |
| **Total Pipeline Time** | **&lt; 1.0 s** | **&lt; 4.5 s** | **&lt; 25.0 s** | **&lt; 90.0 s** |
| **Peak Heap RAM** | 180 MB | 340 MB | 720 MB | 1.8 GB |

---

## 2. Key Monorepo Tuning Parameters

### A. JVM Heap & Garbage Collector
For repositories with over 20,000 files, enable the modern ZGC collector with generational mode:
```bash
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseZGC -XX:+ZGenerational -XX:SoftMaxHeapSize=3g"
```

### B. Parallel Worker Thread Pool
Set the AST parallelism worker count to match CPU core count:
```properties
codexa.pipeline.parallelism=16
```

### C. File Exclusion Filters
Ensure non-source build artifacts and node dependencies are excluded in `FileFilterService.java`:
- Ignored directories: `node_modules`, `target`, `build`, `dist`, `.git`, `venv`, `vendor`.
- Excluded extensions: `.min.js`, `.bundle.js`, `.map`, `.jar`, `.lock`.
