# Tutorial: Authoring a New Static Analysis Rule in Codexa

This practical guide walks through implementing and testing a new deterministic Java AST rule in Codexa from scratch.

---

## 1. Anatomy of an `AnalysisRule`

Every rule implements `com.codexa.rules.api.AnalysisRule` and is registered as a Spring `@Component`:

```java
package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InsecureCookieRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SEC-015";
    }

    @Override
    public String getName() {
        return "Insecure Cookie Flag Configuration";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
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
        return "A05:2021-Security Misconfiguration";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        // Inspect all MethodCallExpr for setSecure(false) or setHttpOnly(false)
        for (MethodCallExpr call : cu.findAll(MethodCallExpr.class)) {
            String methodName = call.getNameAsString();
            if (("setSecure".equals(methodName) || "setHttpOnly".equals(methodName))
                    && call.getArguments().size() == 1
                    && call.getArgument(0).toString().equals("false")) {

                int startLine = call.getBegin().map(p -> p.line).orElse(1);
                int endLine = call.getEnd().map(p -> p.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Cookie without " + methodName.substring(3) + " protection")
                        .description("Disabling " + methodName.substring(3) + " allows cookies to be intercepted in transit or stolen via XSS.")
                        .impact("Session hijacking and credential theft.")
                        .remediation("Call cookie." + methodName + "(true).")
                        .suggestedFix("cookie." + methodName + "(true);")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .build());
            }
        }

        return findings;
    }
}
```

---

## 2. Writing the Corresponding Unit Test

Create `InsecureCookieRuleTest.java` in `backend/src/test/java/com/codexa/rules/security/`:

```java
package com.codexa.rules.security;

import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsecureCookieRuleTest {

    private final InsecureCookieRule rule = new InsecureCookieRule();
    private final JavaAstParserService parser = new JavaAstParserService();

    @Test
    void testFlagsInsecureCookieCall() {
        String code = """
            package com.example;
            import jakarta.servlet.http.Cookie;
            public class AuthController {
                public void setCookie(Cookie cookie) {
                    cookie.setHttpOnly(false); // Violation
                }
            }
            """;

        ParsedJavaFile file = parser.parseDirect("com/example/AuthController.java", code);
        RuleContext context = new RuleContext(file, Path.of("."));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-SEC-015", findings.get(0).getRuleId());
        assertEquals(Severity.HIGH, findings.get(0).getSeverity());
    }

    @Test
    void testIgnoresSecureCookieCall() {
        String code = """
            package com.example;
            import jakarta.servlet.http.Cookie;
            public class AuthController {
                public void setCookie(Cookie cookie) {
                    cookie.setHttpOnly(true); // Clean
                }
            }
            """;

        ParsedJavaFile file = parser.parseDirect("com/example/AuthController.java", code);
        RuleContext context = new RuleContext(file, Path.of("."));

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.isEmpty());
    }
}
```

---

## 3. Best Practices for Rule Developers

1. **Avoid AST node casting without `instanceof` checks**: JavaParser expressions can be method calls, lambda expressions, or field accesses.
2. **Defend against nulls**: Check `getBegin()` and `getEnd()` safely with `Optional.map()`.
3. **Always include both Positive and Negative unit test cases**: Proves detection works and guarantees zero false positives on remediated code.
