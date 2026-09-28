package com.codexa.compliance.model;

import java.util.List;

/**
 * Result of evaluating a specific compliance control (e.g., SOC 2 CC6.1, ISO 27001 A.14.2).
 */
public record ComplianceControlResult(
        String controlId,
        String controlName,
        ComplianceStatus status,
        int findingCount,
        int criticalCount,
        int highCount,
        String description,
        List<String> violatingRules
) {
    public enum ComplianceStatus {
        COMPLIANT,
        NON_COMPLIANT,
        REQUIRES_AUDIT
    }
}
