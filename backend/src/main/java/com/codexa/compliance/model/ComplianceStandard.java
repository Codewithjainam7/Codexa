package com.codexa.compliance.model;

/**
 * Supported enterprise regulatory and security compliance frameworks.
 */
public enum ComplianceStandard {
    SOC2_TYPE2("SOC 2 Type II", "AICPA Trust Services Criteria (Security, Confidentiality, Availability)"),
    ISO_27001("ISO/IEC 27001:2022", "Information Security Management System - Annex A Controls"),
    PCI_DSS_V4("PCI-DSS v4.0", "Payment Card Industry Data Security Standard"),
    HIPAA_SECURITY("HIPAA Security Rule", "45 CFR Part 160 & Part 164 Subparts A and C (Technical Safeguards)"),
    OWASP_TOP_10("OWASP Top 10:2021", "Standard Security Risk Benchmark for Web Applications");

    private final String displayName;
    private final String description;

    ComplianceStandard(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ComplianceStandard fromString(String val) {
        if (val == null || val.isBlank()) return SOC2_TYPE2;
        String normalized = val.toUpperCase().replace("-", "_").trim();
        for (ComplianceStandard standard : values()) {
            if (standard.name().equalsIgnoreCase(normalized) || standard.name().startsWith(normalized)) {
                return standard;
            }
        }
        return SOC2_TYPE2;
    }
}
