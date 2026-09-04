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
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class WeakPasswordStorageRule implements AnalysisRule {

    private static final Set<String> WEAK_HASHES = Set.of("MD5", "SHA-1", "SHA1", "MD2");
    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-PASS-001";
    }

    @Override
    public String getName() {
        return "Weak or Insecure Password Storage";
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
        return "A07:2025 - Authentication Failures";
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
            if ("getInstance".equals(methodName) && call.getScope().map(s -> s.toString().contains("MessageDigest")).orElse(false)) {
                if (call.getArguments().isNonEmpty() && call.getArgument(0) instanceof StringLiteralExpr strExpr) {
                    String algo = strExpr.getValue().toUpperCase();
                    if (WEAK_HASHES.contains(algo)) {
                        int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Insecure cryptographic hash algorithm (" + algo + ") used")
                                .description("Detected use of legacy/broken hash algorithm '" + algo + "'. MD5 and SHA-1 are cryptographically broken and vulnerable to fast collision and pre-image attacks.")
                                .impact("Fast cracking of stored passwords using precomputed rainbow tables and GPU brute-force.")
                                .remediation("Use an adaptive, salted key-derivation function such as BCrypt, SCrypt, or Argon2 (e.g. BCryptPasswordEncoder in Spring Security).")
                                .suggestedFix("PasswordEncoder encoder = new BCryptPasswordEncoder();\nString hash = encoder.encode(rawPassword);")
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://owasp.org/Top10/2025/A07_2025-Authentication_Failures/", "https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html"))
                                .build());
                    }
                }
            }
        });

        return findings;
    }
}
