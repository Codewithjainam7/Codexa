# Codexa Engineering Coding Standards & Architecture Guidelines

This specification defines the software engineering standards, language idioms, defensive programming patterns, and code formatting rules required for contributions to the **Codexa** codebase.

---

## 1. Java 21 LTS Language Standards & Idioms

All backend code targets **Java 21 LTS** and must adhere to modern idiomatic Java design patterns:

### A. Immutable Data Carriers (`record`)
Use Java records for all Data Transfer Objects (DTOs), event payloads, API responses, and value objects:
```java
// Correct: Compact, immutable record with factory methods
public record RuleFinding(
    String ruleId,
    Category category,
    Severity severity,
    Confidence confidence,
    String filePath,
    int startLine,
    int endLine,
    String snippet,
    String message,
    String remediation
) {}
```

### B. Pattern Matching & Switch Expressions
Prefer pattern matching for `instanceof` and modern exhaustive switch expressions:
```java
// Correct: Pattern matching with switch expression
int percent = switch (stageName) {
    case "JAVA_AST_PARSING" -> 40;
    case "SECURITY_AND_QUALITY_RULES" -> 65;
    case "AI_EXPLANATION_AND_REMEDIATION" -> 85;
    case "PRIORITIZATION_AND_SCORING" -> 95;
    default -> 70;
};
```

### C. Concurrency & Virtual Thread Best Practices
- **Never use `Thread.sleep()` in production code** without checking interruption status.
- **Avoid synchronized blocks** on I/O operations; prefer `ReentrantLock` to prevent virtual thread carrier pinning.
- **Never instantiate unbounded thread pools** (`Executors.newCachedThreadPool()` is strictly flagged by `CR-QUAL-001`).

---

## 2. Static Analysis Rule Development Standards

Every rule added to `com.codexa.rules` must adhere to these core invariants:

1. **Implement `com.codexa.rules.api.AnalysisRule`**: Declare explicit metadata (`getRuleId()`, `getCategory()`, `getSeverity()`, `getDefaultConfidence()`, `getOwaspMapping()`).
2. **Defensive AST Traversal**: Always extend `VoidVisitorAdapter` or `ModifierVisitor`. Never assume child nodes exist without checking `isPresent()`.
3. **Evidence Snippet Sanitization**: When capturing code evidence, pass snippets through `SecretMaskingSecurityService` to prevent leaking plain-text secrets into reports (CWE-532).
4. **Deterministic Deduplication Hash**: Ensure findings can be uniquely fingerprinted using:
   $$\text{Hash} = \text{SHA-256}(\text{ruleId} \mathbin{\Vert} \text{filePath} \mathbin{\Vert} \text{startLine} \mathbin{\Vert} \text{evidence})$$
5. **No Network Egress in Rules**: Rules must execute purely in-memory on the supplied `RuleContext`. Never initiate HTTP, database, or external process calls within an AST rule evaluation loop.

---

## 3. Exception Handling & Error Boundaries

- **Never Swallow Exceptions**: Catching `Exception` and doing nothing is strictly forbidden (`CR-QUAL-006`). At minimum, log a warning with context.
- **Domain API Exceptions**: Throw `ApiException(HttpStatus, String errorCode, String message)` for all client-facing failures.
- **Resource Management**: Use try-with-resources for all `InputStream`, `OutputStream`, `ZipFile`, and database transactions.

---

## 4. Frontend Standards (React 18 & Tailwind CSS)

- **Functional Components & Hooks**: Class components are forbidden. Use functional components with typed hooks (`useState`, `useEffect`, `useCallback`, `useMemo`).
- **Monochromatic Luxury Design System**: Follow guidelines in ADR-0006:
  - Base colors: `#000000`, `neutral-900`, `neutral-800`.
  - Functional highlights: `emerald-400` / `emerald-500` strictly for verified security elements.
- **Performance**: Heavy calculation or list filtering (e.g., searching 10,000 findings) must be wrapped in `useMemo` or deferred via `useDeferredValue`.
- **Accessibility**: All interactive buttons and inputs must include `aria-label` and visible keyboard focus rings (`focus:ring-2 focus:ring-emerald-500`).

---

## 5. Git Commit & Pull Request Standards

Codexa follows the **Conventional Commits 1.0.0** specification:

```
<type>(<scope>): <short imperative summary>

[optional body explaining architectural rationale]

[optional footer with issue references]
```

### Approved Types:
- `feat`: New analysis rule, API endpoint, or UI feature.
- `fix`: Bug fix, false-positive elimination, or security patch.
- `docs`: Documentation updates, specs, or ADR additions.
- `perf`: Algorithmic optimization, memory reduction, or cache enhancement.
- `refactor`: Code restructuring without functional behavior changes.
- `test`: Adding or expanding unit/integration test suites.
- `chore`: Dependency updates, build script adjustments.

---

## 6. Code Formatting & Automated Linting

Before submitting pull requests, run automated code formatting:

```bash
# Format backend Java code (Spotless / Google Java Format)
cd backend && mvn spotless:apply

# Format and lint frontend code (ESLint / Prettier)
cd frontend && npm run lint && npm run format
```
