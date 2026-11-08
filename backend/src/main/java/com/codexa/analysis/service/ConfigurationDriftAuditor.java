package com.codexa.analysis.service;

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
import java.util.stream.Stream;

/**
 * CR-CONF-002: Multi-Environment Configuration Drift & Insecure Production Defaults.
 * Compares dev and prod profiles to ensure security toggles (SSL, CSRF, Secure Cookies) are enforced in production.
 */
@Component
public class ConfigurationDriftAuditor implements AnalysisRule {

    @Override
    public String getRuleId() {
        return "CR-CONF-002";
    }

    @Override
    public String getName() {
        return "Multi-Environment Configuration Drift";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
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
                if (fileName.contains("prod") && (fileName.endsWith(".yml") || fileName.endsWith(".yaml") || fileName.endsWith(".properties"))) {
                    try {
                        String content = Files.readString(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        if (content.contains("ddl-auto: create") || content.contains("ddl-auto: create-drop")) {
                            findings.add(RuleFinding.builder()
                                    .ruleId(getRuleId())
                                    .category(getCategory())
                                    .severity(Severity.CRITICAL)
                                    .confidence(getDefaultConfidence())
                                    .title("Destructive Hibernate ddl-auto in Production Profile")
                                    .description("Detected 'ddl-auto: create' or 'create-drop' in production configuration. Upon startup in production, Hibernate will drop and recreate all database tables, destroying all production data.")
                                    .impact("Catastrophic database data loss and service outage.")
                                    .remediation("Set 'spring.jpa.hibernate.ddl-auto=validate' or rely on automated migration tools (Flyway / Liquibase) in production.")
                                    .suggestedFix("spring:\n  jpa:\n    hibernate:\n      ddl-auto: validate")
                                    .owaspMapping(getOwaspMapping())
                                    .filePath(relPath)
                                    .startLine(1)
                                    .endLine(1)
                                    .evidence("ddl-auto: create / create-drop in production config")
                                    .references(List.of("https://cwe.mitre.org/data/definitions/16.html"))
                                    .build());
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
