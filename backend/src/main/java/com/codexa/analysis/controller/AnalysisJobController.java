package com.codexa.analysis.controller;

import com.codexa.analysis.model.*;
import com.codexa.analysis.service.AnalysisJobService;
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

    public AnalysisJobController(AnalysisJobService jobService) {
        this.jobService = jobService;
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
    @Operation(summary = "Get analysis report", description = "Returns a complete downloadable or viewable analysis report.")
    public ResponseEntity<?> getReport(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "json") String format
    ) {
        AnalysisReportResponse report = jobService.generateReport(jobId);

        if ("html".equalsIgnoreCase(format)) {
            String html = generateSimpleHtmlReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codexa-report-" + jobId + ".html\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        }

        return ResponseEntity.ok(report);
    }

    private String generateSimpleHtmlReport(AnalysisReportResponse report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Codexa Report - ")
                .append(report.jobId()).append("</title>")
                .append("<style>body{font-family:sans-serif;margin:40px;line-height:1.6;color:#333;}")
                .append(".score{font-size:32px;font-weight:bold;color:#2563eb;}")
                .append(".disclaimer{background:#fef3c7;border-left:4px solid #f59e0b;padding:12px;margin:20px 0;}")
                .append("table{width:100%;border-collapse:collapse;margin-top:20px;}")
                .append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}")
                .append("th{background:#f8fafc;}")
                .append("</style></head><body>");

        sb.append("<h1>Codexa Production Readiness Report</h1>");
        sb.append("<p><strong>Target:</strong> ").append(report.scanTarget()).append("</p>");
        sb.append("<p><strong>Job ID:</strong> ").append(report.jobId()).append("</p>");
        sb.append("<p><strong>Verdict:</strong> ").append(report.verdict()).append("</p>");
        sb.append("<div class='score'>Readiness Score: ").append(report.overallScore() != null ? report.overallScore() : "Pending").append("/100</div>");
        sb.append("<div class='disclaimer'>").append(report.disclaimer()).append("</div>");

        sb.append("<h2>Findings (").append(report.findings().size()).append(")</h2>");
        sb.append("<table><thead><tr><th>Rule ID</th><th>Severity</th><th>Category</th><th>File</th><th>Line</th><th>Title</th></tr></thead><tbody>");
        for (FindingResponse f : report.findings()) {
            sb.append("<tr>")
                    .append("<td>").append(f.ruleId()).append("</td>")
                    .append("<td>").append(f.severity()).append("</td>")
                    .append("<td>").append(f.category()).append("</td>")
                    .append("<td>").append(f.filePath()).append("</td>")
                    .append("<td>").append(f.startLine()).append("</td>")
                    .append("<td>").append(f.title()).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
