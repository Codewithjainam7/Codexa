# Benchmarks & Performance SLA

- Ingestion & unzipping < 2s for 100MB
- AST parsing < 5s for 1000 files
- Deterministic rule scan < 1s
- End-to-end audit with AI remediation < 45s


### AST Parser Throughput Benchmarks

- **Target Speed**: > 5,000 Lines of Code / Second
- **Memory Footprint**: < 256MB Heap during 10,000 LOC extraction
- **Rule Evaluation Latency**: < 50ms per source compilation unit
