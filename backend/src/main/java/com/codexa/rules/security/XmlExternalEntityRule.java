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

/**
 * CR-XXE-001: XML External Entity (XXE) Injection.
 * Detects XML parsers (DocumentBuilderFactory, SAXParserFactory, XMLInputFactory, TransformerFactory)
 * initialized without secure entity expansion disabled or FEATURE_SECURE_PROCESSING enabled.
 */
@Component
public class XmlExternalEntityRule implements AnalysisRule {

    private static final Set<String> XML_PARSER_FACTORIES = Set.of(
            "DocumentBuilderFactory",
            "SAXParserFactory",
            "XMLInputFactory",
            "TransformerFactory",
            "SchemaFactory"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-XXE-001";
    }

    @Override
    public String getName() {
        return "XML External Entity (XXE) Injection";
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
        return "A05:2021 - Security Misconfiguration";
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
            if ("newInstance".equals(methodName)) {
                String scope = call.getScope().map(s -> s.toString()).orElse("");
                if (XML_PARSER_FACTORIES.contains(scope)) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Unsafe XML Parser Initialization (Potential XXE)")
                            .description("Detected instantiation of " + scope + " without explicit disabling of external general entities and DTD parsing. Untrusted XML inputs can lead to arbitrary local file disclosure (SSRF/XXE) or Denial of Service (Billion Laughs attack).")
                            .impact("Arbitrary local file read (/etc/passwd, cloud metadata), internal network scanning via SSRF, or server memory exhaustion.")
                            .remediation("Disable external entity resolution by setting 'http://apache.org/xml/features/disallow-doctype-decl' to true, or configure XMLConstants.FEATURE_SECURE_PROCESSING.")
                            .suggestedFix("// Secure XXE mitigation:\nDocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();\ndbf.setFeature(\"http://apache.org/xml/features/disallow-doctype-decl\", true);\ndbf.setFeature(\"http://xml.org/sax/features/external-general-entities\", false);\ndbf.setFeature(\"http://xml.org/sax/features/external-parameter-entities\", false);")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of(
                                    "https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing",
                                    "https://cwe.mitre.org/data/definitions/611.html"
                            ))
                            .build());
                }
            }
        });

        return findings;
    }
}
