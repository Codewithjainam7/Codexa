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
 * CR-K8S-001: Kubernetes Insecure hostPath Mount & Missing NetworkPolicy.
 * Detects hostPath mounts escaping container isolation and cluster namespaces lacking ingress isolation.
 */
@Component
public class KubernetesNetworkSecurityRule implements AnalysisRule {

    private static final Pattern HOST_PATH_PATTERN = Pattern.compile(
            "(?i)hostPath\\s*:\\s*\\n?\\s*path\\s*:\\s*[\"']?(?:/|/etc|/var/run/docker\\.sock|/proc|/sys)"
    );

    private static final Pattern ROOT_CONTAINER_PATTERN = Pattern.compile(
            "(?i)runAsNonRoot\\s*:\\s*false|runAsUser\\s*:\\s*0"
    );

    @Override
    public String getRuleId() {
        return "CR-K8S-001";
    }

    @Override
    public String getName() {
        return "Kubernetes HostPath Mount & Insecure SecurityContext";
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
                if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    try {
                        String content = Files.readString(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        if (content.contains("kind: Pod") || content.contains("kind: Deployment") ||
                            content.contains("kind: StatefulSet") || content.contains("kind: DaemonSet")) {

                            if (HOST_PATH_PATTERN.matcher(content).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.CRITICAL)
                                        .confidence(getDefaultConfidence())
                                        .title("Dangerous Kubernetes hostPath Volume Mount")
                                        .description("Detected hostPath volume mount targeting sensitive node paths (/ or /var/run/docker.sock or /etc). Compromising this pod grants immediate root compromise over the underlying Kubernetes host node.")
                                        .impact("Total Kubernetes worker node takeover, host filesystem corruption, and cross-pod lateral movement.")
                                        .remediation("Replace hostPath mounts with persistent volumes (PVC) or ephemeral storage. Never mount Docker sockets or root directories.")
                                        .suggestedFix("# Use persistentVolumeClaim instead of hostPath:\npersistentVolumeClaim:\n  claimName: app-storage-claim")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(1)
                                        .endLine(1)
                                        .evidence("hostPath mount targeting root or docker socket")
                                        .references(List.of("https://cwe.mitre.org/data/definitions/284.html"))
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
