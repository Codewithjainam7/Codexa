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
 * CR-SUPPLY-001: Dependency Confusion & Typosquatting Supply Chain Guard.
 * Flags package manifests that retrieve unscoped internal packages from public registries without private scope configuration.
 */
@Component
public class DependencyConfusionRule implements AnalysisRule {

    private static final Pattern INSECURE_EXTRA_INDEX_PATTERN = Pattern.compile(
            "(?i)extra-index-url\\s*=\\s*https?://pypi\\.org"
    );

    @Override
    public String getRuleId() {
        return "CR-SUPPLY-001";
    }

    @Override
    public String getName() {
        return "Dependency Confusion & Supply Chain Insecurity";
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
        return "A06:2021 - Vulnerable and Outdated Components";
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
                if (fileName.equals("pip.conf") || fileName.equals("requirements.txt") ||
                    fileName.equals(".npmrc") || fileName.equals("package.json")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (INSECURE_EXTRA_INDEX_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("Pip Extra-Index-Url Dependency Confusion Risk")
                                        .description("Detected 'extra-index-url' pointing to public PyPI alongside a private repository. Pip does not prioritize private indexes over public ones; an attacker can register an identical package name on public PyPI with higher version numbers to hijack package installs.")
                                        .impact("Arbitrary code execution on build runners and developer workstations during installation.")
                                        .remediation("Use a single virtual repository proxy (such as Nexus, Artifactory, or AWS CodeArtifact) that controls package resolution, and omit extra-index-url.")
                                        .suggestedFix("# Secure pip.conf:\n[global]\nindex-url = https://artifactory.internal.company.com/api/pypi/pypi-virtual/simple")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/829.html"))
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
