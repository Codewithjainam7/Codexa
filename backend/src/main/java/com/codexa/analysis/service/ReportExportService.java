package com.codexa.analysis.service;

import com.codexa.analysis.model.AnalysisMetricResponse;
import com.codexa.analysis.model.AnalysisReportResponse;
import com.codexa.analysis.model.FindingResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for exporting rich, executive-grade audit reports
 * in HTML (with high-fidelity PDF print support), Markdown, and JSON.
 */
@Service
public class ReportExportService {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public String generateJsonReport(AnalysisReportResponse report) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize report to JSON", e);
        }
    }

    public String generateHtmlReport(AnalysisReportResponse report) {
        StringBuilder sb = new StringBuilder();
        String verdictStr = report.verdict() != null ? report.verdict().name() : "PENDING";
        Double overallScore = report.overallScore() != null ? report.overallScore() : 100.0;
        AnalysisMetricResponse m = report.metrics();

        int critCount = m != null ? m.criticalCount() : 0;
        int highCount = m != null ? m.highCount() : 0;
        int medCount = m != null ? m.mediumCount() : 0;
        int lowCount = m != null ? m.lowCount() : 0;
        int totalFindings = report.findings() != null ? report.findings().size() : 0;

        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Codexa Audit Report - """).append(report.jobId()).append("""
                </title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --bg: #070a13;
                        --card-bg: #0d1322;
                        --card-subtle: #11182c;
                        --border: #1e293b;
                        --border-accent: #334155;
                        --text: #f8fafc;
                        --text-muted: #94a3b8;
                        --text-dim: #64748b;
                        --primary: #3b82f6;
                        --primary-glow: rgba(59, 130, 246, 0.15);
                        --accent-green: #10b981;
                        --accent-red: #f43f5e;
                        --accent-orange: #f97316;
                        --accent-amber: #f59e0b;
                        --accent-sky: #38bdf8;
                    }
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        background: var(--bg);
                        color: var(--text);
                        padding: 32px 20px 60px;
                        line-height: 1.5;
                        -webkit-font-smoothing: antialiased;
                    }
                    .container { max-width: 1060px; margin: 0 auto; }
                    
                    /* Sticky Print & Share Action Header */
                    .action-bar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        background: rgba(13, 19, 34, 0.95);
                        backdrop-filter: blur(12px);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        padding: 12px 18px;
                        margin-bottom: 24px;
                        box-shadow: 0 8px 30px rgba(0,0,0,0.3);
                    }
                    .action-bar-title {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        font-size: 13px;
                        font-weight: 700;
                        color: var(--text);
                    }
                    .btn-group { display: flex; gap: 8px; flex-wrap: wrap; }
                    .btn {
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        padding: 8px 16px;
                        border-radius: 10px;
                        font-size: 12px;
                        font-weight: 700;
                        cursor: pointer;
                        border: 1px solid transparent;
                        transition: all 0.15s ease;
                        text-decoration: none;
                        font-family: inherit;
                    }
                    .btn-primary {
                        background: linear-gradient(135deg, #2563eb, #1d4ed8);
                        color: #ffffff;
                        box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
                    }
                    .btn-primary:hover { background: #1e40af; transform: translateY(-1px); }
                    .btn-secondary {
                        background: var(--card-subtle);
                        color: var(--text);
                        border-color: var(--border);
                    }
                    .btn-secondary:hover { background: #1a233d; border-color: var(--border-accent); }
                    
                    /* Hero Corporate Header */
                    .hero-header {
                        background: radial-gradient(circle at 50% 0%, rgba(37,99,235,0.12) 0%, transparent 70%), var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 20px;
                        padding: 28px;
                        margin-bottom: 24px;
                        position: relative;
                        overflow: hidden;
                    }
                    .hero-header::before {
                        content: '';
                        position: absolute;
                        top: 0; left: 0; right: 0;
                        height: 2px;
                        background: linear-gradient(90deg, #3b82f6, #6366f1, #8b5cf6, transparent);
                    }
                    .brand-row { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: gap; gap: 16px; }
                    .brand-badge {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        background: rgba(59, 130, 246, 0.1);
                        border: 1px solid rgba(59, 130, 246, 0.25);
                        color: var(--primary);
                        font-size: 11px;
                        font-weight: 800;
                        letter-spacing: 0.5px;
                        text-transform: uppercase;
                        padding: 4px 12px;
                        border-radius: 9999px;
                        margin-bottom: 10px;
                    }
                    .brand-title {
                        font-size: 26px;
                        font-weight: 800;
                        letter-spacing: -0.5px;
                        color: #ffffff;
                    }
                    .brand-subtitle {
                        color: var(--text-muted);
                        font-size: 13px;
                        margin-top: 4px;
                    }
                    
                    /* Meta pills */
                    .meta-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 12px;
                        margin-top: 20px;
                        padding-top: 18px;
                        border-top: 1px solid var(--border);
                    }
                    .meta-item { display: flex; flex-direction: column; gap: 3px; }
                    .meta-label { font-size: 11px; text-transform: uppercase; font-weight: 700; color: var(--text-dim); letter-spacing: 0.5px; }
                    .meta-value { font-size: 13px; font-weight: 600; color: var(--text); font-family: 'JetBrains Mono', monospace; word-break: break-all; }
                    
                    /* Disclaimer Banner */
                    .disclaimer-card {
                        background: rgba(245, 158, 11, 0.08);
                        border: 1px solid rgba(245, 158, 11, 0.25);
                        border-left: 4px solid var(--accent-amber);
                        border-radius: 12px;
                        padding: 14px 18px;
                        margin-bottom: 24px;
                        font-size: 12.5px;
                        color: #fde68a;
                        display: flex;
                        gap: 10px;
                        align-items: center;
                    }
                    
                    /* Executive Dashboard Grid */
                    .score-grid {
                        display: grid;
                        grid-template-columns: 1.4fr 1fr 1fr 1fr 1fr;
                        gap: 14px;
                        margin-bottom: 24px;
                    }
                    @media (max-width: 900px) {
                        .score-grid { grid-template-columns: repeat(2, 1fr); }
                        .score-card-hero { grid-column: span 2; }
                    }
                    .score-card {
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        padding: 18px;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        position: relative;
                        overflow: hidden;
                    }
                    .score-card-hero {
                        background: linear-gradient(145deg, #0f182e, #0a0f1d);
                        border-color: rgba(59, 130, 246, 0.3);
                        box-shadow: 0 8px 30px rgba(0,0,0,0.25);
                    }
                    .score-card-label {
                        font-size: 11px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        color: var(--text-muted);
                    }
                    .score-card-hero .score-card-label { color: var(--primary); }
                    .score-value {
                        font-size: 34px;
                        font-weight: 800;
                        font-family: 'JetBrains Mono', monospace;
                        margin: 8px 0;
                        line-height: 1;
                    }
                    .score-value.green { color: var(--accent-green); }
                    .score-value.amber { color: var(--accent-amber); }
                    .score-value.red { color: var(--accent-red); }
                    .score-sub { font-size: 11px; color: var(--text-dim); font-weight: 600; }
                    
                    /* Verdict Badge */
                    .verdict-pill {
                        display: inline-block;
                        padding: 4px 10px;
                        border-radius: 9999px;
                        font-size: 11px;
                        font-weight: 800;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-top: 8px;
                    }
                    .verdict-complete { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }
                    .verdict-promising { background: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); }
                    .verdict-warning { background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.3); }
                    .verdict-danger { background: rgba(244, 63, 94, 0.15); color: #fb7185; border: 1px solid rgba(244, 63, 94, 0.3); }
                    
                    /* Severity Distribution Bar */
                    .triage-section {
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        padding: 20px;
                        margin-bottom: 28px;
                    }
                    .triage-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 14px;
                    }
                    .triage-title { font-size: 13px; font-weight: 700; color: var(--text); text-transform: uppercase; letter-spacing: 0.5px; }
                    .triage-bar {
                        display: flex;
                        height: 12px;
                        border-radius: 9999px;
                        overflow: hidden;
                        background: #1e293b;
                        gap: 2px;
                        margin-bottom: 14px;
                    }
                    .triage-segment { height: 100%; }
                    .triage-legend {
                        display: flex;
                        gap: 16px;
                        flex-wrap: wrap;
                        font-size: 12px;
                    }
                    .legend-item { display: flex; align-items: center; gap: 6px; font-weight: 600; }
                    .dot { width: 8px; height: 8px; border-radius: 9999px; }
                    .bg-crit { background: var(--accent-red); }
                    .bg-high { background: var(--accent-orange); }
                    .bg-med { background: var(--accent-amber); }
                    .bg-low { background: var(--accent-sky); }
                    
                    /* Findings Section */
                    .section-title {
                        font-size: 18px;
                        font-weight: 800;
                        margin: 32px 0 16px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                    }
                    .findings-count-badge {
                        font-size: 12px;
                        background: rgba(59, 130, 246, 0.15);
                        color: var(--primary);
                        border: 1px solid rgba(59, 130, 246, 0.3);
                        padding: 3px 10px;
                        border-radius: 9999px;
                        font-family: 'JetBrains Mono', monospace;
                        font-weight: 700;
                    }
                    
                    .finding-card {
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        padding: 22px;
                        margin-bottom: 18px;
                        transition: border-color 0.2s;
                        position: relative;
                    }
                    .finding-card.border-crit { border-left: 4px solid var(--accent-red); }
                    .finding-card.border-high { border-left: 4px solid var(--accent-orange); }
                    .finding-card.border-med { border-left: 4px solid var(--accent-amber); }
                    .finding-card.border-low { border-left: 4px solid var(--accent-sky); }
                    
                    .finding-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-start;
                        gap: 12px;
                        margin-bottom: 12px;
                    }
                    .finding-title-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
                    .finding-title { font-size: 15px; font-weight: 700; color: #ffffff; }
                    .rule-id {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 11px;
                        font-weight: 700;
                        background: var(--card-subtle);
                        border: 1px solid var(--border);
                        padding: 3px 8px;
                        border-radius: 6px;
                        color: var(--primary);
                    }
                    
                    .badge {
                        padding: 3px 10px;
                        border-radius: 6px;
                        font-size: 11px;
                        font-weight: 800;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        font-family: 'JetBrains Mono', monospace;
                    }
                    .badge-critical { background: rgba(244, 63, 94, 0.15); color: #fb7185; border: 1px solid rgba(244, 63, 94, 0.3); }
                    .badge-high { background: rgba(249, 115, 22, 0.15); color: #fb923c; border: 1px solid rgba(249, 115, 22, 0.3); }
                    .badge-medium { background: rgba(245, 158, 11, 0.15); color: #fde047; border: 1px solid rgba(245, 158, 11, 0.3); }
                    .badge-low { background: rgba(56, 189, 248, 0.15); color: #7dd3fc; border: 1px solid rgba(56, 189, 248, 0.3); }
                    
                    .finding-meta {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        font-size: 12px;
                        color: var(--text-muted);
                        margin-bottom: 14px;
                        flex-wrap: wrap;
                    }
                    .file-pill {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 11.5px;
                        background: #040812;
                        border: 1px solid var(--border);
                        padding: 2px 8px;
                        border-radius: 6px;
                        color: #cbd5e1;
                    }
                    .finding-desc {
                        font-size: 13.5px;
                        color: #cbd5e1;
                        line-height: 1.6;
                        margin-bottom: 16px;
                    }
                    
                    /* Code Remediation Diff */
                    .diff-container {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 12px;
                        margin-top: 14px;
                    }
                    @media (max-width: 768px) { .diff-container { grid-template-columns: 1fr; } }
                    
                    .diff-box {
                        background: #060a14;
                        border: 1px solid var(--border);
                        border-radius: 10px;
                        overflow: hidden;
                    }
                    .diff-box.vuln { border-top: 3px solid var(--accent-red); }
                    .diff-box.fix { border-top: 3px solid var(--accent-green); }
                    
                    .diff-bar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 8px 12px;
                        background: rgba(255,255,255,0.02);
                        border-bottom: 1px solid var(--border);
                        font-size: 11px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }
                    .diff-bar.vuln { color: #f87171; }
                    .diff-bar.fix { color: #4ade80; }
                    
                    .code-snippet {
                        margin: 0;
                        padding: 12px 14px;
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 12px;
                        line-height: 1.55;
                        overflow-x: auto;
                        white-space: pre-wrap;
                        color: #e2e8f0;
                    }
                    
                    .empty-notice {
                        padding: 40px;
                        text-align: center;
                        background: var(--card-bg);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        color: var(--accent-green);
                        font-weight: 700;
                    }
                    
                    /* Clean PDF Print Stylesheet */
                    @media print {
                        body {
                            background: #ffffff !important;
                            color: #0f172a !important;
                            padding: 0 !important;
                            font-size: 12pt;
                        }
                        .no-print { display: none !important; }
                        .container { max-width: 100% !important; margin: 0 !important; padding: 12mm 15mm !important; }
                        .hero-header {
                            background: #f8fafc !important;
                            border: 1px solid #cbd5e1 !important;
                            color: #0f172a !important;
                            page-break-inside: avoid;
                        }
                        .brand-title { color: #0f172a !important; }
                        .score-card, .finding-card, .triage-section {
                            background: #ffffff !important;
                            border: 1px solid #cbd5e1 !important;
                            color: #0f172a !important;
                            page-break-inside: avoid;
                            box-shadow: none !important;
                        }
                        .score-card-hero { background: #f8fafc !important; }
                        .score-card-label { color: #64748b !important; }
                        .diff-box { background: #f8fafc !important; border-color: #cbd5e1 !important; }
                        .code-snippet { color: #0f172a !important; background: #f8fafc !important; }
                        .file-pill { background: #f1f5f9 !important; color: #0f172a !important; border-color: #cbd5e1 !important; }
                        .disclaimer-card { background: #fffbeb !important; border-color: #f59e0b !important; color: #92400e !important; }
                        .finding-title { color: #0f172a !important; }
                        .finding-desc { color: #334155 !important; }
                        * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <!-- Action Bar (Hidden on PDF Print) -->
                    <div class="action-bar no-print">
                        <div class="action-bar-title">
                            <span style="color: var(--primary);">🛡️</span>
                            <span>Codexa Audit Report</span>
                        </div>
                        <div class="btn-group">
                            <button class="btn btn-secondary" onclick="if(navigator.share){navigator.share({title:document.title,url:window.location.href});}else{navigator.clipboard.writeText(window.location.href);alert('Report URL copied!');}">
                                📤 Share Report
                            </button>
                            <button class="btn btn-primary" onclick="window.print()">
                                🖨️ Print or Save as PDF
                            </button>
                        </div>
                    </div>
                    <script>
                        if (window.location.search.indexOf('print=true') !== -1) {
                            window.addEventListener('load', function() { setTimeout(function() { window.print(); }, 400); });
                        }
                    </script>

                    <!-- Corporate Hero Banner -->
                    <div class="hero-header">
                        <div class="brand-row">
                            <div>
                                <div class="brand-badge">CODEXA PLATFORM AUDIT</div>
                                <h1 class="brand-title">Enterprise Code Security &amp; Production Readiness Audit</h1>
                                <p class="brand-subtitle">Automated AST Security Inspection &bull; OWASP Top 10 &bull; AI Remediation Engine</p>
                            </div>
                            <div>
                                <span class="verdict-pill """).append(getVerdictClass(verdictStr)).append("""
                                ">""").append(escapeHtml(verdictStr)).append("""
                                </span>
                            </div>
                        </div>

                        <div class="meta-grid">
                            <div class="meta-item">
                                <span class="meta-label">Target Repository</span>
                                <span class="meta-value">""").append(escapeHtml(report.scanTarget())).append("""
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Audit Job ID</span>
                                <span class="meta-value">""").append(report.jobId()).append("""
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Inspection Timestamp</span>
                                <span class="meta-value">""").append(Instant.now().toString()).append("""
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Source Format</span>
                                <span class="meta-value">""").append(report.sourceType() != null ? report.sourceType().name() : "SOURCE").append("""
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Security Advisory Disclaimer -->
                    <div class="disclaimer-card">
                        <span>⚠️</span>
                        <div><strong>Security Notice:</strong> """).append(escapeHtml(report.disclaimer())).append("""
                        </div>
                    </div>

                    <!-- 5-Metric Production Readiness Scorecard -->
                    <div class="score-grid">
                        <div class="score-card score-card-hero">
                            <div class="score-card-label">Overall Readiness</div>
                            <div class="score-value """).append(getScoreColor(overallScore)).append("""
                            ">""").append(String.format("%.1f", overallScore)).append("""
                            <span style="font-size: 16px; color: var(--text-dim);">/100</span></div>
                            <div class="score-sub">Production Index</div>
                        </div>

                        <div class="score-card">
                            <div class="score-card-label">Security Defense</div>
                            <div class="score-value """).append(m != null ? getScoreColor(m.securityScore()) : "green").append("""
                            ">""").append(m != null ? String.format("%.1f", m.securityScore()) : "100.0").append("""
                            </div>
                            <div class="score-sub">60% Weighting</div>
                        </div>

                        <div class="score-card">
                            <div class="score-card-label">Code Quality</div>
                            <div class="score-value """).append(m != null ? getScoreColor(m.qualityScore()) : "green").append("""
                            ">""").append(m != null ? String.format("%.1f", m.qualityScore()) : "100.0").append("""
                            </div>
                            <div class="score-sub">AST Cyclomatic</div>
                        </div>

                        <div class="score-card">
                            <div class="score-card-label">Maintainability Index</div>
                            <div class="score-value """).append(m != null ? getScoreColor(m.maintainabilityScore()) : "green").append("""
                            ">""").append(m != null ? String.format("%.1f", m.maintainabilityScore()) : "100.0").append("""
                            </div>
                            <div class="score-sub">Technical Debt</div>
                        </div>

                        <div class="score-card">
                            <div class="score-card-label">Architectural Health</div>
                            <div class="score-value """).append(m != null ? getScoreColor(m.architecturalScore()) : "green").append("""
                            ">""").append(m != null ? String.format("%.1f", m.architecturalScore()) : "100.0").append("""
                            </div>
                            <div class="score-sub">Modularity Rating</div>
                        </div>
                    </div>

                    <!-- Severity Distribution Triage Bar -->
                    <div class="triage-section">
                        <div class="triage-header">
                            <span class="triage-title">Findings Severity Distribution</span>
                            <span style="font-size: 12px; font-weight: 700; color: var(--text-muted);">""").append(totalFindings).append("""
                             Total Vulnerabilities</span>
                        </div>
                        <div class="triage-bar">
                            <div class="triage-segment bg-crit" style="width: """).append(getPercentage(critCount, totalFindings)).append("""
                            %;"></div>
                            <div class="triage-segment bg-high" style="width: """).append(getPercentage(highCount, totalFindings)).append("""
                            %;"></div>
                            <div class="triage-segment bg-med" style="width: """).append(getPercentage(medCount, totalFindings)).append("""
                            %;"></div>
                            <div class="triage-segment bg-low" style="width: """).append(getPercentage(lowCount, totalFindings)).append("""
                            %;"></div>
                        </div>
                        <div class="triage-legend">
                            <div class="legend-item"><span class="dot bg-crit"></span><span>Critical: </span><strong>""").append(critCount).append("""
                            </strong></div>
                            <div class="legend-item"><span class="dot bg-high"></span><span>High: </span><strong>""").append(highCount).append("""
                            </strong></div>
                            <div class="legend-item"><span class="dot bg-med"></span><span>Medium: </span><strong>""").append(medCount).append("""
                            </strong></div>
                            <div class="legend-item"><span class="dot bg-low"></span><span>Low: </span><strong>""").append(lowCount).append("""
                            </strong></div>
                        </div>
                    </div>

                    <!-- Findings Section -->
                    <div class="section-title">
                        <span>Detailed Findings &amp; AI Remediation Patches</span>
                        <span class="findings-count-badge">""").append(totalFindings).append("""
                         Findings</span>
                    </div>
            """);

        if (report.findings() == null || report.findings().isEmpty()) {
            sb.append("""
                <div class="empty-notice">
                    <p>✅ Zero static security vulnerabilities or architecture smells detected. Codebase is clean and production-ready!</p>
                </div>
            """);
        } else {
            for (FindingResponse f : report.findings()) {
                String sev = f.severity() != null ? f.severity().name().toUpperCase() : "LOW";
                String borderClass = "border-" + sev.toLowerCase();
                String badgeClass = "badge-" + sev.toLowerCase();

                sb.append("""
                    <div class="finding-card """).append(borderClass).append("""
                    ">
                        <div class="finding-header">
                            <div class="finding-title-row">
                                <span class="badge """).append(badgeClass).append("""
                                ">""").append(sev).append("""
                                </span>
                                <span class="rule-id">""").append(escapeHtml(f.ruleId())).append("""
                                </span>
                                <h3 class="finding-title">""").append(escapeHtml(f.title())).append("""
                                </h3>
                            </div>
                            <span style="font-size: 11px; font-weight: 700; color: var(--text-dim); font-family: 'JetBrains Mono', monospace;">
                                Weight: """).append(String.format("%.1f", f.priorityScore())).append("""
                            </span>
                        </div>

                        <div class="finding-meta">
                            <span>Location: <strong class="file-pill">""").append(escapeHtml(f.filePath())).append(":").append(f.startLine()).append("""
                            </strong></span>
                            <span>&bull;</span>
                            <span>OWASP: <strong style="color: var(--primary);">""").append(escapeHtml(f.owaspMapping() != null ? f.owaspMapping() : "N/A")).append("""
                            </strong></span>
                            <span>&bull;</span>
                            <span>Category: <strong>""").append(f.category() != null ? f.category().name() : "SECURITY").append("""
                            </strong></span>
                        </div>
                """);

                if (f.description() != null && !f.description().isBlank()) {
                    sb.append("<p class=\"finding-desc\">").append(escapeHtml(f.description())).append("</p>");
                }

                if (f.evidenceMasked() != null || f.suggestedFix() != null) {
                    sb.append("<div class=\"diff-container\">");
                    if (f.evidenceMasked() != null && !f.evidenceMasked().isBlank()) {
                        sb.append("""
                            <div class="diff-box vuln">
                                <div class="diff-bar vuln">❌ Vulnerable Code Detected (Masked)</div>
                                <pre class="code-snippet"><code>""").append(escapeHtml(f.evidenceMasked())).append("""
                                </code></pre>
                            </div>
                        """);
                    }
                    if (f.suggestedFix() != null && !f.suggestedFix().isBlank()) {
                        sb.append("""
                            <div class="diff-box fix">
                                <div class="diff-bar fix">✅ Recommended Remediation Patch</div>
                                <pre class="code-snippet"><code>""").append(escapeHtml(f.suggestedFix())).append("""
                                </code></pre>
                            </div>
                        """);
                    }
                    sb.append("</div>");
                }

                sb.append("</div>");
            }
        }

        sb.append("""
                    <div style="text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid var(--border); font-size: 11.5px; color: var(--text-dim);">
                        CODEXA Deterministic AST &bull; AI Code Review Platform &bull; Confidential &bull; Generated on """).append(Instant.now().toString()).append("""
                    </div>
                </div>
            </body>
            </html>
            """);

        return sb.toString();
    }

    public String generateMarkdownReport(AnalysisReportResponse report) {
        StringBuilder sb = new StringBuilder();
        String verdictStr = report.verdict() != null ? report.verdict().name() : "PENDING";
        Double overallScore = report.overallScore() != null ? report.overallScore() : 100.0;
        AnalysisMetricResponse m = report.metrics();

        sb.append("# Codexa Production Readiness Report\n\n");
        sb.append("> **🛡️ Deterministic AST Security & AI Code Review Audit**  \n");
        sb.append("> *Confidential Security Assessment generated by the Codexa Platform.*\n\n");

        sb.append("## 📋 Overview & Metadata\n\n");
        sb.append("| Attribute | Value |\n");
        sb.append("| :--- | :--- |\n");
        sb.append("| **Scan Target** | `").append(report.scanTarget()).append("` |\n");
        sb.append("| **Job ID** | `").append(report.jobId()).append("` |\n");
        sb.append("| **Overall Verdict** | **`").append(verdictStr).append("`** |\n");
        sb.append("| **Overall Score** | **`").append(String.format("%.1f", overallScore)).append(" / 100`** |\n");
        sb.append("| **Maintainability Score:** | **`").append(m != null ? String.format("%.1f", m.maintainabilityScore()) : "100.0").append(" / 100`** |\n");
        sb.append("| **Architectural Score:** | **`").append(m != null ? String.format("%.1f", m.architecturalScore()) : "100.0").append(" / 100`** |\n");
        sb.append("| **Audit Timestamp** | `").append(Instant.now().toString()).append("` |\n\n");

        sb.append("> **Security Advisory:** ").append(report.disclaimer()).append("\n\n");

        if (m != null) {
            sb.append("## 📊 Executive Scorecard Breakdown\n\n");
            sb.append("| Dimension | Score | Status | Benchmark |\n");
            sb.append("| :--- | :---: | :---: | :--- |\n");
            sb.append("| 🔒 **Security Readiness** | **").append(String.format("%.1f", m.securityScore())).append("/100** | ")
                    .append(m.securityScore() >= 80 ? "🟢 PASSED" : (m.securityScore() >= 50 ? "🟡 WARNING" : "🔴 FAILED"))
                    .append(" | `>= 80.0` |\n");
            sb.append("| 🧹 **Code Quality** | **").append(String.format("%.1f", m.qualityScore())).append("/100** | ")
                    .append(m.qualityScore() >= 80 ? "🟢 PASSED" : (m.qualityScore() >= 50 ? "🟡 WARNING" : "🔴 FAILED"))
                    .append(" | `>= 80.0` |\n");
            sb.append("| ⚙️ **Operational Reliability** | **").append(String.format("%.1f", m.operationsScore())).append("/100** | ")
                    .append(m.operationsScore() >= 80 ? "🟢 PASSED" : (m.operationsScore() >= 50 ? "🟡 WARNING" : "🔴 FAILED"))
                    .append(" | `>= 80.0` |\n");
            sb.append("| 📐 **Maintainability Index** | **").append(String.format("%.1f", m.maintainabilityScore())).append("/100** | ")
                    .append(m.maintainabilityScore() >= 75 ? "🟢 PASSED" : (m.maintainabilityScore() >= 50 ? "🟡 WARNING" : "🔴 FAILED"))
                    .append(" | `>= 75.0` |\n");
            sb.append("| 🏗️ **Architectural Health** | **").append(String.format("%.1f", m.architecturalScore())).append("/100** | ")
                    .append(m.architecturalScore() >= 75 ? "🟢 PASSED" : (m.architecturalScore() >= 50 ? "🟡 WARNING" : "🔴 FAILED"))
                    .append(" | `>= 75.0` |\n\n");

            sb.append("### 🚨 Findings Triage Distribution\n\n");
            sb.append("| 🔴 Critical | 🟠 High | 🟡 Medium | 🔵 Low | Total Findings | Scan Duration |\n");
            sb.append("| :---: | :---: | :---: | :---: | :---: | :---: |\n");
            sb.append("| **").append(m.criticalCount()).append("** | **")
                    .append(m.highCount()).append("** | **")
                    .append(m.mediumCount()).append("** | **")
                    .append(m.lowCount()).append("** | **")
                    .append(report.findings().size()).append("** | `")
                    .append(m.durationMs()).append(" ms` |\n\n");
        }

        sb.append("## 🔍 Findings Catalog\n\n");
        sb.append("| Rule ID | Severity | Category | File | Line | Title | Priority |\n");
        sb.append("| :--- | :---: | :--- | :--- | :---: | :--- | :---: |\n");

        for (FindingResponse f : report.findings()) {
            sb.append("| `").append(f.ruleId()).append("` | ")
                    .append(f.severity()).append(" | ")
                    .append(f.category()).append(" | `")
                    .append(f.filePath()).append("` | `")
                    .append(f.startLine()).append("` | ")
                    .append(f.title()).append(" | `")
                    .append(String.format("%.1f", f.priorityScore())).append("` |\n");
        }

        sb.append("\n## 🛠️ Detailed Remediations & Patches\n\n");
        if (report.findings().isEmpty()) {
            sb.append("✅ *No security vulnerabilities or architectural defects identified.*\n\n");
        } else {
            for (FindingResponse f : report.findings()) {
                sb.append("### [").append(f.severity()).append("] ").append(f.ruleId()).append(" - ").append(f.title()).append("\n\n");
                sb.append("- **Severity:** `").append(f.severity()).append("`\n");
                sb.append("- **Category:** `").append(f.category()).append("`\n");
                sb.append("- **OWASP Mapping:** `").append(f.owaspMapping() != null ? f.owaspMapping() : "N/A").append("`\n");
                sb.append("- **File Location:** `").append(f.filePath()).append(":").append(f.startLine()).append("`\n");
                sb.append("- **Priority Score:** `").append(String.format("%.1f", f.priorityScore())).append("`\n\n");

                if (f.description() != null && !f.description().isBlank()) {
                    sb.append("**Vulnerability Analysis:**  \n").append(f.description()).append("\n\n");
                }

                if (f.evidenceMasked() != null && !f.evidenceMasked().isBlank()) {
                    sb.append("#### ❌ Vulnerable Snippet:\n```java\n").append(f.evidenceMasked()).append("\n```\n\n");
                }

                if (f.suggestedFix() != null && !f.suggestedFix().isBlank()) {
                    sb.append("#### ✅ Suggested Fix:\n```java\n").append(f.suggestedFix()).append("\n```\n\n");
                }

                sb.append("---\n\n");
            }
        }

        sb.append("\n---\n*Report generated by **CODEXA Platform** — Zero-Bytecode Static AST Code Review Engine.*  \n");
        return sb.toString();
    }

    private String getScoreColor(Double score) {
        if (score == null) return "green";
        if (score >= 80.0) return "green";
        if (score >= 50.0) return "amber";
        return "red";
    }

    private String getVerdictClass(String verdict) {
        if (verdict == null) return "verdict-promising";
        return switch (verdict.toUpperCase()) {
            case "REVIEW_COMPLETE" -> "verdict-complete";
            case "GENERALLY_PROMISING" -> "verdict-promising";
            case "NEEDS_URGENT_FIXES" -> "verdict-warning";
            default -> "verdict-danger";
        };
    }

    private String getPercentage(int count, int total) {
        if (total == 0) return "0";
        double pct = ((double) count / total) * 100.0;
        return String.format("%.1f", pct);
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
