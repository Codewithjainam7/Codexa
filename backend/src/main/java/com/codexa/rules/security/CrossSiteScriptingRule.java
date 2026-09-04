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
public class CrossSiteScriptingRule implements AnalysisRule {

    private static final Set<String> WRITE_METHODS = Set.of("write", "print", "println");
    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-XSS-001";
    }

    @Override
    public String getName() {
        return "Cross-Site Scripting (XSS) via Unescaped Output";
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
        return "A05:2025 - Injection";
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
            if (WRITE_METHODS.contains(methodName) && call.getScope().isPresent()) {
                String scopeStr = call.getScope().get().toString();
                if (scopeStr.contains("getWriter") || scopeStr.contains("response") || scopeStr.contains("out")) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Direct unescaped response writing (Reflected XSS risk)")
                            .description("Detected direct writing of potentially untrusted data to HTTP response stream via '" + scopeStr + "." + methodName + "()'. Without contextual HTML entity escaping, attackers can execute arbitrary JavaScript in victim browsers.")
                            .impact("Session hijacking, credential theft, and unauthorized actions executed on behalf of authenticated users.")
                            .remediation("Use structured template engines with automatic HTML escaping (e.g. Thymeleaf, React) or sanitize/encode outputs using HtmlUtils.htmlEscape().")
                            .suggestedFix("response.getWriter().write(HtmlUtils.htmlEscape(userInput));")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://owasp.org/Top10/2025/A05_2025-Injection/", "https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html"))
                            .build());
                }
            }
        });

        return findings;
    }
}
