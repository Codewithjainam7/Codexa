# AST Custom Rule Authoring Tutorial

Learn how to write deterministic JavaParser AST static analysis rules in Codexa.

## Step-by-Step Rule Implementation

1. **Implement `AnalysisRule`**:
```java
@Component
public class HardcodedCredentialsRule implements AnalysisRule {

    @Override
    public String getRuleId() {
        return "CR-SEC-011";
    }

    @Override
    public String getRuleName() {
        return "Hardcoded Database Credentials";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        CompilationUnit cu = context.getParsedJavaFile().getCompilationUnit();

        cu.findAll(StringLiteralExpr.class).forEach(literal -> {
            if (literal.getValue().startsWith("jdbc:mysql://") && literal.getValue().contains("password=")) {
                findings.add(new RuleFinding(
                    getRuleId(),
                    getRuleName(),
                    "Hardcoded password detected in JDBC connection string.",
                    context.getParsedJavaFile().getRelativePath(),
                    literal.getRange().map(r -> r.begin.line).orElse(1),
                    literal.getRange().map(r -> r.end.line).orElse(1),
                    "CRITICAL",
                    "SECURITY",
                    literal.toString(),
                    "Externalize connection credentials into environment variables."
                ));
            }
        });
        return findings;
    }
}
```

2. **Register & Test**:
Spring Boot automatically injects any bean implementing `AnalysisRule` into `RuleEngineService`.
