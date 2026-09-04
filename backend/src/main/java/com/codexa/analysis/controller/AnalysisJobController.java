package com.codexa.analysis.controller;

import com.codexa.analysis.model.*;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.analysis.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
@Tag(name = "Analyses", description = "Endpoints for inspecting analysis jobs, findings, and reports")
public class AnalysisJobController {

    private final AnalysisJobService jobService;
    private final ReportExportService reportExportService;

    public AnalysisJobController(AnalysisJobService jobService, ReportExportService reportExportService) {
        this.jobService = jobService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get analysis job summary", description = "Retrieves current job status, execution stage, score breakdown, and top action items.")
    public ResponseEntity<AnalysisJobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobResponse(jobId));
    }

    @GetMapping("/{jobId}/findings")
    @Operation(summary = "Get paginated findings", description = "Retrieves filterable, paginated static analysis and security findings for a job.")
    public ResponseEntity<Page<FindingResponse>> getFindings(
            @PathVariable UUID jobId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Confidence confidence,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(jobService.getFindings(jobId, category, severity, confidence, search, page, size));
    }

    @GetMapping("/{jobId}/report")
    @Operation(summary = "Get analysis report", description = "Returns a complete downloadable or viewable analysis report in JSON, HTML, or Markdown.")
    public ResponseEntity<?> getReport(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "json") String format
    ) {
        AnalysisReportResponse report = jobService.generateReport(jobId);

        if ("html".equalsIgnoreCase(format)) {
            String html = reportExportService.generateHtmlReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codexa-report-" + jobId + ".html\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } else if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
            String markdown = reportExportService.generateMarkdownReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codexa-report-" + jobId + ".md\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(markdown);
        }

        return ResponseEntity.ok(report);
    }
}
