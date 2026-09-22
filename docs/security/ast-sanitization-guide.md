# Codexa Defensive AST Traversal & Sanitization Guide

This guide details the security controls and defensive bounds applied when constructing and traversing Abstract Syntax Trees (ASTs) over untrusted, adversarial source code.

---

## 1. Threat Model for Static Parsers

Exposing static analysis parsers to untrusted code introduces unique parser-level vulnerabilities:
1. **Parser Stack Overflow (CWE-674)**: Deeply recursive expressions (e.g. `1 + (1 + (1 + ...)))` nested 10,000 times) can exhaust the JVM thread stack.
2. **Infinite Loops / Regex ReDoS**: Maliciously crafted source lines engineered to trigger exponential backtracking in regex scanners.
3. **Billion Laughs / XML Entity Expansion**: Malformed configuration files attempting recursive entity expansion.

---

## 2. Defensive Countermeasures

### A. Parser Depth Limitation
JavaParser is initialized with strict token limit constraints and maximum expression depth guards. Files containing syntax structures exceeding depth limits are aborted cleanly:
```java
ParserConfiguration config = new ParserConfiguration()
    .setTabSize(4)
    .setAttributeComments(false)
    .setStoreTokens(true);
```

### B. Timeout Bounding
Individual file parsing tasks are bounded by a strict 5,000ms deadline. If an adversarial file causes a parser stall:
```java
CompletableFuture<ParseResult<CompilationUnit>> future = CompletableFuture.supplyAsync(() -> parser.parse(source), pool);
try {
    ParseResult<CompilationUnit> result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
    log.warn("AST parser timeout on file: skipping deep traversal");
}
```

### C. Safe XML & YAML Parsing
All configuration parsers explicitly disable external entities (XXE defense) and disallow dynamic constructor tags:
- `XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES = false`
- `yaml.safe_load()` in Python; `SafeConstructor` in Java SnakeYAML.
