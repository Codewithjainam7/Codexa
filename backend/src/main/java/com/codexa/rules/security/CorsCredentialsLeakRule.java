package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * CR-CORS-002: Insecure CORS Wildcard with Credentials / Origin Reflection.
 * Detects Access-Control-Allow-Origin: * combined with Access-Control-Allow-Credentials: true,
 * or arbitrary request origin reflection allowing cross-origin credential exfiltration.
 */
@Component
public class CorsCredentialsLeakRule implements AnalysisRule {

    private static final Pattern CORS_WILDCARD_CREDENTIALS = Pattern.compile(
            "(?i)(?:res\\.header\\(|response\\.setHeader\\(|addHeader\\()\\s*[\"']Access-Control-Allow-Origin[\"']\\s*,\\s*[\"']\\*[\"'].*?(?:res\\.header\\(|response\\.setHeader\\(|addHeader\\()\\s*[\"']Access-Control-Allow-Credentials[\"']\\s*,\\s*[\"']true[\"']",
            Pattern.DOTALL
    );

    private static final Pattern REFLECT_ORIGIN_CREDENTIALS = Pattern.compile(
            "(?i)Access-Control-Allow-Origin[\"']\\s*,\\s*(?:req\\.headers\\[?['\"]origin['\"]?\\]?|request\\.getHeader\\([\"']Origin[\"']\\))"
    );

    private static final Pattern WEAK_ORIGIN_REGEX = Pattern.compile(
            "(?i)(?:origin\\.includes|origin\\.indexOf|origin\\.endsWith|origin\\.contains)\\s*\\([\"'][a-zA-Z0-9.-]+\\.[a-z]{2,}[\"']\\)"
    );

    @Override
    public String getRuleId() {
        return "CR-CORS-002";
    }

    @Override
    public String getName() {
        return "Insecure CORS Policy & Credential Leakage";
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
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getDescription() {
        return "Detects CORS configurations that reflect arbitrary client Origin headers or combine wildcard origin with Allow-Credentials=true, permitting malicious third-party origins to steal authenticated session tokens and confidential data via cross-origin fetch requests.";
    }

    @Override
    public String getRemediation() {
        return "Enforce strict allowlists of trusted domains using anchored exact-match regular expressions or explicit hostname lookups. Never echo back arbitrary Origin headers when Allow-Credentials is true.";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isApplicableSourceFile)
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private boolean isApplicableSourceFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".js") || filename.endsWith(".ts")
                || filename.endsWith(".java") || filename.endsWith(".py")
                || filename.endsWith(".go") || filename.endsWith(".cs");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            if (CORS_WILDCARD_CREDENTIALS.matcher(content).find()) {
                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .ruleName(getName())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getConfidence())
                        .filePath(relPath)
                        .lineNumber(1)
                        .snippet("Access-Control-Allow-Origin: * with Access-Control-Allow-Credentials: true")
                        .message("Wildcard CORS origin is combined with Access-Control-Allow-Credentials: true, exposing authenticated responses to any origin.")
                        .remediation(getRemediation())
                        .build());
            }

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (REFLECT_ORIGIN_CREDENTIALS.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("CORS Access-Control-Allow-Origin dynamically reflects the unvalidated incoming request Origin header.")
                            .remediation(getRemediation())
                            .build());
                } else if (WEAK_ORIGIN_REGEX.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Weak CORS Origin validation check using substring or unanchored suffix matching permits attacker domains like evil-target.com.")
                            .remediation("Validate origin hostname strictly against an allowlist set or anchor domain regex with ^ and $.")
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
