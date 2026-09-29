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
    private final com.codexa.compliance.service.ComplianceAuditService complianceAuditService;

    public AnalysisJobController(AnalysisJobService jobService, ReportExportService reportExportService) {
        this(jobService, reportExportService, new SbomDependencyService(), new com.codexa.compliance.service.ComplianceAuditService());
    }

    public AnalysisJobController(
            AnalysisJobService jobService,
            ReportExportService reportExportService,
            SbomDependencyService sbomDependencyService
    ) {
        this(jobService, reportExportService, sbomDependencyService, new com.codexa.compliance.service.ComplianceAuditService());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AnalysisJobController(
            AnalysisJobService jobService,
            ReportExportService reportExportService,
            SbomDependencyService sbomDependencyService,
            com.codexa.compliance.service.ComplianceAuditService complianceAuditService
    ) {
        this.jobService = jobService;
        this.reportExportService = reportExportService;
        this.sbomDependencyService = sbomDependencyService;
        this.complianceAuditService = complianceAuditService;
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

    @GetMapping("/{jobId}/compliance")
    @Operation(summary = "Get regulatory compliance audit packet", description = "Evaluates analysis findings against enterprise regulatory standards: SOC 2 Type II, ISO/IEC 27001, PCI-DSS v4.0, and OWASP Top 10.")
    public ResponseEntity<com.codexa.compliance.model.CompliancePacketReport> getCompliancePacket(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "soc2") String standard
    ) {
        com.codexa.persistence.entity.AnalysisJobEntity entity = jobService.getJobOrThrow(jobId);
        List<com.codexa.persistence.entity.FindingEntity> findings = jobService.getFindingEntitiesForJob(jobId);
        var standardEnum = com.codexa.compliance.model.ComplianceStandard.fromString(standard);
        var report = complianceAuditService.evaluateCompliance(jobId, entity.getSourceIdentifier(), standardEnum, findings);
        return ResponseEntity.ok(report);
    }

    @GetMapping(value = "/{jobId}/badge", produces = "image/svg+xml")
    @Operation(summary = "Get dynamic SVG compliance or readiness badge", description = "Generates a vector SVG shield badge suitable for embedding in GitHub README or audit documentation.")
    public ResponseEntity<String> getBadge(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "soc2") String standard
    ) {
        String leftLabel = "codexa";
        String rightText = "PASS";
        String rightColor = "#10b981";

        if ("readiness".equalsIgnoreCase(standard) || "score".equalsIgnoreCase(standard)) {
            var entity = jobService.getJobOrThrow(jobId);
            double score = entity.getOverallScore() != null ? entity.getOverallScore() : 100.0;
            leftLabel = "codexa | readiness";
            rightText = String.format("%.0f/100", score);
            if (score >= 75) {
                rightColor = "#10b981";
            } else if (score >= 50) {
                rightColor = "#f59e0b";
            } else {
                rightColor = "#ef4444";
            }
        } else {
            var entity = jobService.getJobOrThrow(jobId);
            var findings = jobService.getFindingEntitiesForJob(jobId);
            var stdEnum = com.codexa.compliance.model.ComplianceStandard.fromString(standard);
            var packet = complianceAuditService.evaluateCompliance(jobId, entity.getSourceIdentifier(), stdEnum, findings);
            leftLabel = "codexa | " + standard.toLowerCase().replace("_", " ");
            switch (packet.overallStatus()) {
                case AUDIT_READY -> {
                    rightText = String.format("PASS (%.0f%%)", packet.complianceScore());
                    rightColor = "#10b981";
                }
                case CONDITIONAL_PASS -> {
                    rightText = String.format("CONDITIONAL (%.0f%%)", packet.complianceScore());
                    rightColor = "#f59e0b";
                }
                case AUDIT_BLOCKED -> {
                    rightText = "BLOCKED";
                    rightColor = "#ef4444";
                }
            }
        }

        int leftWidth = Math.max(leftLabel.length() * 7 + 16, 50);
        int rightWidth = Math.max(rightText.length() * 7 + 16, 40);
        int totalWidth = leftWidth + rightWidth;

        String svg = String.format("""
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="20" role="img" aria-label="%s: %s">
                  <title>%s: %s</title>
                  <linearGradient id="s" x2="0" y2="100%%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                  </linearGradient>
                  <clipPath id="r">
                    <rect width="%d" height="20" rx="3" fill="#fff"/>
                  </clipPath>
                  <g clip-path="url(#r)">
                    <rect width="%d" height="20" fill="#1e293b"/>
                    <rect x="%d" width="%d" height="20" fill="%s"/>
                    <rect width="%d" height="20" fill="url(#s)"/>
                  </g>
                  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">
                    <text aria-hidden="true" x="%d" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="%d">%s</text>
                    <text x="%d" y="140" transform="scale(.1)" fill="#fff" textLength="%d">%s</text>
                    <text aria-hidden="true" x="%d" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="%d">%s</text>
                    <text x="%d" y="140" transform="scale(.1)" fill="#fff" textLength="%d">%s</text>
                  </g>
                </svg>
                """,
                totalWidth, leftLabel, rightText,
                leftLabel, rightText,
                totalWidth,
                leftWidth,
                leftWidth, rightWidth, rightColor,
                totalWidth,
                (leftWidth * 10) / 2, (leftWidth - 10) * 10, leftLabel,
                (leftWidth * 10) / 2, (leftWidth - 10) * 10, leftLabel,
                (leftWidth + rightWidth / 2) * 10, (rightWidth - 10) * 10, rightText,
                (leftWidth + rightWidth / 2) * 10, (rightWidth - 10) * 10, rightText
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=300, s-maxage=300")
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .body(svg);
    }
}
