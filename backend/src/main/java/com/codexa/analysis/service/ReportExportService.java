package com.codexa.analysis.service;

import com.codexa.analysis.model.AnalysisReportResponse;
import com.codexa.analysis.model.FindingResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for exporting rich static audit reports in HTML, Markdown, and JSON.
 */
@Service
public class ReportExportService {

    public String generateHtmlReport(AnalysisReportResponse report) {
        StringBuilder sb = new StringBuilder();
        String verdictStr = report.verdict() != null ? report.verdict().name() : "PENDING";
        
        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Codexa Audit Report - """).append(report.jobId()).append("""
                </title>
                <style>
                    :root {
                        --bg: #0f172a;
                        --card-bg: #1e293b;
                        --border: #334155;
                        --text: #f8fafc;
                        --text-muted: #94a3b8;
                        --accent-blue: #38bdf8;
                        --accent-green: #22c55e;
                        --accent-red: #ef4444;
                        --accent-orange: #f97316;
                        --accent-yellow: #eab308;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        background: var(--bg);
                        color: var(--text);
                        margin: 0;
                        padding: 32px 20px;
                        line-height: 1.5;
                    }
                    .container { max-width: 1000px; margin: 0 auto; }
                    .header { border-bottom: 1px solid var(--border); padding-bottom: 24px; margin-bottom: 32px; }
                    .brand { font-size: 24px; font-weight: 800; color: var(--accent-blue); letter-spacing: -0.5px; }
                    .tagline { color: var(--text-muted); font-size: 14px; margin-top: 4px; }
                    .disclaimer {
                        background: rgba(245, 158, 11, 0.1);
                        border-left: 4px solid var(--accent-orange);
                        padding: 14px 18px;
                        border-radius: 6px;
                        margin: 20px 0;
                        font-size: 13px;
                        color: #fcd34d;
                    }
                    .grid-cards {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 16px;
                        margin-bottom: 32px;
                    }
                    .card {
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 10px;
                        padding: 20px;
                    }
                    .card-label { font-size: 12px; font-weight: 600; text-transform: uppercase; color: var(--text-muted); }
                    .card-value { font-size: 32px; font-weight: 800; margin-top: 8px; color: var(--text); }
                    .card-value.green { color: var(--accent-green); }
                    .card-value.red { color: var(--accent-red); }
                    .card-value.orange { color: var(--accent-orange); }
                    .finding {
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 8px;
                        padding: 20px;
                        margin-bottom: 20px;
                    }
                    .finding-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
                    .badge {
                        display: inline-block;
                        padding: 3px 8px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: 700;
                        text-transform: uppercase;
                    }
                    .badge-critical { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid #ef4444; }
                    .badge-high { background: rgba(249, 115, 22, 0.2); color: #fb923c; border: 1px solid #f97316; }
                    .badge-medium { background: rgba(234, 179, 8, 0.2); color: #fde047; border: 1px solid #eab308; }
                    .badge-low { background: rgba(56, 189, 248, 0.2); color: #7dd3fc; border: 1px solid #38bdf8; }
                    .code-block {
                        background: #090d16;
                        border: 1px solid #1e293b;
                        border-radius: 6px;
                        padding: 14px;
                        font-family: monospace;
                        font-size: 13px;
                        overflow-x: auto;
                        margin: 12px 0;
                    }
                    .code-diff { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 12px; }
                    @media (max-width: 768px) { .code-diff { grid-template-columns: 1fr; } }
                    .diff-box { background: #0b1120; border: 1px solid #1e293b; border-radius: 6px; padding: 12px; }
                    .diff-box.vuln { border-top: 3px solid var(--accent-red); }
                    .diff-box.fix { border-top: 3px solid var(--accent-green); }
                    .diff-title { font-size: 12px; font-weight: 700; margin-bottom: 6px; }
                    .diff-title.vuln { color: #f87171; }
                    .diff-title.fix { color: #4ade80; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="brand">CODEXA</div>
                        <div class="tagline">Deterministic AST Security & Production Readiness Assessment</div>
                        <div style="margin-top: 12px; font-size: 14px; color: var(--text-muted);">
                            Target: <strong style="color: var(--text);">""").append(escapeHtml(report.scanTarget())).append("""
                            </strong> &bull; Analyzed: """).append(Instant.now().toString()).append("""
                        </div>
                    </div>
                    
                    <div class="disclaimer">
                        <strong>Important Security Advisory:</strong> """).append(escapeHtml(report.disclaimer())).append("""
                    </div>

                    <div class="grid-cards">
                        <div class="card">
                            <div class="card-label">Readiness Score</div>
                            <div class="card-value """).append(getScoreColor(report.overallScore())).append("""
                            ">""").append(report.overallScore() != null ? String.format("%.1f", report.overallScore()) : "N/A").append("""
                            /100</div>
                        </div>
                        <div class="card">
                            <div class="card-label">Verdict</div>
                            <div class="card-value" style="font-size: 20px; margin-top: 14px;">""").append(escapeHtml(verdictStr)).append("""
                            </div>
                        </div>
                        <div class="card">
                            <div class="card-label">Maintainability Index</div>
                            <div class="card-value """).append(report.metrics() != null ? getScoreColor(report.metrics().maintainabilityScore()) : "green").append("""
                            ">""").append(report.metrics() != null ? String.format("%.1f", report.metrics().maintainabilityScore()) : "100.0").append("""
                            /100</div>
                        </div>
                        <div class="card">
                            <div class="card-label">Architectural Health</div>
                            <div class="card-value """).append(report.metrics() != null ? getScoreColor(report.metrics().architecturalScore()) : "green").append("""
                            ">""").append(report.metrics() != null ? String.format("%.1f", report.metrics().architecturalScore()) : "100.0").append("""
                            /100</div>
                        </div>
                        <div class="card">
                            <div class="card-label">Total Findings</div>
                            <div class="card-value">""").append(report.findings().size()).append("""
                            </div>
                        </div>
                    </div>

                    <h2 style="font-size: 20px; font-weight: 700; margin-top: 40px;">Detailed Findings Catalog</h2>
            """);

        if (report.findings().isEmpty()) {
            sb.append("<div class='finding'><p style='color: var(--accent-green); margin: 0;'>No static security vulnerabilities or quality defects detected in this codebase.</p></div>");
        } else {
            for (FindingResponse f : report.findings()) {
                String badgeClass = "badge-" + (f.severity() != null ? f.severity().name().toLowerCase() : "low");
                sb.append("<div class='finding'>");
                sb.append("<div class='finding-header'>");
                sb.append("<div><strong>").append(escapeHtml(f.title())).append("</strong> &bull; <span style='font-family: monospace; font-size: 13px; color: var(--accent-blue);'>").append(f.ruleId()).append("</span></div>");
                sb.append("<div><span class='badge ").append(badgeClass).append("'>").append(f.severity()).append("</span></div>");
                sb.append("</div>");

                sb.append("<div style='font-size: 13px; color: var(--text-muted); margin-bottom: 8px;'>Location: <span style='color: var(--text);'>").append(escapeHtml(f.filePath())).append(":").append(f.startLine()).append("</span> &bull; Priority Score: <strong>").append(String.format("%.1f", f.priorityScore())).append("</strong></div>");

                if (f.description() != null && !f.description().isBlank()) {
                    sb.append("<p style='font-size: 14px; margin: 10px 0;'>").append(escapeHtml(f.description())).append("</p>");
                }

                if (f.evidenceMasked() != null || f.suggestedFix() != null) {
                    sb.append("<div class='code-diff'>");
                    if (f.evidenceMasked() != null) {
                        sb.append("<div class='diff-box vuln'><div class='diff-title vuln'>Vulnerable Code Snippet (Masked)</div><pre class='code-block'>").append(escapeHtml(f.evidenceMasked())).append("</pre></div>");
                    }
                    if (f.suggestedFix() != null) {
                        sb.append("<div class='diff-box fix'><div class='diff-title fix'>Suggested Remediation</div><pre class='code-block'>").append(escapeHtml(f.suggestedFix())).append("</pre></div>");
                    }
                    sb.append("</div>");
                }

                sb.append("</div>");
            }
        }

        sb.append("""
                </div>
            </body>
            </html>
            """);

        return sb.toString();
    }

    public String generateMarkdownReport(AnalysisReportResponse report) {
        StringBuilder sb = new StringBuilder();
        String verdictStr = report.verdict() != null ? report.verdict().name() : "PENDING";
        
        sb.append("# Codexa Production Readiness Report\n\n");
        sb.append("**Scan Target:** `").append(report.scanTarget()).append("`  \n");
        sb.append("**Job ID:** `").append(report.jobId()).append("`  \n");
        sb.append("**Verdict:** **").append(verdictStr).append("**  \n");
        sb.append("**Overall Score:** **").append(report.overallScore() != null ? String.format("%.1f", report.overallScore()) : "N/A").append("/100**  \n");
        sb.append("**Maintainability Score:** **").append(report.metrics() != null ? String.format("%.1f", report.metrics().maintainabilityScore()) : "100.0").append("/100**  \n");
        sb.append("**Architectural Score:** **").append(report.metrics() != null ? String.format("%.1f", report.metrics().architecturalScore()) : "100.0").append("/100**  \n\n");

        sb.append("> **Security Advisory:** ").append(report.disclaimer()).append("\n\n");

        sb.append("## Findings Summary\n\n");
        sb.append("| Rule ID | Severity | Category | File | Line | Title | Priority |\n");
        sb.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");

        for (FindingResponse f : report.findings()) {
            sb.append("| `").append(f.ruleId()).append("` | ")
                    .append(f.severity()).append(" | ")
                    .append(f.category()).append(" | `")
                    .append(f.filePath()).append("` | ")
                    .append(f.startLine()).append(" | ")
                    .append(f.title()).append(" | ")
                    .append(String.format("%.1f", f.priorityScore())).append(" |\n");
        }

        sb.append("\n## Detailed Findings\n\n");
        for (FindingResponse f : report.findings()) {
            sb.append("### ").append(f.ruleId()).append(" - ").append(f.title()).append("\n\n");
            sb.append("- **Severity:** ").append(f.severity()).append("\n");
            sb.append("- **Category:** ").append(f.category()).append("\n");
            sb.append("- **Location:** `").append(f.filePath()).append(":").append(f.startLine()).append("`\n");
            sb.append("- **Priority Score:** ").append(String.format("%.1f", f.priorityScore())).append("\n\n");

            if (f.description() != null && !f.description().isBlank()) {
                sb.append("**Explanation:**\n\n").append(f.description()).append("\n\n");
            }

            if (f.evidenceMasked() != null && !f.evidenceMasked().isBlank()) {
                sb.append("**Vulnerable Snippet:**\n```java\n").append(f.evidenceMasked()).append("\n```\n\n");
            }

            if (f.suggestedFix() != null && !f.suggestedFix().isBlank()) {
                sb.append("**Suggested Fix:**\n```java\n").append(f.suggestedFix()).append("\n```\n\n");
            }

            sb.append("---\n\n");
        }

        return sb.toString();
    }

    private String getScoreColor(Double score) {
        if (score == null) return "";
        if (score >= 80.0) return "green";
        if (score >= 50.0) return "orange";
        return "red";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
