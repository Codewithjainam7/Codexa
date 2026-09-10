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
public class WeakHashAlgorithmRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();
    private static final Set<String> WEAK_ALGORITHMS = Set.of("MD5", "MD2", "SHA-1", "SHA1");

    @Override
    public String getRuleId() {
        return "CR-SEC-009";
    }

    @Override
    public String getName() {
        return "Weak Cryptographic Hash Algorithm (MD5 / SHA-1)";
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
        return "A02:2021 - Cryptographic Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (MethodCallExpr call : cu.findAll(MethodCallExpr.class)) {
            String name = call.getNameAsString();
            if ("getInstance".equals(name) && call.getArguments().isNonEmpty()) {
                call.getArgument(0).ifStringLiteralExpr(sle -> {
                    String algo = sle.getValue().trim().toUpperCase();
                    if (WEAK_ALGORITHMS.contains(algo)) {
                        int startLine = call.getBegin().map(p -> p.line).orElse(1);
                        int endLine = call.getEnd().map(p -> p.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Weak Hash Algorithm: " + algo)
                                .description("Detected collision-vulnerable hash algorithm '" + algo + "'. MD5 and SHA-1 have known practical collision attacks and are broken for security purposes.")
                                .impact("Collision attacks permit forged digital signatures, certificates, and compromised data integrity.")
                                .remediation("Upgrade to SHA-256 or SHA-3 (e.g. MessageDigest.getInstance(\"SHA-256\")). For password hashing use Argon2id or bcrypt.")
                                .suggestedFix("MessageDigest md = MessageDigest.getInstance(\"SHA-256\");")
                                .owaspMapping(getOwaspMapping())
                                .filePath(filename)
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://owasp.org/Top10/2021/A02_2021-Cryptographic_Failures/"))
                                .build());
                    }
                });
            }
        }

        return findings;
    }
}
