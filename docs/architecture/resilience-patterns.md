# Codexa Resilience Patterns & Fault Isolation Architecture

This document documents the fault-tolerance and resilience design patterns implemented across Codexa to prevent unexpected scanner crashes, network dropouts, or malicious repository payloads from impacting availability.

---

## 1. Resilience Philosophy

A static analysis engine frequently encounters untrusted, malformed, or hostile inputs (e.g. truncated source files, syntax errors, decompression bombs, or external API timeouts). Codexa is engineered to be **resilient by design**: a failure in an individual file, rule, or external AI API must never crash the analysis job or compromise platform availability.

---

## 2. Key Resilience Patterns

### A. Per-File AST Sandbox & Isolation
When parsing thousands of files, an unsupported language construct or malformed syntax must not abort the overall pipeline:
```java
try {
    ParseResult<CompilationUnit> result = parser.parse(source);
    if (result.isSuccessful()) {
        analyzeCompilationUnit(result.getResult().get(), context);
    } else {
        log.warn("Syntax parse warning in {}: fall back to polyglot regex engine", file.getFileName());
        fallbackPolyglotEngine.scan(file, context);
    }
} catch (Exception e) {
    log.error("Recovered from parser fault in file {}: continuing scan", file, e);
    // Continue scanning next file without pipeline crash
}
```

### B. AI Remediation Circuit Breaker & Two-Tier Fallback
If the external LLM provider (OpenRouter / NVIDIA) experiences network latency, rate limits, or HTTP 5xx errors:
1. Codexa's `AIExplanationService` catches the exception.
2. It switches immediately (&lt; 1ms) to `DeterministicExplanationTemplateService`.
3. High-quality offline remediation diffs are generated without stalling user requests.

### C. Graceful Degradation on Client Storage
Frontend safeStorage wraps HTML5 `localStorage` in `try-catch` blocks, gracefully falling back to transient in-memory arrays when private browsing disables persistent cookies/storage.
