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
import java.util.Set;

@Component
public class SensitiveLoggingRule implements AnalysisRule {

    private static final Set<String> LOG_METHODS = Set.of("info", "debug", "warn", "error", "trace", "print", "println");
    private static final Set<String> SENSITIVE_TERMS = Set.of("password", "token", "apikey", "secret", "cardnumber", "cvv", "bearer", "authorization");

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-LOG-001";
    }

    @Override
    public String getName() {
        return "Sensitive Data Written to Application Logs";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A09:2025 - Security Logging and Alerting Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if (LOG_METHODS.contains(methodName) && call.getArguments().isNonEmpty()) {
                String argsText = call.getArguments().toString().toLowerCase();
                for (String term : SENSITIVE_TERMS) {
                    if (argsText.contains(term)) {
                        int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Potential sensitive variable ('" + term + "') logged")
                                .description("Detected potentially sensitive information (field containing '" + term + "') being written to application logs. Log files are often aggregated in plain-text storage accessible to wider internal audiences.")
                                .impact("Exposure of user passwords, access tokens, or payment details in centralized log management systems.")
                                .remediation("Never write passwords, tokens, or PII into logs. Mask or omit sensitive arguments before logging.")
                                .suggestedFix("log.info(\"User logged in successfully for user: {}\", userId);")
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://owasp.org/Top10/2025/A09_2025-Security_Logging_and_Alerting_Failures/"))
                                .build());
                        break;
                    }
                }
            }
        });

        return findings;
    }
}
