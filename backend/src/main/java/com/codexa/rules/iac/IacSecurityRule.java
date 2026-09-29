package com.codexa.rules.iac;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Infrastructure-as-Code (IaC) & Cloud Security Scanner.
 * Audits Dockerfiles, Kubernetes manifests, Docker Compose, and Terraform definitions.
 */
@Component
public class IacSecurityRule implements AnalysisRule {

    private static final Logger log = LoggerFactory.getLogger(IacSecurityRule.class);

    private static final Pattern UNTAGGED_FROM_PATTERN = Pattern.compile("(?i)^\\s*FROM\\s+([a-zA-Z0-9_./-]+)(?::latest)?\\s*(?:AS\\s+\\w+)?$");
    private static final Pattern K8S_PRIVILEGED_PATTERN = Pattern.compile("(?i)(privileged|allowPrivilegeEscalation)\\s*:\\s*true");
    private static final Pattern TF_OPEN_CIDR_PATTERN = Pattern.compile("(?i)cidr_blocks\\s*=\\s*\\[\\s*[\"']0\\.0\\.0\\.0/0[\"']\\s*\\]");
    private static final Pattern TF_UNENCRYPTED_PATTERN = Pattern.compile("(?i)encrypted\\s*=\\s*false");

    @Override
    public String getRuleId() {
        return "CR-IAC-001";
    }

    @Override
    public String getName() {
        return "Infrastructure-as-Code (IaC) & Cloud Security Misconfiguration";
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
    public boolean isRepositoryWide() {
        return true;
    }

    @Override
    public String getOwaspMapping() {
        return "A05:2021 - Security Misconfiguration";
    }

    @Override
    public String getDescription() {
        return "Audits Docker, Kubernetes, and Terraform infrastructure configurations for privilege escalation, root execution, and unencrypted cloud resources.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();
        if (stagingDir == null || !Files.exists(stagingDir)) return findings;

        try (Stream<Path> stream = Files.walk(stagingDir, 10)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                // Skip target, dist, node_modules
                if (relPath.contains("target/") || relPath.contains("dist/") || relPath.contains("node_modules/")) {
                    return;
                }

                if (fileName.contains("dockerfile") || fileName.endsWith(".dockerfile")) {
                    auditDockerfile(path, relPath, findings);
                } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    auditKubernetesOrCompose(path, relPath, findings);
                } else if (fileName.endsWith(".tf") || fileName.endsWith(".tfvars")) {
                    auditTerraform(path, relPath, findings);
                }
            });
        } catch (IOException e) {
            log.debug("Error walking staging dir for IaC scan: {}", e.getMessage());
        }

        return findings;
    }

    private void auditDockerfile(Path path, String relPath, List<RuleFinding> findings) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            boolean hasUserDirective = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                int lineNum = i + 1;

                if (line.toUpperCase().startsWith("USER ")) {
                    hasUserDirective = true;
                    if (line.equalsIgnoreCase("USER root") || line.equalsIgnoreCase("USER 0")) {
                        findings.add(createFinding(
                                "CR-IAC-001",
                                "Container Configured to Run as Root User",
                                Severity.HIGH,
                                relPath,
                                lineNum,
                                line,
                                "Explicitly running container workload as root breaks process containment boundaries and facilitates container escape.",
                                "Declare and switch to an unprivileged system user (e.g. `RUN adduser -D appuser && USER appuser`)."
                        ));
                    }
                }

                if (UNTAGGED_FROM_PATTERN.matcher(line).matches() && !line.contains("@sha256:") && (line.endsWith(":latest") || !line.contains(":"))) {
                    findings.add(createFinding(
                            "CR-IAC-002",
                            "Unpinned or Latest Docker Base Image",
                            Severity.MEDIUM,
                            relPath,
                            lineNum,
                            line,
                            "Using ':latest' or unpinned container base image creates non-deterministic builds and exposes containers to upstream supply chain poisoning.",
                            "Pin base image with explicit semver and SHA-256 digest (e.g. `FROM alpine:3.19@sha256:...`)."
                    ));
                }
            }

            if (!hasUserDirective && !lines.isEmpty()) {
                findings.add(createFinding(
                        "CR-IAC-001",
                        "Missing Non-Root USER Directive in Dockerfile",
                        Severity.HIGH,
                        relPath,
                        1,
                        "Dockerfile",
                        "No USER directive specified; container will execute by default under root UID 0.",
                        "Add `USER <non-root-user>` directive before the container ENTRYPOINT or CMD."
                ));
            }
        } catch (Exception ignored) {}
    }

    private void auditKubernetesOrCompose(Path path, String relPath, List<RuleFinding> findings) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                int lineNum = i + 1;

                if (K8S_PRIVILEGED_PATTERN.matcher(line).find()) {
                    findings.add(createFinding(
                            "CR-IAC-003",
                            "Kubernetes Privileged Container Escalation Enabled",
                            Severity.CRITICAL,
                            relPath,
                            lineNum,
                            line,
                            "Privileged mode grants container full root capabilities and device access to host kernel, allowing trivial host takeover.",
                            "Set `securityContext.privileged: false` and `securityContext.allowPrivilegeEscalation: false`."
                    ));
                }
            }
        } catch (Exception ignored) {}
    }

    private void auditTerraform(Path path, String relPath, List<RuleFinding> findings) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                int lineNum = i + 1;

                if (TF_OPEN_CIDR_PATTERN.matcher(line).find()) {
                    findings.add(createFinding(
                            "CR-IAC-005",
                            "Terraform Unrestricted 0.0.0.0/0 Security Group Ingress",
                            Severity.HIGH,
                            relPath,
                            lineNum,
                            line,
                            "Security group allows inbound traffic from entire internet (0.0.0.0/0), exposing cloud infrastructure to automated port scans.",
                            "Restrict `cidr_blocks` to explicit corporate VPN IP ranges or internal VPC subnets."
                    ));
                }

                if (TF_UNENCRYPTED_PATTERN.matcher(line).find()) {
                    findings.add(createFinding(
                            "CR-IAC-006",
                            "Unencrypted Cloud Storage Volume in Terraform",
                            Severity.HIGH,
                            relPath,
                            lineNum,
                            line,
                            "Cloud storage volume or S3 bucket is explicitly configured without encryption at rest.",
                            "Enable default server-side encryption (KMS or AES-256) via `encrypted = true`."
                    ));
                }
            }
        } catch (Exception ignored) {}
    }

    private RuleFinding createFinding(
            String ruleId,
            String title,
            Severity severity,
            String filePath,
            int line,
            String snippet,
            String description,
            String remediation
    ) {
        return RuleFinding.builder()
                .ruleId(ruleId)
                .category(Category.SECURITY)
                .severity(severity)
                .confidence(Confidence.HIGH)
                .filePath(filePath)
                .startLine(line)
                .endLine(line)
                .title(title)
                .description(description)
                .impact("Cloud infrastructure security posture breach violating CIS Benchmark and ISO 27001 A.8.20.")
                .remediation(remediation)
                .suggestedFix(remediation)
                .evidence(snippet)
                .owaspMapping("A05:2021 - Security Misconfiguration")
                .references(List.of("https://cwe.mitre.org/data/definitions/250.html", "https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html"))
                .build();
    }
}
