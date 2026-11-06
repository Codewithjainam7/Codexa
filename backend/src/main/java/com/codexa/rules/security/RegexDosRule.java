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
 * CR-REDOS-001: Regular Expression Denial of Service (ReDoS).
 * Detects exponential or polynomial catastrophic backtracking patterns inside compiled regular expressions.
 */
@Component
public class RegexDosRule implements AnalysisRule {

    private static final Pattern CATASTROPHIC_PATTERN = Pattern.compile(
            "(?:\\([^)]+[+*]\\)[+*]|\\([^)]+\\|[^)]+\\)[+*]|(?:\\[[^\\]]+\\][+*])[+*])"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-REDOS-001";
    }

    @Override
    public String getName() {
        return "Regular Expression Denial of Service (ReDoS)";
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
        return "A06:2021 - Vulnerable and Outdated Components";
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
            if ("compile".equals(methodName) && call.getArguments().isNonEmpty()) {
                String firstArg = call.getArgument(0).toString();
                if (CATASTROPHIC_PATTERN.matcher(firstArg).find()) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Catastrophic Backtracking Regular Expression (ReDoS)")
                            .description("Detected regular expression pattern containing nested quantifier repetitions (e.g. (a+)+ or (.*a)+). Evaluating untrusted input against this regex causes exponential backtracking, freezing the server thread (100% CPU utilization).")
                            .impact("Complete server thread lockup, request timeout cascades, and denial of service across all clients.")
                            .remediation("Refactor regex to eliminate nested quantifiers, utilize atomic grouping / possessive quantifiers (e.g. (?>a+) or a++), or enforce strict input length limits before matching.")
                            .suggestedFix("// Secure remediation:\nPattern pattern = Pattern.compile(\"^[a-zA-Z0-9_]{1,64}$\");")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of(
                                    "https://cwe.mitre.org/data/definitions/1333.html",
                                    "https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS"
                            ))
                            .build());
                }
            }
        });

        return findings;
    }
}
