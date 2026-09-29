package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.SbomComponent;
import com.codexa.analysis.model.SbomReport;
import com.codexa.analysis.model.Severity;
import com.codexa.analysis.service.SbomDependencyService;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Open-Source License Compliance & Viral Copyleft Legal Risk Auditor.
 * Audits dependency licenses against AGPL, GPLv2, GPLv3, and SSPL for enterprise compliance.
 */
@Component
public class LicenseComplianceRule implements AnalysisRule {

    private static final Set<String> HIGH_RISK_COPYLEFT_LICENSES = Set.of(
            "GPL-2.0", "GPL-3.0", "AGPL-3.0", "GPL-2.0-ONLY", "GPL-3.0-ONLY", "AGPL-3.0-ONLY", "SSPL-1.0"
    );

    private final SbomDependencyService sbomService;

    public LicenseComplianceRule() {
        this(new SbomDependencyService());
    }

    @Autowired
    public LicenseComplianceRule(SbomDependencyService sbomService) {
        this.sbomService = sbomService != null ? sbomService : new SbomDependencyService();
    }

    @Override
    public String getRuleId() {
        return "CR-LIC-001";
    }

    @Override
    public String getName() {
        return "High-Risk Viral Copyleft License (AGPL / GPL)";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
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
    public boolean isRepositoryWide() {
        return true;
    }

    @Override
    public String getOwaspMapping() {
        return "A06:2021 - Vulnerable and Outdated Components";
    }

    @Override
    public String getDescription() {
        return "Detects open-source dependencies licensed under viral copyleft terms (GPL/AGPL) that legally mandate releasing proprietary source code upon distribution.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();
        if (stagingDir == null) return findings;

        try {
            SbomReport report = sbomService.generateSbom(stagingDir, null, "LicenseAudit");
            for (SbomComponent component : report.components()) {
                String license = component.license();
                if (license != null && isViralCopyleft(license)) {
                    findings.add(RuleFinding.builder()
                            .ruleId("CR-LIC-001")
                            .category(Category.OPERATIONS)
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .filePath(component.filePath())
                            .startLine(component.lineNumber() > 0 ? component.lineNumber() : 1)
                            .endLine(component.lineNumber() > 0 ? component.lineNumber() : 1)
                            .title("High-Risk Copyleft License Detected: " + component.name() + " (" + license + ")")
                            .description("Component '" + component.name() + "' is licensed under '" + license + "'. Incorporating strong copyleft code into enterprise software can trigger reciprocal open-sourcing obligations.")
                            .impact("Commercial legal risk: Strong copyleft terms require derivative works and linked software to be distributed under identical open-source licenses.")
                            .remediation("Replace '" + component.name() + "' with an alternative library licensed under permissive terms (MIT, Apache-2.0, BSD-3-Clause) or acquire a commercial dual-license.")
                            .suggestedFix("// Consider replacing " + component.name() + " with a permissive Apache-2.0 / MIT alternative")
                            .evidence("Package: " + component.purl() + " | License: " + license)
                            .owaspMapping("A06:2021 - Vulnerable and Outdated Components")
                            .references(List.of("https://opensource.org/licenses/GPL-3.0", "https://choosealicense.com/licenses/agpl-3.0/"))
                            .build());
                }
            }
        } catch (Exception ignored) {}

        return findings;
    }

    private boolean isViralCopyleft(String license) {
        String upper = license.toUpperCase().trim();
        for (String copyleft : HIGH_RISK_COPYLEFT_LICENSES) {
            if (upper.contains(copyleft)) return true;
        }
        return false;
    }
}
