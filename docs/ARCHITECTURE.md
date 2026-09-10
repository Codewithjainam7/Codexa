# Codexa Platform Architecture

```mermaid
graph TD
    Client[Web Browser / CLI / CI Action] -->|Multipart ZIP or Git URL| Gateway[AnalysisJobController]
    Gateway --> Ingest[SecureZipExtractor]
    Ingest -->|Canonical Temp Files| Parser[JavaAstParserService]
    Parser -->|AST CompilationUnits| RuleEngine[Deterministic Rule Engine]
    RuleEngine -->|Finding Candidates| AILayer[Nvidia Nemotron 550B Remediation]
    AILayer --> Scoring[ReadinessScoringEngine]
    Scoring --> Diagnostics[ProjectDiagnosticsCollector]
    Diagnostics --> Export[ReportExportService: JSON / HTML / MD / CSV / SARIF]
```

## Core Architectural Guarantees
1. **Deterministic Execution:** The rule evaluation engine is 100% deterministic and operates on AST syntax graphs.
2. **Zero Ingress Vulnerability:** Zip Slip, Tar bomb, and depth traversal attacks are rejected before disk extraction.
3. **Memory Bounded:** Dynamic headroom check guarantees safe streaming on large (up to 3GB) codebases.
