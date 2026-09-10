package com.codexa.analysis.service;

import com.codexa.analysis.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReportExportServiceTest {

    private ReportExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ReportExportService();
    }

    @Test
    void shouldGenerateValidCsvReport() {
        UUID jobId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        FindingResponse finding = new FindingResponse(
                findingId,
                "CR-SQL-001",
                Category.SECURITY,
                Severity.CRITICAL,
                Confidence.HIGH,
                "SQL Injection Detected",
                "Concatenating raw SQL statements",
                "Full database compromise",
                "Use parameterized PreparedStatement",
                "A03:2021-Injection",
                "src/main/java/UserRepo.java",
                42,
                45,
                "SELECT * FROM users WHERE id = '...'",
                "SELECT * FROM users WHERE id = ?",
                9.5,
                false,
                List.of("https://owasp.org")
        );

        AnalysisReportResponse report = new AnalysisReportResponse(
                jobId,
                "TestApp",
                "project.zip",
                SourceType.ZIP,
                65.0,
                ProductionVerdict.NEEDS_URGENT_FIXES,
                "Ready for review",
                Instant.now(),
                new AnalysisMetricResponse(80.0, 70.0, 75.0, 10, 10, 1, 0, 0, 0, 500L),
                List.of(finding),
                "Disclaimer text",
                null
        );

        String csv = exportService.generateCsvReport(report);
        assertNotNull(csv);
        assertTrue(csv.startsWith("Finding ID,Rule ID,Category,Severity"));
        assertTrue(csv.contains("CR-SQL-001"));
        assertTrue(csv.contains("SQL Injection Detected"));
        assertTrue(csv.contains("CRITICAL"));
        assertTrue(csv.contains("UserRepo.java"));
    }

    @Test
    void shouldGenerateValidSarifReport() {
        UUID jobId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        FindingResponse finding = new FindingResponse(
                findingId, "CR-SEC-009", Category.SECURITY, Severity.HIGH, Confidence.HIGH,
                "Weak Hash", "MD5 detected", "Collision attack", "Use SHA-256",
                "A02:2021", "Crypto.java", 12, 12, "MD5", "SHA-256", 8.0, false, List.of()
        );
        AnalysisReportResponse report = new AnalysisReportResponse(
                jobId, "App", "app.zip", SourceType.ZIP, 80.0, ProductionVerdict.PRODUCTION_READY,
                "Pass", Instant.now(), new AnalysisMetricResponse(80.0, 80.0, 80.0, 5, 5, 0, 1, 0, 0, 100L),
                List.of(finding), "Disclaimer", null
        );

        String sarif = exportService.generateSarifReport(report);
        assertNotNull(sarif);
        assertTrue(sarif.contains("\"version\": \"2.1.0\""));
        assertTrue(sarif.contains("CR-SEC-009"));
        assertTrue(sarif.contains("Codexa Security Engine"));
    }

}
