# Codexa Custom Rule Engine & Plugin SPI Architecture

This document describes the extensible plugin architecture, Service Provider Interface (SPI), Spring bean auto-discovery mechanism, and isolation safeguards that allow developers and enterprise security teams to author custom static analysis rules in **Codexa**.

---

## 1. Architectural Philosophy & Extensibility Goals

Codexa is built with an open, modular rule execution pipeline. Enterprise organizations frequently have proprietary coding standards, internal framework invariants, and domain-specific regulatory rules (e.g., healthcare HIPAA data flows, proprietary fintech transaction wrappers).

The Codexa Rule Engine satisfies three core architectural principles:
1. **Zero-Core Intrusion**: Custom security rules are decoupled from the core analysis pipeline; adding or modifying a rule never requires modifying core pipeline classes.
2. **Spring Auto-Wiring & Java SPI Dual-Mode**: Rules can be registered either natively via Spring Boot's component scan (`@Component`) or externally via standard Java `ServiceLoader` (`META-INF/services/com.codexa.rules.api.AnalysisRule`).
3. **Execution Isolation & Deterministic Error Containment**: Individual rule exceptions are caught, logged, and isolated without crashing the overall analysis pipeline.

---

## 2. Core SPI: The `AnalysisRule` Contract

Every static rule, whether built-in or loaded via an external plugin, implements the `com.codexa.rules.api.AnalysisRule` interface:

```java
package com.codexa.rules.api;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import java.util.List;

public interface AnalysisRule {

    /**
     * Unique alphanumeric identifier (e.g., "CR-CUSTOM-001").
     */
    String getRuleId();

    /**
     * Human-readable rule title for reports and UI modals.
     */
    String getName();

    /**
     * Taxonomy category: SECURITY, QUALITY, PERFORMANCE, OPERATIONS, ARCHITECTURE.
     */
    Category getCategory();

    /**
     * Finding severity: CRITICAL, HIGH, MEDIUM, LOW, INFO.
     */
    Severity getSeverity();

    /**
     * Default confidence level: HIGH, MEDIUM, LOW.
     */
    Confidence getDefaultConfidence();

    /**
     * Reference standard mapping (e.g. "OWASP A01:2021", "CWE-89", "PCI-DSS 6.5.1").
     */
    String getOwaspMapping();

    /**
     * Markdown explanation and remediation rationale.
     */
    default String getDescription() {
        return getName();
    }

    /**
     * Core evaluation entrypoint invoked by the RuleEngineService.
     */
    List<RuleFinding> evaluate(RuleContext context);
}
```

---

## 3. Evaluation Context (`RuleContext`) & AST Access

When evaluating a rule, the engine passes a `RuleContext` containing rich context about the file and overall repository:

```java
public class RuleContext {
    private final ParsedJavaFile parsedJavaFile;
    private final PipelineContext pipelineContext;
    
    // Per-file AST compilation unit access
    public CompilationUnit getCompilationUnit() { ... }
    
    // Relative file path (e.g. "src/main/services/OrderService.java")
    public String getRelativePath() { ... }
    
    // Repository-wide configuration and file catalog
    public PipelineContext getPipelineContext() { ... }
}
```

### Context Evaluation Modes:
- **Per-File AST Evaluation**: For Java files, `parsedJavaFile.getCompilationUnit()` returns the parsed JavaParser AST `CompilationUnit`, allowing visitors and AST queries.
- **Repository-Level Evaluation**: For universal rules (TypeScript, Python, Go, Dockerfiles, YAML configs), `pipelineContext.getStagingDirectory()` provides the root path to all extracted workspace files.

---

## 4. Authoring a Custom Plugin Rule: Step-by-Step

### Example: Enforcing Transaction Timeout Annotation (`CR-FIN-001`)

Suppose a financial institution requires that every Spring `@Transactional` service method explicitly defines a `timeout = ...` attribute.

```java
package com.enterprise.rules;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionTimeoutRule implements AnalysisRule {

    @Override
    public String getRuleId() {
        return "CR-FIN-001";
    }

    @Override
    public String getName() {
        return "Missing Transaction Timeout in Financial Service";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
    }

    @Override
    public Severity getSeverity() {
        return Severity.HIGH;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "CWE-400"; // Uncontrolled Resource Consumption
    }

    @Override
    public String getDescription() {
        return "Financial transactions must specify an explicit timeout attribute to avoid thread starvation under database lock contention.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context.getCompilationUnit() == null) {
            return findings;
        }

        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(method -> {
            for (AnnotationExpr annotation : method.getAnnotations()) {
                if ("Transactional".equals(annotation.getNameAsString())) {
                    boolean hasTimeout = annotation.toString().contains("timeout");
                    if (!hasTimeout) {
                        findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .filePath(context.getRelativePath())
                            .startLine(method.getBegin().map(p -> p.line).orElse(1))
                            .endLine(method.getEnd().map(p -> p.line).orElse(1))
                            .snippet(annotation.toString())
                            .message("Method '" + method.getNameAsString() + "' has @Transactional without explicit timeout.")
                            .remediation("Add timeout attribute: @Transactional(timeout = 10)")
                            .evidence(method.getDeclarationAsString())
                            .build());
                    }
                }
            }
        });

        return findings;
    }
}
```

---

## 5. Plugin Discovery & Registration Modes

Codexa supports two discovery modes:

### Mode A: Spring Component Scan (In-Tree / Modular Monolith)
Place your class inside the `com.codexa.rules` package hierarchy (or configure `@ComponentScan(basePackages = {"com.codexa", "com.mycompany.rules"})`). Spring Boot automatically discovers, instantiates, and injects your bean into `RuleEngineService`:

```java
@Service
public class RuleEngineService {
    // Spring automatically populates all beans implementing AnalysisRule
    public RuleEngineService(List<AnalysisRule> rules) {
        this.rules = rules != null ? rules : List.of();
    }
}
```

### Mode B: Java ServiceLoader SPI (Out-of-Tree JAR Files)
For third-party or proprietary plugins delivered as pre-compiled `.jar` files:
1. Package the compiled rule class into a standard JAR.
2. Include the SPI descriptor in the JAR:
   ```
   META-INF/services/com.codexa.rules.api.AnalysisRule
   ```
   Containing the fully qualified class name:
   ```
   com.enterprise.rules.TransactionTimeoutRule
   ```
3. Drop the JAR file into Codexa's plugin directory:
   ```bash
   /opt/codexa/plugins/enterprise-rules-1.0.0.jar
   ```
4. On startup, Codexa's `PluginLoaderService` loads all external providers via `URLClassLoader` and registers them with `RuleEngineService`.

---

## 6. Fault Isolation & Error Containment

To prevent buggy or slow third-party rules from disrupting pipeline stability, Codexa enforces rigorous sandboxing:

1. **Catch-All Exception Boundaries**:
   If an AST query in a custom rule encounters an uncaught runtime exception (e.g. `NullPointerException`, `IndexOutOfBoundsException`), the engine catches the exception, logs a warning with the rule ID and filename, and proceeds with the next rule:
   ```java
   try {
       List<RuleFinding> ruleFindings = rule.evaluate(ruleContext);
       ...
   } catch (Exception e) {
       log.warn("Rule '{}' threw exception on file '{}': {}", 
           rule.getRuleId(), parsedFile.getRelativePath(), e.getMessage());
   }
   ```
2. **Deduplication Hash Guarantee**:
   All findings emitted by custom rules pass through SHA-256 deduplication hashing:
   $$\text{Hash} = \text{SHA-256}(\text{ruleId} \mathbin{\Vert} \text{filePath} \mathbin{\Vert} \text{startLine} \mathbin{\Vert} \text{endLine} \mathbin{\Vert} \text{evidence})$$
   This guarantees that duplicate findings generated across multiple passes or inner classes are deduplicated before persistence.

---

## 7. Testing Custom Rules

Custom rules can be tested in isolation without starting a Spring application context, using standard JUnit 5:

```java
class TransactionTimeoutRuleTest {

    private final TransactionTimeoutRule rule = new TransactionTimeoutRule();

    @Test
    void shouldDetectMissingTimeout() {
        String code = """
            public class PaymentService {
                @Transactional
                public void processPayment() {
                    // charge credit card
                }
            }
            """;
        
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(code).getResult().get();
        ParsedJavaFile file = new ParsedJavaFile("PaymentService.java", code, cu, List.of());
        RuleContext context = new RuleContext(file, null);

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-FIN-001", findings.get(0).ruleId());
        assertEquals(Severity.HIGH, findings.get(0).severity());
    }
}
```
