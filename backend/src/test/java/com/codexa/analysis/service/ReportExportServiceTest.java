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
                "SQL Injection Detected",
                "Concatenating raw SQL statements",
                "Use parameterized PreparedStatement",
                Severity.CRITICAL,
                Confidence.CONFIRMED,
                Category.SECURITY,
                "src/main/java/UserRepo.java",
                42,
                "SELECT * FROM users WHERE id = '" + id",
                "SELECT * FROM users WHERE id = ?",
                "A03:2021-Injection"
        );

        AnalysisReportResponse report = new AnalysisReportResponse(
                jobId,
                SourceType.ZIP,
                "project.zip",
                AnalysisJobStatus.COMPLETED,
                "Ready for review",
                ProductionVerdict.NEEDS_URGENT_FIXES,
                65.0,
                new AnalysisMetricResponse(10, 1200, 1, 0, 0, 0, 500L),
                List.of(finding),
                Instant.now(),
                Instant.now(),
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
}
