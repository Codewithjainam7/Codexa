# Codexa Large-Scale Performance Benchmarks

## 3GB Repository Ingestion & Analysis Benchmark
- **Test Machine:** AMD Ryzen 9 / 32GB RAM / NVMe SSD / OpenJDK 17
- **Test Payload:** 3.1 GB Spring & Microservices Monorepo (18,400 source files, 1.8M LOC)

| Phase | Duration | Throughput | Peak Heap |
| :--- | :---: | :---: | :---: |
| **Decompression & Safety Quarantine** | 4.6s | 674 MB/s | 180 MB |
| **AST Parse & Compilation Unit Gen** | 12.8s | ~140,000 LOC/s | 820 MB |
| **Static Rule Evaluation (30 Rules)** | 1.9s | ~950,000 LOC/s | 410 MB |
| **Total Pipeline (AST + Static Rules)** | 19.3s | ~93,000 LOC/s | 850 MB |
