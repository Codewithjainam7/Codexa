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
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeakCryptographyRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-CRYPTO-001";
    }

    @Override
    public String getName() {
        return "Weak Cryptographic Algorithm or Insecure Random Generator";
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
        return "A04:2025 - Cryptographic Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        // 1. Check Cipher algorithms (DES, ECB mode)
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if ("getInstance".equals(call.getNameAsString()) && call.getScope().map(s -> s.toString().contains("Cipher")).orElse(false)) {
                if (call.getArguments().isNonEmpty() && call.getArgument(0) instanceof StringLiteralExpr str) {
                    String cipherName = str.getValue().toUpperCase();
                    if (cipherName.contains("DES") || cipherName.contains("/ECB/")) {
                        int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Insecure cipher transformation: " + str.getValue())
                                .description("Detected insecure cipher transformation '" + str.getValue() + "'. DES is broken, and ECB mode produces identical ciphertext for identical plaintext blocks, leaking pattern information.")
                                .impact("Loss of ciphertext confidentiality and data exposure.")
                                .remediation("Use AES in GCM authenticated mode (e.g., 'AES/GCM/NoPadding') with a 256-bit key and unique IV.")
                                .suggestedFix("Cipher cipher = Cipher.getInstance(\"AES/GCM/NoPadding\");")
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://owasp.org/Top10/2025/A04_2025-Cryptographic_Failures/", "https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html"))
                                .build());
                    }
                }
            }
        });

        // 2. Check insecure java.util.Random for token / secret generation
        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            if ("Random".equals(creation.getTypeAsString())) {
                String parentCode = creation.getParentNode().map(Object::toString).orElse("").toLowerCase();
                if (parentCode.contains("token") || parentCode.contains("session") || parentCode.contains("otp") || parentCode.contains("key") || parentCode.contains("secret")) {
                    int startLine = creation.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = creation.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(creation, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Insecure PRNG used for security-sensitive token/key")
                            .description("Detected 'java.util.Random' used in a context involving tokens, OTPs, or keys. 'java.util.Random' is linear and predictable.")
                            .impact("Attackers can predict generated session tokens, OTPs, or reset keys.")
                            .remediation("Use java.security.SecureRandom for all cryptographically sensitive pseudo-random number generation.")
                            .suggestedFix("SecureRandom random = new SecureRandom();")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://owasp.org/Top10/2025/A04_2025-Cryptographic_Failures/"))
                            .build());
                }
            }
        });

        return findings;
    }
}
