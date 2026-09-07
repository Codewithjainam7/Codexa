# Codexa High-Performance Analysis Pipeline

Codexa processes source code archives and Git repositories through a multi-stage deterministic pipeline engineered for blazing speed, high-scale ingestion, and rigorous static security analysis.

```
       [ Uploaded Archive / Git URL (Up to 3 GB) ]
                          │
                          ▼
        [ Stage 1: Sandboxed Ingestion ]
       (Path Traversal, Quotas, 50,000 Files)
                          │
                          ▼
       [ Stage 2: Parallel AST Parsing ]
    (ThreadLocal JavaParser, ForkJoinPool)
                          │
                          ▼
       [ Stage 3: Rule Evaluation Engine ]
    (23+ OWASP Deterministic AST Security Rules)
                          │
                          ▼
      [ Stage 4: Deep Project Diagnostics ]
  (White-Box AST Metrics & Black-Box Ingress Map)
                          │
                          ▼
     [ Stage 5: AI Enrichment & Fallback ]
    (Top-3 Findings LLM + Template Fallback)
                          │
                          ▼
    [ Stage 6: Multi-Factor Risk Scoring ]
 (Security 60%, Quality 25%, Ops 15%, Maintainability)
                          │
                          ▼
    [ Stage 7: Report Generation & Export ]
   (Interactive Dashboard, PDF, HTML, MD, JSON)
```

## Stage Breakdown

### Stage 1: Sandboxed Ingestion & Scaling
- **High-Capacity Quotas**: Supports archives up to **3,072 MB (3.0 GB)** upload size, **4,000 MB (4.0 GB)** extracted capacity, and **50,000 files** per scan.
- **Zip Slip Defense**: Canonical path validation prevents directory traversal out of isolated staging directories.
- **Selective Filtering**: Discards binaries, `node_modules`, build outputs, coverage caches, and vendor directories.

### Stage 2: Parallel AST Parsing
- Parses source files into structured Abstract Syntax Trees concurrently using a `ForkJoinPool` with `ThreadLocal<JavaParser>` instances.
- Zero parser re-instantiation overhead across worker threads.

### Stage 3: Deterministic Rule Evaluation (23+ Rules)
- Analyzes AST nodes across 23 deterministic rules including SQL Injection, Command Injection, Insecure Deserialization, Path Traversal, SSRF, CSRF, and Cryptographic failures.
- Deterministic AST visitor pattern produces reproducible results with zero hallucination.

### Stage 4: Deep Project Diagnostics Collection
- **Code Composition**: Extracts lines of code (LOC), documentation/comment lines, blank lines, and language distribution (Java, TypeScript, Python, SQL, Config).
- **White-Box AST Metrics**: Calculates average/peak cyclomatic complexity, AST nesting depth, total classes/methods/interfaces, and top complex files refactoring leaderboard.
- **Black-Box Attack Surface**: Maps exposed HTTP endpoints (`@GetMapping`, `@PostMapping`, Express, FastAPI), access control boundaries, and perimeter status.

### Stage 5: AI Enrichment & Remediation Diff Generation
- Prioritizes top findings for deep neural LLM remediation generation with prompt secret masking.
- Offline deterministic template fallback ensures zero downtime if AI service is unavailable.

### Stage 6: Production Readiness & Quality Scoring
- **Overall Score**: `0.60 * Security + 0.25 * Quality + 0.15 * Operations`.
- **Maintainability Index**: Refactoring penalty based on cyclomatic complexity and nesting depth.
- **Architectural Health**: Evaluates layered separation, controller isolation, and error handling.
- **Verdict Resolver**: Enforces strict production blocking caps on critical vulnerabilities.

### Stage 7: Multi-Format Report Export
- **JSON**: Complete machine-readable scan payload via `/api/v1/analyses/{id}/export?format=json`.
- **Markdown**: Formatted executive summary, code composition, white-box complexity, black-box endpoints, and collapsible diffs.
- **HTML & PDF**: Standalone styled executive report with `@media print` white-paper styles.
