package com.codexa.analysis.controller;

import com.codexa.analysis.model.*;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.analysis.service.ReportExportService;
import com.codexa.analysis.service.SbomDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
@Tag(name = "Analyses", description = "Endpoints for inspecting analysis jobs, findings, and reports")
public class AnalysisJobController {

    private final AnalysisJobService jobService;
    private final ReportExportService reportExportService;
    private final SbomDependencyService sbomDependencyService;

    public AnalysisJobController(AnalysisJobService jobService, ReportExportService reportExportService) {
        this(jobService, reportExportService, new SbomDependencyService());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AnalysisJobController(
            AnalysisJobService jobService,
            ReportExportService reportExportService,
            SbomDependencyService sbomDependencyService
    ) {
        this.jobService = jobService;
        this.reportExportService = reportExportService;
        this.sbomDependencyService = sbomDependencyService;
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
            @RequestParam(defaultValue = "1000") int size
    ) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = Math.min(Math.max(1, size), 5000);
        return ResponseEntity.ok(jobService.getFindings(jobId, category, severity, confidence, search, sanitizedPage, sanitizedSize));
    }

    @GetMapping("/{jobId}/report")
    @Operation(summary = "Get analysis report", description = "Returns a complete downloadable or viewable analysis report in JSON, HTML, or Markdown.")
    public ResponseEntity<?> getReport(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "false") boolean download,
            @RequestParam(defaultValue = "false") boolean view
    ) {
        AnalysisReportResponse report = jobService.generateReport(jobId);
        String disposition = view ? "inline" : "attachment";

        if ("html".equalsIgnoreCase(format) || "pdf".equalsIgnoreCase(format)) {
            String html = reportExportService.generateHtmlReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"codexa-report-" + jobId + ".html\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } else if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
            String markdown = reportExportService.generateMarkdownReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"codexa-report-" + jobId + ".md\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(markdown);
                } else if ("sarif".equalsIgnoreCase(format)) {
            String sarif = reportExportService.generateSarifReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"codexa-report-\" + jobId + \".sarif\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(sarif);
        } else if ("csv".equalsIgnoreCase(format)) {
            String csv = reportExportService.generateCsvReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"codexa-report-" + jobId + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csv);
        }

        if (download) {
            String json = reportExportService.generateJsonReport(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codexa-report-" + jobId + ".json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        return ResponseEntity.ok(report);
    }

    @GetMapping("/{jobId}/export")
    @Operation(summary = "Download analysis report attachment", description = "Downloads an audit report file in JSON, HTML, or Markdown.")
    public ResponseEntity<?> exportReport(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "json") String format
    ) {
        return getReport(jobId, format, true, false);
    }

    @GetMapping("/{jobId}/sbom")
    @Operation(summary = "Get Software Bill of Materials (SBOM)", description = "Returns machine-readable CycloneDX v1.5 or SPDX v2.3 SBOM JSON cataloging all repository dependencies and supply-chain vulnerabilities.")
    public ResponseEntity<?> getSbom(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "cyclonedx") String format
    ) {
        com.codexa.persistence.entity.AnalysisJobEntity entity = jobService.getJobOrThrow(jobId);
        String projectName = entity.getSourceIdentifier() != null && !entity.getSourceIdentifier().isBlank()
                ? entity.getSourceIdentifier() : "Codexa-Job-" + jobId;
        SbomReport report = new SbomReport(jobId, projectName, "1.5", entity.getCreatedAt(), 0, 0, List.of(), List.of());
        if ("cyclonedx".equalsIgnoreCase(format)) {
            String json = sbomDependencyService.exportCycloneDxJson(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codexa-sbom-" + jobId + ".cyclonedx.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }
        return ResponseEntity.ok(report);
    }
}
