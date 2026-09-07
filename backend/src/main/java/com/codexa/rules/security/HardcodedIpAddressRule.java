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
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class HardcodedIpAddressRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    // Matches RFC 1918 private IPv4 addresses (10.x.x.x, 192.168.x.x, 172.16-31.x.x) and loopback
    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
            "^(?:10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|172\\.(?:1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}|127\\.0\\.0\\.1)$"
    );

    @Override
    public String getRuleId() {
        return "CR-SEC-008";
    }

    @Override
    public String getName() {
        return "Hardcoded Internal IP Address";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
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
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (StringLiteralExpr sle : cu.findAll(StringLiteralExpr.class)) {
            String val = sle.getValue().trim();
            if (PRIVATE_IP_PATTERN.matcher(val).matches()) {
                int line = sle.getBegin().map(p -> p.line).orElse(1);
                int endLine = sle.getEnd().map(p -> p.line).orElse(line);
                String evidence = snippetExtractor.extractNodeSnippet(sle, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .title("Hardcoded Private IP Address: " + val)
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .owaspMapping(getOwaspMapping())
                        .filePath(filename)
                        .startLine(line)
                        .endLine(endLine)
                        .evidence(evidence)
                        .description("Hardcoded private IP address exposes internal network topology and inhibits dynamic orchestration.")
                        .impact("Exposure of internal infrastructure addressing and fragile environment configuration.")
                        .remediation("Source hostnames and IP addresses from externalized environment variables or configuration properties.")
                        .suggestedFix("// Recommended: externalize host IP into application.yml or environment variable\n@Value(\"${service.host.ip}\")\nprivate String serviceHost;")
                        .references(List.of("https://owasp.org/Top10/2021/A05_2021-Security_Misconfiguration/"))
                        .build());
            }
        }

        return findings;
    }
}
