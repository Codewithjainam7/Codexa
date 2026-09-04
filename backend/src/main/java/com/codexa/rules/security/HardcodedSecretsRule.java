package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HardcodedSecretsRule implements AnalysisRule {

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("(?<![A-Z0-9])[A-Z0-9]{20}(?![A-Z0-9])");
    private static final Pattern AWS_SECRET_PATTERN = Pattern.compile("(?<![A-Za-z0-9/+=])[A-Za-z0-9/+=]{40}(?![A-Za-z0-9/+=])");
    private static final Pattern GENERIC_SECRET_PATTERN = Pattern.compile(
            "(?i)(?:api[_-]?key|secret[_-]?key|jwt[_-]?secret|access[_-]?token|db[_-]?password|password)\\s*=\\s*[\"']([^\"'\\s]{8,})[\"']"
    );
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile("-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----");

    @Override
    public String getRuleId() {
        return "CR-SEC-001";
    }

    @Override
    public String getName() {
        return "Hardcoded Credentials or API Secrets";
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
        return "A02:2025 - Security Misconfiguration";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getLines() == null) {
            return findings;
        }

        List<String> lines = parsedFile.getLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;

            // Check for private key header
            if (PRIVATE_KEY_PATTERN.matcher(line).find()) {
                findings.add(buildSecretFinding(parsedFile.getRelativePath(), lineNumber, "Private Key Header in source code",
                        "-----BEGIN PRIVATE KEY----- [REDACTED]"));
                continue;
            }

            // Check for generic variable assignments (apiKey = "...", password = "...")
            Matcher genericMatcher = GENERIC_SECRET_PATTERN.matcher(line);
            if (genericMatcher.find()) {
                String secretValue = genericMatcher.group(1);
                // Ignore obvious placeholders
                if (!isPlaceholder(secretValue)) {
                    String maskedLine = maskSecretInLine(line, secretValue);
                    findings.add(buildSecretFinding(parsedFile.getRelativePath(), lineNumber, "Hardcoded credential/secret variable", maskedLine));
                }
            }
        }

        return findings;
    }

    private boolean isPlaceholder(String val) {
        String lower = val.toLowerCase();
        return lower.contains("placeholder") || lower.contains("example") || lower.contains("your_") ||
                lower.contains("change_me") || lower.contains("test") || lower.contains("dummy") ||
                lower.equals("password") || lower.equals("12345678");
    }

    private String maskSecretInLine(String line, String secret) {
        if (secret.length() <= 4) {
            return line.replace(secret, "****");
        }
        String visible = secret.substring(0, 2);
        String masked = visible + "*".repeat(secret.length() - 2);
        return line.replace(secret, masked);
    }

    private RuleFinding buildSecretFinding(String filePath, int line, String title, String maskedEvidence) {
        return RuleFinding.builder()
                .ruleId(getRuleId())
                .category(getCategory())
                .severity(getSeverity())
                .confidence(getDefaultConfidence())
                .title(title)
                .description("Detected hardcoded cryptographic credential, API token, or private key in source code. Storing sensitive credentials in version control exposes backend systems to credential theft and unauthorized access.")
                .impact("Unauthorized access to cloud infrastructure, external APIs, and database resources.")
                .remediation("Externalize all secrets into environment variables (e.g. System.getenv(\"API_KEY\")) or dedicated secret vaults (e.g. AWS Secrets Manager, HashiCorp Vault). Never commit secrets to source control.")
                .suggestedFix("// Secure remediation: externalize secret into environment variables\n@Value(\"${API_KEY}\")\nprivate String apiKey;")
                .owaspMapping(getOwaspMapping())
                .filePath(filePath)
                .startLine(line)
                .endLine(line)
                .evidence(maskedEvidence)
                .references(List.of("https://owasp.org/Top10/2025/A02_2025-Security_Misconfiguration/", "https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html"))
                .build();
    }
}
