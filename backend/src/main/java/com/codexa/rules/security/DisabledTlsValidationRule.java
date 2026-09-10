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
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DisabledTlsValidationRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();
    private static final Set<String> TRUST_METHODS = Set.of("checkServerTrusted", "checkClientTrusted");

    @Override
    public String getRuleId() {
        return "CR-SEC-010";
    }

    @Override
    public String getName() {
        return "Disabled SSL/TLS Certificate Validation";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.CRITICAL;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A07:2021 - Identification and Authentication Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
            String name = md.getNameAsString();
            if (TRUST_METHODS.contains(name) && md.getBody().isPresent()) {
                if (md.getBody().get().getStatements().isEmpty()) {
                    int startLine = md.getBegin().map(p -> p.line).orElse(1);
                    int endLine = md.getEnd().map(p -> p.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(md, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Disabled TLS Certificate Check: " + name)
                            .description("Method '" + name + "' contains an empty body that skips SSL/TLS certificate validation, enabling trivial Man-in-the-Middle (MitM) attacks.")
                            .impact("Complete loss of transport confidentiality and integrity; adversaries can intercept and alter encrypted network traffic.")
                            .remediation("Do not bypass TLS verification. Use standard system trust stores or import explicit CA certificates into Java keystore.")
                            .suggestedFix("// Remove custom TrustManager and use system default SSLContext:\nSSLContext sslContext = SSLContext.getDefault();")
                            .owaspMapping(getOwaspMapping())
                            .filePath(filename)
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://cwe.mitre.org/data/definitions/295.html"))
                            .build());
                }
            }
        }

        return findings;
    }
}
