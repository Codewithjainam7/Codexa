package com.codexa.compliance.service;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.compliance.model.ComplianceControlResult;
import com.codexa.compliance.model.CompliancePacketReport;
import com.codexa.compliance.model.CompliancePacketReport.OverallComplianceStatus;
import com.codexa.compliance.model.ComplianceStandard;
import com.codexa.persistence.entity.FindingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ComplianceAuditServiceTest {

    private final ComplianceAuditService complianceAuditService = new ComplianceAuditService();

    @Test
    @DisplayName("Clean repository should pass SOC 2 audit with 100% compliance score")
    void testCleanRepositoryAuditReady() {
        UUID jobId = UUID.randomUUID();
        CompliancePacketReport report = complianceAuditService.evaluateCompliance(
                jobId, "acme/payment-gateway", ComplianceStandard.SOC2_TYPE2, List.of()
        );

        assertNotNull(report);
        assertEquals(OverallComplianceStatus.AUDIT_READY, report.overallStatus());
        assertEquals(100.0, report.complianceScore());
        assertEquals(report.totalControls(), report.compliantControls());
        assertEquals(0, report.nonCompliantControls());
        assertEquals(0, report.totalViolations());
        assertTrue(report.executiveSummary().contains("Zero critical compliance blockers"));
    }

    @Test
    @DisplayName("Critical SQL injection should block SOC 2 and PCI-DSS compliance")
    void testCriticalSqlInjectionBlocksCompliance() {
        UUID jobId = UUID.randomUUID();
        FindingEntity finding = new FindingEntity();
        finding.setRuleId("CR-SQL-001");
        finding.setDescription("CWE-89 SQL Injection via dynamic string concatenation");
        finding.setOwaspMapping("A03:2021");
        finding.setSeverity(Severity.CRITICAL);
        finding.setCategory(Category.SECURITY);
        finding.setConfidence(Confidence.HIGH);
        finding.setFilePath("src/main/java/UserRepo.java");

        CompliancePacketReport report = complianceAuditService.evaluateCompliance(
                jobId, "acme/vulnerable-app", ComplianceStandard.SOC2_TYPE2, List.of(finding)
        );

        assertNotNull(report);
        assertEquals(OverallComplianceStatus.AUDIT_BLOCKED, report.overallStatus());
        assertTrue(report.criticalViolations() >= 1);

        ComplianceControlResult injectionControl = report.controls().stream()
                .filter(c -> "CC6.6".equals(c.controlId()))
                .findFirst()
                .orElse(null);

        assertNotNull(injectionControl, "Control CC6.6 must be evaluated");
        assertEquals(ComplianceControlResult.ComplianceStatus.NON_COMPLIANT, injectionControl.status());
        assertTrue(injectionControl.violatingRules().contains("CR-SQL-001"));
    }

    @Test
    @DisplayName("Evaluate compliance across ISO 27001, PCI-DSS, and OWASP Top 10")
    void testMultiStandardEvaluation() {
        UUID jobId = UUID.randomUUID();
        FindingEntity hardcodedSecret = new FindingEntity();
        hardcodedSecret.setRuleId("CR-SECRET-001");
        hardcodedSecret.setDescription("CWE-798 Hardcoded secret token");
        hardcodedSecret.setOwaspMapping("A02:2021");
        hardcodedSecret.setSeverity(Severity.CRITICAL);
        hardcodedSecret.setCategory(Category.SECURITY);
        hardcodedSecret.setConfidence(Confidence.HIGH);

        // ISO 27001
        CompliancePacketReport isoReport = complianceAuditService.evaluateCompliance(
                jobId, "acme/cloud-api", ComplianceStandard.ISO_27001, List.of(hardcodedSecret)
        );
        assertNotNull(isoReport);
        assertEquals(ComplianceStandard.ISO_27001, isoReport.standard());

        // PCI-DSS v4
        CompliancePacketReport pciReport = complianceAuditService.evaluateCompliance(
                jobId, "acme/cloud-api", ComplianceStandard.PCI_DSS_V4, List.of(hardcodedSecret)
        );
        assertNotNull(pciReport);
        assertEquals(ComplianceStandard.PCI_DSS_V4, pciReport.standard());
        assertTrue(pciReport.criticalViolations() >= 1);

        // OWASP Top 10
        CompliancePacketReport owaspReport = complianceAuditService.evaluateCompliance(
                jobId, "acme/cloud-api", ComplianceStandard.OWASP_TOP_10, List.of(hardcodedSecret)
        );
        assertNotNull(owaspReport);
        assertEquals(ComplianceStandard.OWASP_TOP_10, owaspReport.standard());
    }
}
