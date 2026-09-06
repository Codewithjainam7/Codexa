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

    private AnalysisReportResponse createMockReport() {
        AnalysisMetricResponse metrics = new AnalysisMetricResponse(
                95.0, 90.0, 88.0, 92.0, 89.0,
                15, 15, 0, 1, 2, 3, 450L
        );

        FindingResponse finding = new FindingResponse(
                UUID.randomUUID(),
                "CR-SQL-001",
                Category.SECURITY,
                Severity.HIGH,
                Confidence.HIGH,
                "SQL Injection Vector",
                "Raw parameter concat in query.",
                "Data leakage risk.",
                "Use parameterized query.",
                "OWASP A03:2021",
                "src/main/UserController.java",
                42,
                45,
                "String q = 'SELECT * FROM users';",
                "db.find(id);",
                85.0,
                false,
                List.of("https://owasp.org")
        );

        return new AnalysisReportResponse(
                UUID.randomUUID(),
                "Codexa Code Review & Security Audit",
                "https://github.com/example/repo",
                SourceType.GITHUB,
                92.5,
                ProductionVerdict.GENERALLY_PROMISING,
                "Audit completed with 1 high finding.",
                Instant.now(),
                metrics,
                List.of(finding),
                AnalysisReportResponse.STANDARD_DISCLAIMER
        );
    }

    @Test
    void shouldGenerateValidMarkdownReport() {
        AnalysisReportResponse report = createMockReport();
        String markdown = exportService.generateMarkdownReport(report);

        assertNotNull(markdown);
        assertTrue(markdown.contains("# Codexa Production Readiness Report"));
        assertTrue(markdown.contains("CR-SQL-001"));
        assertTrue(markdown.contains("Maintainability Score:"));
        assertTrue(markdown.contains("Architectural Score:"));
        assertTrue(markdown.contains("SQL Injection Vector"));
    }

    @Test
    void shouldGenerateValidHtmlReport() {
        AnalysisReportResponse report = createMockReport();
        String html = exportService.generateHtmlReport(report);

        assertNotNull(html);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Codexa Audit Report"));
        assertTrue(html.contains("Maintainability Index"));
        assertTrue(html.contains("Architectural Health"));
        assertTrue(html.contains("CR-SQL-001"));
    }

    @Test
    void shouldGenerateValidJsonReport() {
        AnalysisReportResponse report = createMockReport();
        String json = exportService.generateJsonReport(report);

        assertNotNull(json);
        assertTrue(json.contains("\"jobId\""));
        assertTrue(json.contains("CR-SQL-001"));
        assertTrue(json.contains("maintainabilityScore"));
        assertTrue(json.contains("architecturalScore"));
    }
}
