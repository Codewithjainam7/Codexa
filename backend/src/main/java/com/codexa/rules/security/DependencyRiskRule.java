package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DependencyRiskRule implements AnalysisRule {

    private static final Logger log = LoggerFactory.getLogger(DependencyRiskRule.class);

    private static final Map<String, String> VULNERABLE_COORDINATES = Map.of(
            "log4j-core:2.14.1", "Log4Shell CVE-2021-44228 (Remote Code Execution)",
            "log4j-core:2.15.0", "Log4j CVE-2021-45046 (Denial of Service / RCE)",
            "spring-beans:5.3.17", "Spring4Shell CVE-2022-22965 (Remote Code Execution)",
            "fastjson:1.2.80", "Fastjson Deserialization Vulnerability (RCE)",
            "commons-collections:3.2.1", "Apache Commons Collections Deserialization RCE"
    );

    @Override
    public String getRuleId() {
        return "CR-DEP-001";
    }

    @Override
    public String getName() {
        return "Vulnerable or Outdated Dependency";
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
        return "A03:2025 - Software Supply Chain Security";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        List<Path> allFiles = context.getAllSourceFiles();
        if (allFiles == null) return findings;

        for (Path file : allFiles) {
            String fileName = file.getFileName().toString().toLowerCase();
            if (fileName.equals("pom.xml") || fileName.endsWith(".gradle")) {
                try {
                    String content = Files.readString(file);
                    for (Map.Entry<String, String> entry : VULNERABLE_COORDINATES.entrySet()) {
                        String[] parts = entry.getKey().split(":");
                        String artifact = parts[0];
                        String version = parts[1];

                        if (content.contains(artifact) && content.contains(version)) {
                            String relPath = context.getStagingDirectory().relativize(file).toString().replace('\\', '/');
                            findings.add(RuleFinding.builder()
                                    .ruleId(getRuleId())
                                    .category(getCategory())
                                    .severity(getSeverity())
                                    .confidence(getDefaultConfidence())
                                    .title("Known vulnerable dependency version: " + artifact + ":" + version)
                                    .description("Detected dependency coordinate '" + artifact + ":" + version + "' matching known security advisory: " + entry.getValue())
                                    .impact("Exploitation of known CVE vulnerabilities in third-party supply chain components.")
                                    .remediation("Upgrade the dependency to the latest patched stable version in your build configuration.")
                                    .suggestedFix("Upgrade " + artifact + " to latest secure release.")
                                    .owaspMapping(getOwaspMapping())
                                    .filePath(relPath)
                                    .startLine(1)
                                    .endLine(1)
                                    .evidence(artifact + ":" + version + " -> " + entry.getValue())
                                    .references(List.of("https://owasp.org/Top10/2025/A03_2025-Software_Supply_Chain_Security/"))
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error reading dependency file {}: {}", file, e.getMessage());
                }
            }
        }

        return findings;
    }
}
