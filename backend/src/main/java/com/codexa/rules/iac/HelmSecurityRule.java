package com.codexa.rules.iac;

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
 * CR-HELM-001: Plaintext Credentials in Helm values.yaml.
 * Detects hardcoded database passwords and secrets checked into Helm charts.
 */
@Component
public class HelmSecurityRule implements AnalysisRule {

    private static final Pattern HELM_SECRET_PATTERN = Pattern.compile(
            "(?i)(?:password|secretKey|dbPassword|rootPassword|adminPassword|token)\\s*:\\s*[\"']([^\"'\\s]{6,})[\"']"
    );

    @Override
    public String getRuleId() {
        return "CR-HELM-001";
    }

    @Override
    public String getName() {
        return "Hardcoded Secrets in Helm Chart Values";
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
    public boolean isRepositoryWide() {
        return true;
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();

        if (stagingDir == null || !Files.exists(stagingDir)) {
            return findings;
        }

        try (Stream<Path> stream = Files.walk(stagingDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.contains("values") && (fileName.endsWith(".yaml") || fileName.endsWith(".yml"))) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (HELM_SECRET_PATTERN.matcher(line).find() && !line.contains("{{") && !line.contains("example")) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("Plaintext Secret in Helm values.yaml")
                                        .description("Detected plaintext password or sensitive token hardcoded in Helm values file. Storing secrets in cleartext in Helm repositories leads to credential theft and unauthorized cluster access.")
                                        .impact("Compromise of backend database credentials or API access tokens.")
                                        .remediation("Reference external secrets using Kubernetes Secrets, Helm Secrets (SOPS), or HashiCorp Vault integrations.")
                                        .suggestedFix("# Reference existing Kubernetes secret instead of hardcoding values:\nexistingSecret: \"app-secrets\"\nsecretKeyRef: \"db-password\"")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/312.html"))
                                        .build());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (IOException ignored) {
        }

        return findings;
    }
}
