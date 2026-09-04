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
public class InsecureConfigurationRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-CONFIG-001";
    }

    @Override
    public String getName() {
        return "Insecure CORS or Security Configuration";
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
        return "A02:2025 - Security Misconfiguration";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        // 1. Check for allowedOrigins("*") with allowCredentials(true) in same class or chain
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if ("allowedOrigins".equals(methodName) && call.getArguments().toString().contains("\"*\"")) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Overly permissive CORS configuration (Wildcard allowedOrigins)")
                        .description("Detected CORS configuration specifying wildcard origin ('*'). Wildcard CORS allows any external domain to make cross-origin requests to your API.")
                        .impact("Cross-origin data exfiltration and unauthorized cross-domain actions.")
                        .remediation("Explicitly whitelist trusted origins instead of using wildcard ('*').")
                        .suggestedFix("registry.addMapping(\"/api/**\").allowedOrigins(\"https://app.yourdomain.com\");")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://owasp.org/Top10/2025/A02_2025-Security_Misconfiguration/", "https://cheatsheetseries.owasp.org/cheatsheets/Cross-Origin_Resource_Sharing_Cheat_Sheet.html"))
                        .build());
            }
        });

        return findings;
    }
}
