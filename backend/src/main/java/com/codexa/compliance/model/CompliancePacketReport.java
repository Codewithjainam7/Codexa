package com.codexa.compliance.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Complete executive-grade compliance packet encapsulating regulatory control scores.
 */
public record CompliancePacketReport(
        UUID jobId,
        ComplianceStandard standard,
        String targetRepository,
        Instant evaluatedAt,
        OverallComplianceStatus overallStatus,
        double complianceScore,
        int totalControls,
        int compliantControls,
        int nonCompliantControls,
        int totalViolations,
        int criticalViolations,
        List<ComplianceControlResult> controls,
        String executiveSummary
) {
    public enum OverallComplianceStatus {
        AUDIT_READY,
        CONDITIONAL_PASS,
        AUDIT_BLOCKED
    }
}
