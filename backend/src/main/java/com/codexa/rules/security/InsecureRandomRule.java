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
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InsecureRandomRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SEC-007";
    }

    @Override
    public String getName() {
        return "Insecure Pseudo-Random Number Generator (PRNG)";
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

        // 1. Check for 'new Random()'
        for (ObjectCreationExpr oce : cu.findAll(ObjectCreationExpr.class)) {
            String typeName = oce.getTypeAsString();
            if ("Random".equals(typeName) || "java.util.Random".equals(typeName)) {
                int line = oce.getBegin().map(p -> p.line).orElse(1);
                int endLine = oce.getEnd().map(p -> p.line).orElse(line);
                String evidence = snippetExtractor.extractNodeSnippet(oce, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .title("Insecure java.util.Random Instance")
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .owaspMapping(getOwaspMapping())
                        .filePath(filename)
                        .startLine(line)
                        .endLine(endLine)
                        .evidence(evidence)
                        .description("java.util.Random uses a linear congruential formula that is mathematically predictable. Use java.security.SecureRandom for tokens, keys, and security contexts.")
                        .impact("Predictable pseudorandom generation leading to token spoofing, session hijacking, or replay attacks.")
                        .remediation("Use java.security.SecureRandom for all security-sensitive random value generation.")
                        .suggestedFix("SecureRandom random = new SecureRandom();")
                        .references(List.of("https://owasp.org/Top10/2021/A02_2021-Cryptographic_Failures/"))
                        .build());
            }
        }

        // 2. Check for Math.random()
        for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
            if ("random".equals(mce.getNameAsString())) {
                boolean isMath = mce.getScope()
                        .map(s -> "Math".equals(s.toString()) || "java.lang.Math".equals(s.toString()))
                        .orElse(false);
                if (isMath) {
                    int line = mce.getBegin().map(p -> p.line).orElse(1);
                    int endLine = mce.getEnd().map(p -> p.line).orElse(line);
                    String evidence = snippetExtractor.extractNodeSnippet(mce, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .title("Insecure Math.random() Call")
                            .category(getCategory())
                            .severity(Severity.MEDIUM)
                            .confidence(getDefaultConfidence())
                            .owaspMapping(getOwaspMapping())
                            .filePath(filename)
                            .startLine(line)
                            .endLine(endLine)
                            .evidence(evidence)
                            .description("Math.random() is backed by an internal java.util.Random generator and is unsuitable for security-sensitive entropy generation.")
                            .impact("Predictable pseudorandom output unsuitable for security controls.")
                            .remediation("Use SecureRandom.getInstanceStrong() or SecureRandom instead of Math.random().")
                            .suggestedFix("SecureRandom.getInstanceStrong().nextDouble();")
                            .references(List.of("https://owasp.org/Top10/2021/A02_2021-Cryptographic_Failures/"))
                            .build());
                }
            }
        }

        return findings;
    }
}
