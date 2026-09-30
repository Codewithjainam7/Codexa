# Deterministic AST Parser Engine & Rule Lifecycle

## 1. Overview
At the core of Codexa's white-box static analysis is a high-throughput, deterministic Abstract Syntax Tree (AST) engine powered by **JavaParser** and multi-language universal parsers. 

Unlike simple regex grep matchers that suffer from high false-positive rates (flagging comments, string literals, and test utilities), Codexa parses code into typed syntax trees, resolves symbol scopes, and evaluates rules using the **Visitor Pattern**.

---

## 2. AST Analysis Pipeline Lifecycle

```
[Target Source File] ──> [Lexer & Tokenizer] ──> [CompilationUnit AST]
                                                         │
                                                         ▼
                                               [RuleEngineService]
                                                         │
                                 ┌───────────────────────┴───────────────────────┐
                                 ▼                                               ▼
                     [Single-File AST Rules]                           [Repository-Wide Rules]
                     - SqlInjectionRule                                - DependencyRiskRule
                     - CommandInjectionRule                            - IacSecurityRule
                     - ApiSecurityTop10Rule                            - LicenseComplianceRule
                                 │                                               │
                                 └───────────────────────┬───────────────────────┘
                                                         ▼
                                               [RuleFinding Collection]
                                                         ▼
                                             [Deterministic Scoring Engine]
```

---

## 3. The `AnalysisRule` Contract

Every rule implements the unified [`AnalysisRule`](file:///F:/Codexa/backend/src/main/java/com/codexa/rules/api/AnalysisRule.java) interface:

```java
public interface AnalysisRule {
    String getId();
    String getName();
    String getDescription();
    Severity getSeverity();
    Category getCategory();
    
    // Repository-wide rules run once per repository context (IaC, Licenses, SBOM)
    default boolean isRepositoryWide() {
        return false;
    }

    List<RuleFinding> evaluate(RuleContext context);
}
```

---

## 4. AST Visitor Implementation Example

Rules extend JavaParser's `VoidVisitorAdapter` to traverse specific AST nodes:

```java
cu.accept(new VoidVisitorAdapter<Void>() {
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        if ("executeQuery".equals(n.getNameAsString()) || "execute".equals(n.getNameAsString())) {
            // Check argument expression types for BinaryExpr string concatenation
            if (!n.getArguments().isEmpty() && n.getArguments().get(0) instanceof BinaryExpr) {
                findings.add(new RuleFinding(...));
            }
        }
    }
}, null);
```
