import React, { useEffect, useState, useMemo } from 'react';
import { getAnalysisJob, getFindings } from '../api/client';
import FindingsFilterBar from './FindingsFilterBar';
import FileTreeExplorer from './FileTreeExplorer';
import LiveReviewPulseLoader from './LiveReviewPulseLoader';
import ExpandableFindingCards from './ExpandableFindingCards';
import GlowingEffect from './ui/GlowingEffect';
import { 
  CheckCircle, AlertTriangle, XCircle, Clock, Shield, 
  ArrowLeft, RefreshCw, FileText, ExternalLink, HelpCircle,
  LayoutGrid, ListFilter, FolderTree, Code, Printer, Download
} from 'lucide-react';

export default function AnalysisDetailView({ jobId, onBack }) {
  const [job, setJob] = useState(null);
  const [findings, setFindings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [searchFilter, setSearchFilter] = useState('');
  const [selectedFile, setSelectedFile] = useState('');
  
  // Live animated progress state
  const [liveProgress, setLiveProgress] = useState(25);
  const [liveStage, setLiveStage] = useState('INGESTION');

  // Smooth micro-step progress updater while scanning
  useEffect(() => {
    if (!job || (job.status !== 'COMPLETED' && job.status !== 'FAILED')) {
      const interval = setInterval(() => {
        setLiveProgress(prev => {
          const target = job?.progressPercent || 30;
          if (prev < target) {
            return Math.min(prev + 3, target);
          } else if (prev < 95) {
            return Math.min(prev + 0.3, 96);
          }
          return prev;
        });
      }, 150);
      return () => clearInterval(interval);
    } else if (job.status === 'COMPLETED') {
      setLiveProgress(100);
    }
  }, [job?.status, job?.progressPercent]);

  const fetchJobData = async () => {
    try {
      const data = await getAnalysisJob(jobId);
      setJob(data);

      if (data.progressStage) {
        setLiveStage(data.progressStage);
      }
      if (data.progressPercent) {
        setLiveProgress(prev => Math.max(prev, data.progressPercent));
      }

      if (data.status === 'COMPLETED' || data.status === 'FAILED') {
        const fData = await getFindings(jobId, {
          category: categoryFilter,
          severity: severityFilter,
          search: searchFilter
        });
        const allFindings = fData.content || [];
        setFindings(allFindings);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobData();
    const interval = setInterval(() => {
      if (job && (job.status === 'COMPLETED' || job.status === 'FAILED')) {
        clearInterval(interval);
      } else {
        fetchJobData();
      }
    }, 650);

    return () => clearInterval(interval);
  }, [jobId, job?.status, categoryFilter, severityFilter, searchFilter]);

  // Filter findings based on selected file from FileTreeExplorer
  const filteredFindings = useMemo(() => {
    if (!selectedFile) return findings;
    return findings.filter(f => {
      const cleanPath = f.filePath ? f.filePath.replace(/^[^\/]+\//, '') : '';
      return cleanPath === selectedFile || (f.filePath && f.filePath.endsWith(selectedFile));
    });
  }, [findings, selectedFile]);

  const downloadBlob = (content, filename, contentType) => {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const handleExportJson = () => {
    const exportData = {
      jobId,
      sourceIdentifier: job?.sourceIdentifier,
      sourceType: job?.sourceType,
      status: job?.status,
      overallScore: job?.overallScore,
      verdict: job?.verdict,
      metrics: job?.metrics,
      exportedAt: new Date().toISOString(),
      findings: findings.map(f => ({
        ruleId: f.ruleId,
        category: f.category,
        severity: f.severity,
        confidence: f.confidence,
        title: f.title,
        description: f.description,
        impact: f.impact,
        remediation: f.remediation,
        owaspMapping: f.owaspMapping,
        filePath: f.filePath,
        startLine: f.startLine,
        endLine: f.endLine,
        evidenceMasked: f.evidenceMasked,
        suggestedFix: f.suggestedFix,
        priorityScore: f.priorityScore
      }))
    };
    downloadBlob(JSON.stringify(exportData, null, 2), `codexa-report-${jobId}.json`, 'application/json');
  };

  const handleExportMarkdown = () => {
    let md = `# CODEXA Security & Production Readiness Audit Report\n\n`;
    md += `**Target:** \`${job?.sourceIdentifier || 'Target Repository'}\`  \n`;
    md += `**Job ID:** \`${jobId}\`  \n`;
    md += `**Overall Score:** **${job?.overallScore ?? 100}/100**  \n`;
    md += `**Verdict:** **${job?.verdict || 'REVIEW_COMPLETE'}**  \n`;
    md += `**Maintainability Index:** **${job?.metrics?.maintainabilityScore || 100}/100**  \n`;
    md += `**Total Findings:** **${findings.length}**  \n\n`;
    md += `> **Security Advisory:** Confidential deterministic security report generated by Codexa.\n\n`;
    md += `## Findings Catalog\n\n`;
    md += `| Rule ID | Severity | Category | File | Line | Title | Priority |\n`;
    md += `| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n`;
    findings.forEach(f => {
      md += `| \`${f.ruleId}\` | ${f.severity} | ${f.category} | \`${f.filePath}\` | ${f.startLine} | ${f.title} | ${f.priorityScore} |\n`;
    });
    md += `\n## Detailed Remediations\n\n`;
    findings.forEach(f => {
      md += `### ${f.ruleId}: ${f.title}\n\n`;
      md += `- **Severity:** ${f.severity}\n`;
      md += `- **Category:** ${f.category}\n`;
      md += `- **Location:** \`${f.filePath}:${f.startLine}\`\n`;
      md += `- **OWASP:** ${f.owaspMapping || 'N/A'}\n\n`;
      if (f.description) md += `**Analysis:**\n${f.description}\n\n`;
      if (f.impact) md += `**Impact:**\n${f.impact}\n\n`;
      if (f.evidenceMasked) md += `**Detected Snippet:**\n\`\`\`\n${f.evidenceMasked}\n\`\`\`\n\n`;
      if (f.suggestedFix) md += `**Suggested Fix:**\n\`\`\`\n${f.suggestedFix}\n\`\`\`\n\n`;
      md += `---\n\n`;
    });
    downloadBlob(md, `codexa-report-${jobId}.md`, 'text/markdown');
  };

  const handleExportHtml = () => {
    const html = `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><title>Codexa Audit Report - ${jobId}</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 32px 20px; line-height: 1.5; }
  .container { max-width: 960px; margin: 0 auto; }
  .card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
  .badge { display: inline-block; padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: bold; }
  .badge-crit { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid #ef4444; }
  .badge-high { background: rgba(249, 115, 22, 0.2); color: #fb923c; border: 1px solid #f97316; }
  pre { background: #090d16; border: 1px solid #334155; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 12px; overflow-x: auto; }
</style>
</head>
<body>
<div class="container">
  <h1>Codexa Code Security & Readiness Report</h1>
  <p>Target: <strong>${job?.sourceIdentifier || ''}</strong> &bull; Score: <strong>${job?.overallScore ?? 100}/100</strong> &bull; Verdict: <strong>${job?.verdict || ''}</strong></p>
  <div class="card">
    <h3>Executive Summary</h3>
    <p>Security: ${job?.metrics?.securityScore || 100}/100 &bull; Quality: ${job?.metrics?.qualityScore || 100}/100 &bull; Maintainability: ${job?.metrics?.maintainabilityScore || 100}/100</p>
  </div>
  ${findings.map(f => `
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <h4>${f.title} (${f.ruleId})</h4>
        <span class="badge ${f.severity === 'CRITICAL' ? 'badge-crit' : 'badge-high'}">${f.severity}</span>
      </div>
      <p style="color:#94a3b8;font-size:12px;">${f.filePath}:${f.startLine}</p>
      <p>${f.description || ''}</p>
      ${f.evidenceMasked ? `<pre><code>${f.evidenceMasked.replace(/</g, '&lt;')}</code></pre>` : ''}
      ${f.suggestedFix ? `<pre style="border-color:#22c55e;"><code>${f.suggestedFix.replace(/</g, '&lt;')}</code></pre>` : ''}
    </div>
  `).join('')}
</div>
</body></html>`;
    downloadBlob(html, `codexa-report-${jobId}.html`, 'text/html');
  };

  const handleExportPdf = () => {
    window.print();
  };

  const getVerdictBadge = (verdict) => {
    switch (verdict) {
      case 'REVIEW_COMPLETE':
        return <span className="px-3.5 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full text-xs font-bold shadow-sm shadow-emerald-500/20">Review Complete / Low Risk</span>;
      case 'GENERALLY_PROMISING':
        return <span className="px-3.5 py-1 bg-blue-500/10 text-blue-300 border border-blue-500/30 rounded-full text-xs font-bold shadow-sm shadow-cyan-500/20">Generally Promising</span>;
      case 'NEEDS_URGENT_FIXES':
        return <span className="px-3.5 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/30 rounded-full text-xs font-bold shadow-sm shadow-amber-500/20">Needs Urgent Fixes</span>;
      default:
        return <span className="px-3.5 py-1 bg-rose-500/10 text-rose-400 border border-rose-500/30 rounded-full text-xs font-bold shadow-sm shadow-rose-500/20">Not Ready / High Risk</span>;
    }
  };

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/40 shadow-sm shadow-rose-500/20">CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-orange-500/20 text-orange-300 border border-orange-500/40 shadow-sm shadow-orange-500/20">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40 shadow-sm shadow-amber-500/20">MEDIUM</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-blue-500/20 text-blue-300 border border-blue-500/40">LOW</span>;
    }
  };

  if (loading && !job) {
    return (
      <div className="py-24 text-center space-y-4">
        <RefreshCw className="w-10 h-10 animate-spin text-blue-400 mx-auto" />
        <p className="text-slate-300 text-sm font-medium font-display">Connecting to Codexa Inspection Engine...</p>
      </div>
    );
  }

  const isScanning = !job || (job.status !== 'COMPLETED' && job.status !== 'FAILED');
  const displayJob = {
    ...job,
    sourceIdentifier: job?.sourceIdentifier || 'Target Repository',
    progressStage: job?.progressStage || liveStage,
    progressPercent: job?.status === 'COMPLETED' ? 100 : Math.round(liveProgress)
  };

  return (
    <div className="max-w-7xl mx-auto px-2 sm:px-6 py-4 sm:py-8 space-y-4 sm:space-y-6">
      {/* Top Navigation & Report Exporters */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 sm:gap-4">
        <button
          onClick={onBack}
          className="flex items-center space-x-2 px-3.5 sm:px-4 py-1.5 sm:py-2 rounded-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:text-slate-950 dark:hover:text-white hover:border-slate-300 dark:hover:border-slate-700 transition-all shadow-sm active:scale-95 cursor-pointer font-display"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </button>

        {!isScanning && job?.status === 'COMPLETED' && (
          <div className="grid grid-cols-4 gap-1.5 w-full sm:w-auto sm:flex sm:items-center sm:space-x-2">
            <button
              onClick={handleExportPdf}
              title="Print or Save as PDF"
              className="px-2.5 sm:px-3.5 py-1.5 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 text-[11px] sm:text-xs font-semibold rounded-xl sm:rounded-full flex items-center justify-center space-x-1 sm:space-x-1.5 transition-colors border border-slate-200 dark:border-slate-800 shadow-sm cursor-pointer"
            >
              <Printer className="w-3.5 h-3.5 text-rose-500 shrink-0" />
              <span>PDF</span>
            </button>
            <button
              onClick={handleExportHtml}
              title="Download standalone HTML report"
              className="px-2.5 sm:px-3.5 py-1.5 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 text-[11px] sm:text-xs font-semibold rounded-xl sm:rounded-full flex items-center justify-center space-x-1 sm:space-x-1.5 transition-colors border border-slate-200 dark:border-slate-800 shadow-sm cursor-pointer"
            >
              <FileText className="w-3.5 h-3.5 text-blue-500 shrink-0" />
              <span>HTML</span>
            </button>
            <button
              onClick={handleExportMarkdown}
              title="Download Markdown summary"
              className="px-2.5 sm:px-3.5 py-1.5 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 text-[11px] sm:text-xs font-semibold rounded-xl sm:rounded-full flex items-center justify-center space-x-1 sm:space-x-1.5 transition-colors border border-slate-200 dark:border-slate-800 shadow-sm cursor-pointer"
            >
              <FileText className="w-3.5 h-3.5 text-violet-500 shrink-0" />
              <span>MD</span>
            </button>
            <button
              onClick={handleExportJson}
              title="Download raw JSON findings"
              className="px-2.5 sm:px-3.5 py-1.5 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 text-[11px] sm:text-xs font-semibold rounded-xl sm:rounded-full flex items-center justify-center space-x-1 sm:space-x-1.5 transition-colors border border-slate-200 dark:border-slate-800 shadow-sm cursor-pointer"
            >
              <FileText className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
              <span>JSON</span>
            </button>
          </div>
        )}
      </div>

      {/* When In Scanning Mode -> Display Animated Pulse Loader */}
      {isScanning && (
        <LiveReviewPulseLoader job={displayJob} />
      )}

      {/* Completed Inspection Dashboard */}
      {!isScanning && job?.status === 'COMPLETED' && (
        <>
          {/* Main Scorecard Banner with Glowing Effect Container */}
          <div className="relative rounded-2xl sm:rounded-3xl cdx-glass-card p-4 sm:p-8 space-y-4 sm:space-y-6 shadow-xl overflow-hidden">
            <GlowingEffect
              spread={45}
              glow={true}
              disabled={false}
              proximity={64}
              inactiveZone={0.01}
            />
            <div className="relative z-10 space-y-4 sm:space-y-6">
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 sm:gap-6 pb-4 sm:pb-6 border-b border-[var(--border-subtle)]">
                <div className="space-y-1.5 min-w-0 max-w-full">
                  <div className="flex flex-wrap items-center gap-2">
                    <h1 className="text-sm sm:text-2xl font-bold font-mono text-[var(--text-primary)] tracking-tight break-all max-w-full leading-snug">{job?.sourceIdentifier}</h1>
                    <span className="text-[10px] sm:text-[11px] px-2.5 py-0.5 bg-blue-500/10 text-blue-700 dark:text-blue-300 rounded-full uppercase font-mono font-bold border border-blue-500/30 shrink-0">
                      {job?.sourceType}
                    </span>
                  </div>
                  <p className="text-[11px] sm:text-xs text-[var(--text-muted)] font-mono truncate">Job ID: {jobId} &bull; Total Files: {job?.metrics?.totalFiles || 0}</p>
                </div>

                <div className="flex flex-wrap items-center gap-3">
                  {job?.verdict && getVerdictBadge(job.verdict)}
                  <span className="px-3.5 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-500/30">
                    COMPLETED
                  </span>
                </div>
              </div>

              {/* Score Cards Grid with Maintainability Index */}
              <div className="grid grid-cols-2 lg:grid-cols-5 gap-3 sm:gap-4">
                <div className="p-3.5 sm:p-5 cdx-card rounded-2xl relative overflow-hidden group hover:border-blue-500/40 transition-all">
                  <div className="text-[10px] sm:text-xs font-bold text-[var(--text-muted)] uppercase tracking-wider font-display">Overall Score</div>
                  <div className={`text-2xl sm:text-4xl font-black mt-1 sm:mt-2 font-mono ${
                    (job?.overallScore ?? 100) >= 75 ? 'text-emerald-600 dark:text-emerald-400' :
                    (job?.overallScore ?? 100) >= 50 ? 'text-amber-600 dark:text-amber-400' : 'text-rose-600 dark:text-rose-400'
                  }`}>
                    {job?.overallScore ?? 100}
                    <span className="text-xs sm:text-base text-[var(--text-muted)] font-normal">/100</span>
                  </div>
                  <div className="mt-1 sm:mt-2 text-[10px] sm:text-[11px] text-[var(--text-muted)] font-medium truncate">Readiness Index</div>
                </div>

                <div className="p-3.5 sm:p-5 cdx-card rounded-2xl group hover:border-blue-500/40 transition-all">
                  <div className="text-[10px] sm:text-xs font-bold text-[var(--text-muted)] uppercase tracking-wider font-display">Security (60%)</div>
                  <div className="text-2xl sm:text-3xl font-black text-[var(--text-primary)] mt-1 sm:mt-2 font-mono">
                    {job?.metrics?.securityScore ?? 100}
                    <span className="text-xs sm:text-base text-[var(--text-muted)] font-normal">/100</span>
                  </div>
                  <div className="mt-1 sm:mt-2 flex flex-wrap items-center gap-1 text-[10px] sm:text-[11px] font-mono">
                    <span className="text-rose-600 dark:text-rose-400 font-bold">{job?.metrics?.criticalCount || 0} Crit</span>
                    <span className="text-[var(--text-muted)]">&bull;</span>
                    <span className="text-orange-600 dark:text-orange-400 font-bold">{job?.metrics?.highCount || 0} High</span>
                  </div>
                </div>

                <div className="p-3.5 sm:p-5 cdx-card rounded-2xl group hover:border-blue-500/40 transition-all">
                  <div className="text-[10px] sm:text-xs font-bold text-[var(--text-muted)] uppercase tracking-wider font-display">Quality (25%)</div>
                  <div className="text-2xl sm:text-3xl font-black text-[var(--text-primary)] mt-1 sm:mt-2 font-mono">
                    {job?.metrics?.qualityScore ?? 100}
                    <span className="text-xs sm:text-base text-[var(--text-muted)] font-normal">/100</span>
                  </div>
                  <div className="mt-1 sm:mt-2 text-[10px] sm:text-[11px] text-[var(--text-muted)] truncate">AST Code Smells</div>
                </div>

                <div className="p-3.5 sm:p-5 cdx-card rounded-2xl group hover:border-blue-500/40 transition-all">
                  <div className="text-[10px] sm:text-xs font-bold text-[var(--text-muted)] uppercase tracking-wider font-display">Maintainability</div>
                  <div className="text-2xl sm:text-3xl font-black text-blue-600 dark:text-blue-400 mt-1 sm:mt-2 font-mono">
                    {job?.metrics?.maintainabilityScore ?? (job?.metrics?.qualityScore ?? 100)}
                    <span className="text-xs sm:text-base text-[var(--text-muted)] font-normal">/100</span>
                  </div>
                  <div className="mt-1 sm:mt-2 text-[10px] sm:text-[11px] text-[var(--text-muted)] truncate">Architecture &amp; Smells</div>
                </div>

                <div className="p-3.5 sm:p-5 cdx-card rounded-2xl group hover:border-blue-500/40 transition-all">
                  <div className="text-[10px] sm:text-xs font-bold text-[var(--text-muted)] uppercase tracking-wider font-display">Ops (15%)</div>
                  <div className="text-2xl sm:text-3xl font-black text-[var(--text-primary)] mt-1 sm:mt-2 font-mono">
                    {job?.metrics?.operationsScore ?? 100}
                    <span className="text-xs sm:text-base text-[var(--text-muted)] font-normal">/100</span>
                  </div>
                  <div className="mt-1 sm:mt-2 text-[10px] sm:text-[11px] text-[var(--text-muted)] truncate">Hardening</div>
                </div>
              </div>

              {/* Architecture & Code Generation Recommendations Panel */}
              <div className="p-4 sm:p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Shield className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                    <h3 className="text-xs sm:text-sm font-bold text-[var(--text-primary)] font-display">
                      Security &amp; Architectural Review Recommendations
                    </h3>
                  </div>
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-mono font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20">
                    Maintainability: {job?.metrics?.maintainabilityScore ?? (job?.metrics?.qualityScore ?? 100)}/100
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-1 text-xs">
                  <div className="p-3 rounded-xl cdx-recessed border border-[var(--border-subtle)] space-y-1">
                    <div className="font-bold text-[var(--text-primary)] font-display flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-blue-500" />
                      <span>Parameterization &amp; Sanitization</span>
                    </div>
                    <p className="text-[11px] text-[var(--text-muted)] leading-relaxed font-sans">
                      Replace direct string interpolations in SQL and command execution paths with strongly typed parameterized binders.
                    </p>
                  </div>

                  <div className="p-3 rounded-xl cdx-recessed border border-[var(--border-subtle)] space-y-1">
                    <div className="font-bold text-[var(--text-primary)] font-display flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-emerald-500" />
                      <span>Boundary Exception Handling</span>
                    </div>
                    <p className="text-[11px] text-[var(--text-muted)] leading-relaxed font-sans">
                      Eliminate silent exception suppression (empty catch blocks) to preserve runtime observability and crash diagnostics.
                    </p>
                  </div>

                  <div className="p-3 rounded-xl cdx-recessed border border-[var(--border-subtle)] space-y-1">
                    <div className="font-bold text-[var(--text-primary)] font-display flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-amber-500" />
                      <span>Secret Vault &amp; Config Isolation</span>
                    </div>
                    <p className="text-[11px] text-[var(--text-muted)] leading-relaxed font-sans">
                      Extract API keys, private keys, and passwords out of repository source files into injected runtime environment variables.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Filter Bar with Elevated Stacking Priority */}
          <div className="relative z-30">
            <FindingsFilterBar
              category={categoryFilter} setCategory={setCategoryFilter}
              severity={severityFilter} setSeverity={setSeverityFilter}
              search={searchFilter} setSearch={setSearchFilter}
            />
          </div>

          {/* Workspace Layout: Left (File Explorer Tree) | Right (Expandable Finding Cards) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            {/* Left Column: Repository Folder Structure */}
            <div className="lg:col-span-4 sticky top-6">
              <FileTreeExplorer
                findings={findings}
                selectedFile={selectedFile}
                onSelectFile={setSelectedFile}
              />
            </div>

            {/* Right Column: Aceternity Framer-Motion Expandable Finding Cards */}
            <div className="lg:col-span-8 space-y-4">
              <div className="flex items-center justify-between pb-1">
                <h3 className="text-base font-bold text-slate-900 dark:text-white flex items-center space-x-2 font-display">
                  <span>Detected Findings</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-blue-500/10 text-xs text-blue-600 dark:text-blue-300 font-mono font-bold border border-blue-500/20">
                    {filteredFindings.length}
                  </span>
                </h3>

                {selectedFile && (
                  <span className="text-xs text-slate-500 dark:text-slate-400 font-mono truncate max-w-xs">
                    Filtered: <strong className="text-blue-600 dark:text-blue-300">{selectedFile}</strong>
                  </span>
                )}
              </div>

              {filteredFindings.length === 0 ? (
                <div className="bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800 rounded-3xl p-12 text-center space-y-3 shadow-md">
                  <CheckCircle className="w-12 h-12 text-blue-600 dark:text-blue-400 mx-auto" />
                  <p className="text-slate-900 dark:text-slate-200 font-bold text-base font-display">No issues found matching criteria!</p>
                  <p className="text-xs text-slate-600 dark:text-slate-400 font-sans">All scanned AST rules and heuristics passed for this selection.</p>
                </div>
              ) : (
                <ExpandableFindingCards
                  findings={filteredFindings}
                  getSeverityBadge={getSeverityBadge}
                />
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
