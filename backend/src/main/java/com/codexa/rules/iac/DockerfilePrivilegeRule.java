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
 * CR-IAC-009: Dangerous Dockerfile Privilege Escalation & Sudo Installation.
 * Flags SUID permission granting, installation of sudo utilities, or NOPASSWD configuration inside container builds.
 */
@Component
public class DockerfilePrivilegeRule implements AnalysisRule {

    private static final Pattern SUID_SET_PATTERN = Pattern.compile(
            "(?i)chmod\\s+(?:[ug]\\+s|[1-7]?[4-7][0-7]{2})"
    );

    private static final Pattern SUDO_INSTALL_PATTERN = Pattern.compile(
            "(?i)(?:apt-get|yum|dnf|apk|pacman)\\s+(?:install|add)\\s+.*\\bsudo\\b"
    );

    private static final Pattern SUDOERS_NOPASSWD = Pattern.compile(
            "(?i)NOPASSWD\\s*:\\s*ALL"
    );

    @Override
    public String getRuleId() {
        return "CR-IAC-009";
    }

    @Override
    public String getName() {
        return "Dockerfile SUID Escalation & Sudo Package Installation";
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
        return "Detects container images configuring SUID bits, installing sudo, or granting unauthenticated root access inside Dockerfiles, facilitating container breakout and privilege escalation.";
    }

    @Override
    public String getRemediation() {
        return "Never install the sudo binary in production container images. Strip SUID/SGID bits from system binaries and execute services as dedicated unprivileged system users.";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isDockerfile)
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private boolean isDockerfile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.equals("dockerfile") || filename.startsWith("dockerfile.") || filename.endsWith(".dockerfile");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (SUID_SET_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Granting SUID/SGID bits inside container build introduces privilege escalation vectors.")
                            .remediation(getRemediation())
                            .build());
                } else if (SUDO_INSTALL_PATTERN.matcher(line).find() || SUDOERS_NOPASSWD.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Installing sudo or setting NOPASSWD in container images violates principle of least privilege.")
                            .remediation("Remove sudo from image dependencies and run container as non-root user via USER instruction.")
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
