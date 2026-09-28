package com.codexa.compliance.service;

import com.codexa.compliance.model.ComplianceControlResult;
import com.codexa.compliance.model.ComplianceControlResult.ComplianceStatus;
import com.codexa.compliance.model.CompliancePacketReport;
import com.codexa.compliance.model.CompliancePacketReport.OverallComplianceStatus;
import com.codexa.compliance.model.ComplianceStandard;
import com.codexa.persistence.entity.FindingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Compliance Audit & Regulatory Framework Verification Service.
 * Evaluates static findings against SOC 2 Type II, ISO/IEC 27001, PCI-DSS v4.0, and OWASP Top 10.
 */
@Service
public class ComplianceAuditService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceAuditService.class);

    private record ControlDefinition(
            String id,
            String name,
            String description,
            Set<String> rulePrefixes,
            Set<String> cweIds
    ) {}

    public CompliancePacketReport evaluateCompliance(
            UUID jobId,
            String targetRepository,
            ComplianceStandard standard,
            List<FindingEntity> findings
    ) {
        List<ControlDefinition> controlDefs = getDefinitionsForStandard(standard);
        List<ComplianceControlResult> controlResults = new ArrayList<>();

        int totalViolations = 0;
        int totalCriticals = 0;
        int compliantControls = 0;
        int nonCompliantControls = 0;

        for (ControlDefinition def : controlDefs) {
            List<FindingEntity> matched = findings.stream()
                    .filter(f -> matchesControl(f, def))
                    .toList();

            int findingCount = matched.size();
            int criticalCount = (int) matched.stream().filter(f -> "CRITICAL".equalsIgnoreCase(f.getSeverity().name())).count();
            int highCount = (int) matched.stream().filter(f -> "HIGH".equalsIgnoreCase(f.getSeverity().name())).count();

            totalViolations += findingCount;
            totalCriticals += criticalCount;

            ComplianceStatus status;
            if (findingCount == 0) {
                status = ComplianceStatus.COMPLIANT;
                compliantControls++;
            } else if (criticalCount > 0 || highCount > 2) {
                status = ComplianceStatus.NON_COMPLIANT;
                nonCompliantControls++;
            } else {
                status = ComplianceStatus.REQUIRES_AUDIT;
            }

            List<String> violatingRules = matched.stream()
                    .map(FindingEntity::getRuleId)
                    .distinct()
                    .toList();

            controlResults.add(new ComplianceControlResult(
                    def.id(),
                    def.name(),
                    status,
                    findingCount,
                    criticalCount,
                    highCount,
                    def.description(),
                    violatingRules
            ));
        }

        double complianceScore = controlDefs.isEmpty() ? 100.0 :
                Math.round((compliantControls * 100.0 / controlDefs.size()) * 10.0) / 10.0;

        OverallComplianceStatus overallStatus;
        if (nonCompliantControls == 0 && complianceScore >= 85.0) {
            overallStatus = OverallComplianceStatus.AUDIT_READY;
        } else if (nonCompliantControls <= 1 && totalCriticals == 0) {
            overallStatus = OverallComplianceStatus.CONDITIONAL_PASS;
        } else {
            overallStatus = OverallComplianceStatus.AUDIT_BLOCKED;
        }

        String summary = buildExecutiveSummary(standard, overallStatus, complianceScore, compliantControls, controlDefs.size(), totalViolations, totalCriticals);

        return new CompliancePacketReport(
                jobId,
                standard,
                targetRepository,
                Instant.now(),
                overallStatus,
                complianceScore,
                controlDefs.size(),
                compliantControls,
                nonCompliantControls,
                totalViolations,
                totalCriticals,
                controlResults,
                summary
        );
    }

    private boolean matchesControl(FindingEntity finding, ControlDefinition def) {
        String ruleId = finding.getRuleId() != null ? finding.getRuleId().toUpperCase() : "";
        for (String prefix : def.rulePrefixes()) {
            if (ruleId.startsWith(prefix.toUpperCase())) {
                return true;
            }
        }

        String owasp = finding.getOwaspMapping();
        if (owasp != null) {
            String normOwasp = owasp.trim().toUpperCase();
            if (normOwasp.contains(def.id().toUpperCase()) || def.id().toUpperCase().contains(normOwasp)) {
                return true;
            }
        }

        String desc = finding.getDescription();
        if (desc != null) {
            for (String cwe : def.cweIds()) {
                if (desc.contains(cwe)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String buildExecutiveSummary(
            ComplianceStandard standard,
            OverallComplianceStatus status,
            double score,
            int compliant,
            int total,
            int violations,
            int criticals
    ) {
        if (status == OverallComplianceStatus.AUDIT_READY) {
            return String.format(
                    "The audited codebase satisfies %s requirements with an enterprise compliance score of %.1f%% (%d of %d controls satisfied). Zero critical compliance blockers were identified across static attack surfaces.",
                    standard.getDisplayName(), score, compliant, total
            );
        } else if (status == OverallComplianceStatus.CONDITIONAL_PASS) {
            return String.format(
                    "The audited codebase achieved a conditional pass under %s with %.1f%% control satisfaction (%d of %d controls compliant). Minor non-critical remediations are required prior to final attestation.",
                    standard.getDisplayName(), score, compliant, total
            );
        } else {
            return String.format(
                    "Compliance audit under %s is currently BLOCKED (%.1f%% compliant). Identified %d total violations including %d critical risk items that directly breach mandatory regulatory controls.",
                    standard.getDisplayName(), score, violations, criticals
            );
        }
    }

    private List<ControlDefinition> getDefinitionsForStandard(ComplianceStandard standard) {
        return switch (standard) {
            case SOC2_TYPE2 -> List.of(
                    new ControlDefinition("CC6.1", "Logical Access Controls & Credential Protection", "Enforce logical access boundaries, preventing hardcoded credentials and broken access control.", Set.of("CR-SECRET", "CR-AUTH", "CR-RLS"), Set.of("CWE-798", "CWE-306", "CWE-639")),
                    new ControlDefinition("CC6.6", "Perimeter Security & Injection Defenses", "Prevent injection vulnerabilities across application perimeter inputs.", Set.of("CR-SQL", "CR-CMD", "CR-XSS", "CR-SSRF"), Set.of("CWE-89", "CWE-78", "CWE-79", "CWE-918")),
                    new ControlDefinition("CC6.7", "Data Transmission Encryption & Cryptography", "Enforce robust cryptographic ciphers, hashing functions, and TLS configuration.", Set.of("CR-CRYPTO", "CR-HASH", "CR-RAND", "CR-CONFIG"), Set.of("CWE-327", "CWE-328", "CWE-338", "CWE-295")),
                    new ControlDefinition("CC6.8", "Malicious Software Prevention & Supply Chain", "Prevent execution of vulnerable third-party components and unsafe deserialization.", Set.of("CR-DEP", "CR-DESER"), Set.of("CWE-1395", "CWE-502")),
                    new ControlDefinition("CC7.1", "Vulnerability Detection & Static Governance", "Enforce static analysis scanning and manage code complexity maintainability.", Set.of("CR-QUAL"), Set.of("CWE-1074", "CWE-1075", "CWE-400")),
                    new ControlDefinition("CC7.2", "Security Event Logging & Monitoring", "Ensure system events are recorded without leaking PII or credentials into logs.", Set.of("CR-LOG"), Set.of("CWE-532", "CWE-390"))
            );
            case ISO_27001 -> List.of(
                    new ControlDefinition("A.8.20", "Network Security & Cross-Domain Isolation", "Secure web communication boundaries and prevent SSRF / permissive CORS.", Set.of("CR-SSRF", "CR-CORS"), Set.of("CWE-918", "CWE-942")),
                    new ControlDefinition("A.8.24", "Use of Cryptography", "Proper implementation of cryptography avoiding broken hashes or PRNGs.", Set.of("CR-CRYPTO", "CR-HASH", "CR-RAND"), Set.of("CWE-327", "CWE-328", "CWE-338")),
                    new ControlDefinition("A.8.28", "Secure Coding Principles", "Defend custom application code against injection, deserialization, and traversal flaws.", Set.of("CR-SQL", "CR-CMD", "CR-PATH", "CR-DESER"), Set.of("CWE-89", "CWE-78", "CWE-22", "CWE-502")),
                    new ControlDefinition("A.8.29", "Security Testing in Development", "Automated static analysis gates integrated into software development lifecycle.", Set.of("CR-QUAL", "CR-PARAM"), Set.of("CWE-1074", "CWE-1321")),
                    new ControlDefinition("A.8.30", "Outsourced Development & Supply Chain", "Verify third-party open source dependencies against known vulnerabilities.", Set.of("CR-DEP"), Set.of("CWE-1395"))
            );
            case PCI_DSS_V4 -> List.of(
                    new ControlDefinition("Req-3.4", "Protect Cardholder Data & Key Storage", "Prevent plaintext key and secret exposure; enforce approved cryptography.", Set.of("CR-SECRET", "CR-CRYPTO", "CR-HASH"), Set.of("CWE-798", "CWE-327", "CWE-328")),
                    new ControlDefinition("Req-6.2", "Secure Software Development Lifecycle", "Mitigate common software vulnerabilities including injection, traversal, and XSS.", Set.of("CR-SQL", "CR-CMD", "CR-PATH", "CR-XSS"), Set.of("CWE-89", "CWE-78", "CWE-22", "CWE-79")),
                    new ControlDefinition("Req-6.3", "Supply Chain Component Security", "Manage and audit third-party open-source software libraries for CVEs.", Set.of("CR-DEP", "CR-DESER"), Set.of("CWE-1395", "CWE-502")),
                    new ControlDefinition("Req-10.2", "Audit Logging & Sensitive Data Redaction", "Record security events while preventing credential or PAN leakage to logs.", Set.of("CR-LOG"), Set.of("CWE-532"))
            );
            default -> List.of(
                    new ControlDefinition("A01:2021", "Broken Access Control", "Enforce authorization checks and mitigate path traversal / IDOR.", Set.of("CR-AUTH", "CR-PATH", "CR-RLS", "CR-CORS"), Set.of("CWE-22", "CWE-306", "CWE-639", "CWE-942")),
                    new ControlDefinition("A02:2021", "Cryptographic Failures", "Mitigate weak ciphers, outdated hashing, and hardcoded credentials.", Set.of("CR-CRYPTO", "CR-HASH", "CR-SECRET", "CR-RAND"), Set.of("CWE-327", "CWE-328", "CWE-798", "CWE-338")),
                    new ControlDefinition("A03:2021", "Injection", "Prevent SQL, OS Command, and Cross-Site Scripting injections.", Set.of("CR-SQL", "CR-CMD", "CR-XSS"), Set.of("CWE-89", "CWE-78", "CWE-79")),
                    new ControlDefinition("A05:2021", "Security Misconfiguration", "Eliminate insecure defaults, disabled TLS, and exposed actuator ports.", Set.of("CR-CONFIG", "CR-DEBUG", "CR-CSRF"), Set.of("CWE-295", "CWE-489", "CWE-352")),
                    new ControlDefinition("A06:2021", "Vulnerable Components", "Identify outdated or CVE-vulnerable software dependencies in manifests.", Set.of("CR-DEP"), Set.of("CWE-1395")),
                    new ControlDefinition("A09:2021", "Logging and Monitoring Failures", "Prevent log injection and confidential token disclosure in loggers.", Set.of("CR-LOG"), Set.of("CWE-532", "CWE-390")),
                    new ControlDefinition("A10:2021", "Server-Side Request Forgery", "Block unvalidated outbound HTTP calls to internal metadata and private IPs.", Set.of("CR-SSRF"), Set.of("CWE-918"))
            );
        };
    }
}
