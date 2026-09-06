package com.codexa.analysis.controller;

import com.codexa.analysis.model.AnalysisMetricResponse;
import com.codexa.analysis.model.AnalysisReportResponse;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.model.SourceType;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.analysis.service.ReportExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalysisJobControllerReportTest {

    private MockMvc mockMvc;
    private UUID testJobId;
    private AnalysisReportResponse mockReport;

    @BeforeEach
    void setUp() {
        testJobId = UUID.randomUUID();
        AnalysisMetricResponse metrics = new AnalysisMetricResponse(
                95.0, 95.0, 95.0, 95.0, 95.0,
                10, 10,
                0, 0, 0, 0, 150L
        );
        mockReport = new AnalysisReportResponse(
                testJobId,
                "Codexa Code Review & Security Audit",
                "test-repo.zip",
                SourceType.ZIP,
                95.0,
                ProductionVerdict.REVIEW_COMPLETE,
                "Production ready",
                Instant.now(),
                metrics,
                List.of(),
                AnalysisReportResponse.STANDARD_DISCLAIMER
        );

        AnalysisJobService stubJobService = new AnalysisJobService(null, null, null) {
            @Override
            public AnalysisReportResponse generateReport(UUID jobId) {
                return mockReport;
            }
        };

        ReportExportService reportExportService = new ReportExportService();
        AnalysisJobController controller = new AnalysisJobController(stubJobService, reportExportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void exportEndpointsShouldReturnAppropriateContentAndHeaders() throws Exception {
        // Test HTML export
        mockMvc.perform(get("/api/v1/analyses/" + testJobId + "/report").param("format", "html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"codexa-report-" + testJobId + ".html\""));

        // Test Markdown export
        mockMvc.perform(get("/api/v1/analyses/" + testJobId + "/report").param("format", "markdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"codexa-report-" + testJobId + ".md\""));

        // Test JSON download via /export
        mockMvc.perform(get("/api/v1/analyses/" + testJobId + "/export").param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"codexa-report-" + testJobId + ".json\""));
    }
}
