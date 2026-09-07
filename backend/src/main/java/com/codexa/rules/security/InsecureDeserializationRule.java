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
public class InsecureDeserializationRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SEC-005";
    }

    @Override
    public String getName() {
        return "Insecure Object Deserialization";
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
        return "A08:2021-Software and Data Integrity Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        // 1. ObjectInputStream.readObject() or readUnshared()
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if ("readObject".equals(methodName) || "readUnshared".equals(methodName)) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Unsafe Java Deserialization via '" + methodName + "()'")
                        .description("Directly deserializing byte streams with 'ObjectInputStream." + methodName + "()' without class-filtering gadget chains allows Remote Code Execution (RCE) via gadget payloads (CWE-502).")
                        .impact("Complete arbitrary Remote Code Execution (RCE) and system takeover.")
                        .remediation("Avoid Java native serialization. Use safe data formats like JSON (Jackson with safe typing) or implement a ValidatingObjectInputStream.")
                        .suggestedFix("""
                                // Migrate from native ObjectInputStream to safe JSON or enforce class allowlist
                                ValidatingObjectInputStream ois = new ValidatingObjectInputStream(in);
                                ois.accept(SafeDomainClass.class);
                                """)
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of(
                                "https://owasp.org/www-community/vulnerabilities/Deserialization_of_untrusted_data",
                                "https://cwe.mitre.org/data/definitions/502.html"
                        ))
                        .build());
            }

            // 2. Jackson ObjectMapper.enableDefaultTyping()
            if ("enableDefaultTyping".equals(methodName)) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(Severity.HIGH)
                        .confidence(Confidence.HIGH)
                        .title("Polymorphic Deserialization via 'enableDefaultTyping()'")
                        .description("Enabling default typing on Jackson ObjectMapper allows attackers to instantiate arbitrary Java classes from serialized JSON (CWE-502).")
                        .impact("Potential Remote Code Execution via polymorphic gadget chains.")
                        .remediation("Disable default typing. Use explicit @JsonTypeInfo or BasicPolymorphicTypeValidator.")
                        .suggestedFix("""
                                BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                    .allowIfBaseType("com.example.model")
                                    .build();
                                mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
                                """)
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://cwe.mitre.org/data/definitions/502.html"))
                        .build());
            }
        });

        // 3. XMLDecoder creation
        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            if ("XMLDecoder".equals(creation.getTypeAsString())) {
                int startLine = creation.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = creation.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(creation, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(Severity.CRITICAL)
                        .confidence(Confidence.HIGH)
                        .title("Insecure Deserialization via 'XMLDecoder'")
                        .description("XMLDecoder can execute arbitrary Java methods and constructors during XML parsing (CWE-502).")
                        .impact("Remote Code Execution (RCE).")
                        .remediation("Replace XMLDecoder with safe XML or JSON parsers.")
                        .suggestedFix("// Replace XMLDecoder with safe Jackson/Gson parser")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://cwe.mitre.org/data/definitions/502.html"))
                        .build());
            }
        });

        return findings;
    }
}
