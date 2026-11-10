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
 * CR-K8S-002: Kubernetes Secrets Exposed via Environment Variables.
 * Detects secrets mapped into containers as environment variables via secretKeyRef or envFrom,
 * which leak into /proc, child processes, log aggregators, and container inspection.
 */
@Component
public class KubernetesSecretsEnvRule implements AnalysisRule {

    private static final Pattern SECRET_ENV_PATTERN = Pattern.compile(
            "(?i)valueFrom\\s*:\\s*\\r?\\n?\\s*secretKeyRef\\s*:"
    );

    private static final Pattern ENV_FROM_SECRET_PATTERN = Pattern.compile(
            "(?i)envFrom\\s*:\\s*\\r?\\n?\\s*-\\s*secretRef\\s*:"
    );

    @Override
    public String getRuleId() {
        return "CR-K8S-002";
    }

    @Override
    public String getName() {
        return "Kubernetes Secret Ingestion via Environment Variables";
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
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getDescription() {
        return "Injecting Kubernetes secrets into container environment variables exposes credentials to all child processes, system crash dumps, /proc/$PID/environ inspection, and diagnostic log streams.";
    }

    @Override
    public String getRemediation() {
        return "Mount secrets into containers as read-only volume files (secret volume with defaultMode: 0400) or utilize the Secrets Store CSI Driver instead of environment variables.";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isKubernetesManifest)
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private boolean isKubernetesManifest(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".yaml") || filename.endsWith(".yml");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            // Quick check if file looks like a k8s manifest
            if (!content.contains("apiVersion") && !content.contains("kind:")) {
                return;
            }

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains("secretKeyRef:") || line.contains("secretRef:")) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Secret referenced in container environment configuration. Secrets in env vars are visible across all subprocesses and crash dumps.")
                            .remediation(getRemediation())
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
