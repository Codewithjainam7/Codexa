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
import java.util.regex.Pattern;

/**
 * Rule CR-PARAM-007: Detects unsanitized request parameter values reflected directly into HTTP response streams.
 */
@Component
public class ReflectedParameterXssRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    private static final Pattern REFLECTED_PARAM_PATTERN = Pattern.compile(
            "(?i)(?:\\.getWriter\\(\\)\\.(?:write|println|print)|\\.getOutputStream\\(\\)\\.write|res\\.send|res\\.write)\\s*\\(.*?(?:req\\.getParameter|request\\.getParameter|params?\\.get).*?\\)"
    );

    @Override
    public String getRuleId() {
        return "CR-PARAM-007";
    }

    @Override
    public String getName() {
        return "Insecure Parameter Reflection / Reflected XSS";
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
        return "A03:2021 - Injection";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        String relPath = parsedFile.getRelativePath();
        String lowerPath = relPath.toLowerCase();

        // Skip test files, rule classes, and fixtures
        if (lowerPath.contains("test") || lowerPath.contains("rule") || lowerPath.contains("fixture")) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
            String callText = mce.toString();
            if (REFLECTED_PARAM_PATTERN.matcher(callText).find()) {
                // If it already uses HtmlUtils or StringEscapeUtils, it is safely sanitized
                if (callText.contains("HtmlUtils.htmlEscape") || callText.contains("StringEscapeUtils.escapeHtml")
                        || callText.contains("encodeForHTML") || callText.contains("sanitiz")) {
                    continue;
                }

                int line = mce.getBegin().map(p -> p.line).orElse(1);
                int endLine = mce.getEnd().map(p -> p.line).orElse(line);
                String evidence = snippetExtractor.extractNodeSnippet(mce, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .title("Insecure Parameter Reflection into HTTP Output Stream")
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .owaspMapping(getOwaspMapping())
                        .filePath(relPath)
                        .startLine(line)
                        .endLine(endLine)
                        .evidence(evidence)
                        .description("Directly outputting unsanitized request parameter values (e.g. req.getParameter()) to the HTTP response stream leads to Reflected Cross-Site Scripting (XSS).")
                        .impact("Enables attackers to execute arbitrary malicious JavaScript in victims' browsers, hijack session cookies, and perform unauthorized state changes.")
                        .remediation("Encode all user-supplied parameter inputs with contextual HTML escaping (e.g. HtmlUtils.htmlEscape(param)) before writing to the response stream.")
                        .suggestedFix("String safeValue = org.springframework.web.util.HtmlUtils.htmlEscape(request.getParameter(\"input\"));\nresponse.getWriter().println(safeValue);")
                        .references(List.of("https://owasp.org/www-community/attacks/xss/", "https://cwe.mitre.org/data/definitions/79.html"))
                        .build());
            }
        }

        return findings;
    }
}
