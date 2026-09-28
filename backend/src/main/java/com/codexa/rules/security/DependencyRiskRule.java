package com.codexa.rules.security;

import com.codexa.analysis.model.*;
import com.codexa.analysis.service.SbomDependencyService;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Static Analysis Rule: CR-DEP-001 (Software Supply Chain Security & Vulnerable Dependencies).
 * Audits repository manifests (Maven pom.xml, Gradle, npm package.json, PyPI requirements.txt, go.mod)
 * against known supply chain CVE advisories.
 */
@Component
public class DependencyRiskRule implements AnalysisRule {

    private static final Logger log = LoggerFactory.getLogger(DependencyRiskRule.class);

    private final SbomDependencyService sbomService;

    public DependencyRiskRule() {
        this(new SbomDependencyService());
    }

    @Autowired
    public DependencyRiskRule(SbomDependencyService sbomService) {
        this.sbomService = sbomService != null ? sbomService : new SbomDependencyService();
    }

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
        return Severity.HIGH;
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
    public String getDescription() {
        return "Third-party supply chain libraries containing publicly disclosed CVE vulnerabilities or unpatched remote code execution flaws.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();
        if (stagingDir == null) return findings;

        try {
            SbomReport report = sbomService.generateSbom(stagingDir, null, "RepositoryAudit");

            for (SbomComponent component : report.components()) {
                for (CveAdvisory adv : sbomService.getAdvisories()) {
                    if (component.name().equalsIgnoreCase(adv.componentName())) {
                        if (sbomService.isVersionVulnerable(component.version(), adv.vulnerableVersionMatcher())) {
                            String relPath = component.filePath();

                            findings.add(RuleFinding.builder()
                                    .ruleId(getRuleId())
                                    .category(getCategory())
                                    .severity(adv.severity())
                                    .confidence(getDefaultConfidence())
                                    .filePath(relPath)
                                    .startLine(component.lineNumber() > 0 ? component.lineNumber() : 1)
                                    .endLine(component.lineNumber() > 0 ? component.lineNumber() : 1)
                                    .title("Vulnerable Dependency: " + component.name() + ":" + component.version())
                                    .description("Dependency coordinate " + component.name() + ":" + component.version() + " matches " + adv.cveId() + ": " + adv.summary())
                                    .impact("Known security vulnerability in supply chain dependency: " + adv.cveId())
                                    .remediation("Upgrade " + component.name() + " to version " + adv.fixedVersion() + " or higher to patch " + adv.cveId() + ".")
                                    .suggestedFix("Upgrade " + component.name() + " to version " + adv.fixedVersion())
                                    .evidence("PURL: " + component.purl() + " | CVE: " + adv.cveId() + " (CVSS " + adv.cvssScore() + ")")
                                    .owaspMapping(getOwaspMapping())
                                    .references(List.of(adv.referenceUrl()))
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Dependency risk evaluation encountered error: {}", ex.getMessage());
        }

        return findings;
    }
}
