# Codexa Engine Performance Targets & Empirical SLAs

## 1. Executive Summary & SLA Guarantees
Codexa is engineered for sub-second developer feedback loops and high-throughput CI/CD pipeline gating. All performance metrics are benchmarked on standard cloud compute instances (4 vCPU, 8 GB RAM, NVMe storage) using Java 21 LTS with Virtual Threads enabled.

---

## 2. End-to-End Pipeline Latency Percentiles

| Repository Size | Lines of Code (LOC) | File Count | p50 Latency | p90 Latency | p99 Latency | Max Memory |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Microservice (Small)** | 5,000 – 15,000 | 25 – 80 | **320 ms** | **480 ms** | **710 ms** | 128 MB |
| **Medium Service** | 25,000 – 75,000 | 120 – 350 | **1.12 s** | **1.64 s** | **2.20 s** | 240 MB |
| **Enterprise Service** | 100,000 – 250,000 | 500 – 1,200 | **2.85 s** | **3.90 s** | **5.10 s** | 480 MB |
| **Monorepo (Large)** | 500,000 – 1,000,000 | 2,500 – 6,000 | **8.40 s** | **11.20 s** | **14.80 s** | 1.1 GB |

---

## 3. Pipeline Stage Breakdown (Medium Repository: 50,000 LOC)

```
[1. Ingestion & Unpack]  ████ 140ms (12.5%)
[2. AST Lexing & Token]  ████████████ 420ms (37.5%)
[3. AST Rule Evaluation] ██████████ 360ms (32.1%)
[4. Scoring & Triage]    ██ 80ms (7.1%)
[5. Report & Badges]     ███ 120ms (10.7%)
────────────────────────────────────────────
Total Elapsed Time:      1,120 ms (1.12 seconds)
```

---

## 4. Throughput Targets
* **AST Parsing Velocity**: $> 45,000$ lines of executable code per second per CPU core.
* **Deterministic Rule Evaluation**: $< 1.2$ milliseconds per source compilation unit per rule.
* **Concurrent Pipeline Scaling**: Up to 64 concurrent repository analyses without thread exhaustion under Java 21 Project Loom fibers.
