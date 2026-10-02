# AST Rule Engine Micro-Benchmarks & Rule Execution Latencies

## 1. Micro-Benchmark Methodology
Micro-benchmarks measure the CPU time spent executing each analysis rule against an abstract syntax tree of 1,000 AST nodes. Executed via JMH (Java Microbenchmark Harness) on OpenJDK 21.

---

## 2. Individual Rule Evaluation Latency

| Rule ID | Rule Name | Category | Average Latency | Ops / Second |
| :--- | :--- | :--- | :--- | :--- |
| `CR-SEC-001` | SQL Injection (Statement Concat) | SECURITY | **0.18 ms** | 5,550 ops/sec |
| `CR-SEC-004` | SSRF Vulnerability Sink | SECURITY | **0.22 ms** | 4,540 ops/sec |
| `CR-CMD-001` | Command Injection (Runtime.exec) | SECURITY | **0.14 ms** | 7,140 ops/sec |
| `CR-DESER-001`| Insecure Deserialization | SECURITY | **0.19 ms** | 5,260 ops/sec |
| `CR-LLM-001` | LLM Prompt Injection | SECURITY | **0.25 ms** | 4,000 ops/sec |
| `CR-API-001` | BOPLA Mass Assignment | SECURITY | **0.31 ms** | 3,220 ops/sec |
| `CR-IAC-001` | Dockerfile Root Execution | OPERATIONS | **0.08 ms** | 12,500 ops/sec |
| `CR-LIC-001` | Viral Copyleft License Audit | OPERATIONS | **0.42 ms** | 2,380 ops/sec |

---

## 3. Zero-Allocation AST Traversal
Rules are optimized for zero object allocation in hot visitor loops, minimizing GC pause times to under 2 milliseconds using Java 21 Generational ZGC.
