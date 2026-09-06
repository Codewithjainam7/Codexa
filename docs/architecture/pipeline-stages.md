# Codexa High-Performance Analysis Pipeline

Codexa processes source code archives and Git repositories through a multi-stage deterministic pipeline engineered for blazing speed and rigorous security analysis.

```
       [ Uploaded Archive / Git URL ]
                    │
                    ▼
     [ Stage 1: Sandboxed Ingestion ]
    (Path Traversal, Quotas, 64KB Buffer)
                    │
                    ▼
   [ Stage 2: Parallel AST Parsing ]
 (ThreadLocal JavaParser, ForkJoinPool)
                    │
                    ▼
   [ Stage 3: Rule Evaluation Engine ]
 (19+ OWASP Deterministic AST Visitors)
                    │
                    ▼
  [ Stage 4: AI Enrichment & Fallback ]
  (Top-3 Findings LLM + Template Fallback)
                    │
                    ▼
 [ Stage 5: Multi-Factor Risk Scoring ]
 (Security, Quality, Ops, Maintainability, Debt)
                    │
                    ▼
 [ Stage 6: Report Generation & Export ]
 (JSON API, Markdown, Standalone HTML & PDF)
```

## Stage Breakdown

### Stage 1: Sandboxed Ingestion
- **Zip Slip Mitigation**: Enforces canonical destination containment within staging root.
- **Enterprise Boundaries**: Limits up to 500MB compressed, 1000MB extracted, 20,000 files.
- **Selective Filtering**: Discards binaries, node_modules, build outputs, and vendor trees.

### Stage 2: Parallel AST Parsing
- Parses source files into structured Abstract Syntax Trees concurrently using a `ForkJoinPool` with `ThreadLocal<JavaParser>` instances.
- Zero parser re-instantiation overhead across threads.

### Stage 3: Deterministic Rule Evaluation
- Analyzes AST nodes across 19 OWASP Top 10 rules including SQL injection, command injection, secret leakage, and missing access control.
- Deterministic detection produces zero false negatives on known patterns.

### Stage 4: AI Enrichment & Remediation Diff Generation
- Prioritizes top-3 highest severity findings for deep neural LLM remediation generation.
- Remaining findings use instant deterministic remediation templates, avoiding latency spikes and rate limits.

### Stage 5: Production Readiness & Quality Scoring
- **Overall Score**: `0.60 * Security + 0.25 * Quality + 0.15 * Operations`
- **Maintainability Index**: Evaluates code complexity, swallowed exceptions, and smell count.
- **Architectural Health**: Penalizes structural anti-patterns, cyclic complexity, and nesting.
- **Verdict Resolver**: Enforces strict production blocking caps on critical vulnerabilities.

### Stage 6: Multi-Format Report Export
- **JSON**: Machine-readable full scan output via `/api/v1/analyses/{id}/export?format=json`.
- **Markdown**: Formatted executive summary table, findings catalog, and remediation snippets.
- **HTML**: Standalone responsive report with embedded `@media print` styles and one-click "Print / Save as PDF" button.
