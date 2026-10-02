# Multi-Stage Ingestion & Analysis Orchestrator Specification

## 1. Overview
The [`AnalysisPipeline`](file:///F:/Codexa/backend/src/main/java/com/codexa/analysis/pipeline/AnalysisPipeline.java) coordinates the execution of static analysis across 6 deterministic stages.

---

## 2. Pipeline Execution Stages

1. **INGESTION**: Validates repository URL or unpacks ZIP archive with Zip Bomb protection (max 10,000 files, max 3 GB decompressed).
2. **DISCOVERY**: Scans directory tree, filters binary/ignored files, and catalogs source languages.
3. **AST_PARSING**: Builds JavaParser CompilationUnits and token streams using virtual thread workers.
4. **RULE_EVALUATION**: Executes single-file AST rules and repository-wide checks (IaC, License compliance).
5. **SCORING**: Computes multi-dimensional readiness index and categorizes overall production verdict.
6. **REPORT_GENERATION**: Emits HTML, PDF, Markdown, SARIF, and dynamic vector SVG trust badges.
