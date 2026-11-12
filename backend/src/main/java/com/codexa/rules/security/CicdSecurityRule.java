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
 * CR-CICD-001: CI/CD Pipeline Script Injection & Untrusted PR Head Checkout.
 * Detects unescaped GitHub Actions expression interpolation inside run steps
 * (${{ github.event... }}) and insecure pull_request_target checkout combinations.
 */
@Component
public class CicdSecurityRule implements AnalysisRule {

    private static final Pattern UNTRUSTED_EXPRESSION_PATTERN = Pattern.compile(
            "\\$\\{\\{\\s*github\\.event\\.(?:issue\\.title|issue\\.body|pull_request\\.title|pull_request\\.body|pull_request\\.head\\.ref|comment\\.body)\\s*\\}\\}"
    );

    private static final Pattern INSECURE_PR_TARGET_CHECKOUT = Pattern.compile(
            "(?i)ref\\s*:\\s*\\$\\{\\{\\s*github\\.event\\.pull_request\\.head\\.(?:sha|ref)\\s*\\}\\}"
    );

    @Override
    public String getRuleId() {
        return "CR-CICD-001";
    }

    @Override
    public String getName() {
        return "CI/CD Workflow Script Injection & Untrusted Checkout";
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
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getDescription() {
        return "Detects untrusted user-controlled inputs (issue titles, PR branches, comment bodies) interpolated directly into shell script steps in GitHub Actions workflows, allowing arbitrary shell command injection and secret theft.";
    }

    @Override
    public String getRemediation() {
        return "Never inline untrusted expression values into run: scripts. Assign them to intermediate environment variables (env:) first and reference them as shell variables ($VAR).";
    }

    @Override
    public List<RuleFinding> analyze(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        if (context == null || context.getProjectDirectory() == null) {
            return findings;
        }

        try (Stream<Path> paths = Files.walk(context.getProjectDirectory())) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isWorkflowFile)
                 .forEach(path -> scanFile(path, context, findings));
        } catch (IOException e) {
            // Ignore unreadable paths
        }

        return findings;
    }

    private boolean isWorkflowFile(Path path) {
        String p = path.toString().replace('\\', '/').toLowerCase();
        return p.contains(".github/workflows/") || p.endsWith(".gitlab-ci.yml") || p.endsWith("azure-pipelines.yml");
    }

    private void scanFile(Path path, RuleContext context, List<RuleFinding> findings) {
        try {
            String content = Files.readString(path);
            String relPath = context.getProjectDirectory().relativize(path).toString().replace('\\', '/');

            String[] lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (UNTRUSTED_EXPRESSION_PATTERN.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getConfidence())
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Direct interpolation of untrusted GitHub event property into workflow script enables arbitrary command injection.")
                            .remediation(getRemediation())
                            .build());
                } else if (INSECURE_PR_TARGET_CHECKOUT.matcher(line).find()) {
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .ruleName(getName())
                            .category(getCategory())
                            .severity(Severity.HIGH)
                            .confidence(Confidence.HIGH)
                            .filePath(relPath)
                            .lineNumber(i + 1)
                            .snippet(line.trim())
                            .message("Checking out untrusted pull request code with elevated repository token permissions in pull_request_target.")
                            .remediation("Use pull_request trigger or isolate untrusted code execution without sensitive secrets access.")
                            .build());
                }
            }
        } catch (IOException e) {
            // Ignore unreadable file
        }
    }
}
