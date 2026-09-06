"use client";
import React, { useState, useEffect } from 'react';
import { 
  X, Share2, Copy, Check, Download, ExternalLink, 
  Printer, FileText, Shield, CheckCircle2, AlertTriangle
} from 'lucide-react';

export default function ExportShareModal({ 
  isOpen, 
  onClose, 
  job, 
  jobId, 
  findings = [], 
  initialFormat = 'pdf' 
}) {
  const [format, setFormat] = useState(initialFormat || 'pdf');
  const [copied, setCopied] = useState(false);
  const [shareSuccess, setShareSuccess] = useState(false);

  // Synchronize format when initialFormat changes
  useEffect(() => {
    if (initialFormat) {
      setFormat(initialFormat);
    }
  }, [initialFormat]);

  if (!isOpen) return null;

  const triggerHaptic = (ms = 12) => {
    try {
      if (typeof window !== 'undefined' && window.navigator?.vibrate) {
        window.navigator.vibrate(ms);
      }
    } catch (_) {}
  };

  const baseUrl = typeof window !== 'undefined' ? window.location.origin : '';
  const exportUrl = `${baseUrl}/api/v1/analyses/${jobId}/export?format=${format === 'pdf' ? 'html' : format}`;
  const reportViewUrl = `${baseUrl}/api/v1/analyses/${jobId}/report?format=${format === 'pdf' ? 'html' : format}&view=true`;
  const reportPrintUrl = `${baseUrl}/api/v1/analyses/${jobId}/report?format=html&view=true&print=true`;

  // Generate clean share text for messaging apps (WhatsApp, Slack, Email)
  const getShareSummary = () => {
    const critCount = findings.filter(f => (f.severity || '').toUpperCase() === 'CRITICAL').length;
    const highCount = findings.filter(f => (f.severity || '').toUpperCase() === 'HIGH').length;
    
    return `🛡️ CODEXA Security & Production Readiness Audit
Target: ${job?.sourceIdentifier || 'Repository'}
Score: ${job?.overallScore ?? 100}/100 (${job?.verdict || 'REVIEW_COMPLETE'})
Maintainability Index: ${job?.metrics?.maintainabilityScore || 100}/100
Architectural Health: ${job?.metrics?.architecturalScore || 100}/100
Findings: ${findings.length} total (${critCount} Critical, ${highCount} High)

Full Audit Report: ${reportViewUrl}`;
  };

  // Generate format-specific raw content for copying
  const getRawContent = () => {
    if (format === 'json') {
      return JSON.stringify({
        jobId,
        target: job?.sourceIdentifier,
        score: job?.overallScore,
        verdict: job?.verdict,
        metrics: job?.metrics,
        findings
      }, null, 2);
    }
    if (format === 'md') {
      let md = `# CODEXA Security & Production Readiness Report\n\n`;
      md += `**Target:** \`${job?.sourceIdentifier || 'Repository'}\`  \n`;
      md += `**Job ID:** \`${jobId}\`  \n`;
      md += `**Score:** **${job?.overallScore ?? 100}/100**  \n`;
      md += `**Verdict:** **${job?.verdict || 'REVIEW_COMPLETE'}**  \n`;
      md += `**Maintainability:** **${job?.metrics?.maintainabilityScore || 100}/100**  \n`;
      md += `**Architectural Health:** **${job?.metrics?.architecturalScore || 100}/100**  \n\n`;
      md += `## Findings (${findings.length})\n\n`;
      findings.forEach(f => {
        md += `### [${f.severity}] ${f.title} (${f.ruleId})\n`;
        md += `- Location: \`${f.filePath}:${f.startLine}\`\n`;
        if (f.description) md += `- Description: ${f.description}\n`;
        if (f.suggestedFix) md += `\n\`\`\`\n${f.suggestedFix}\n\`\`\`\n`;
        md += `\n---\n\n`;
      });
      return md;
    }
    return getShareSummary();
  };

  // Resilient clipboard copy with textarea fallback for Android WebViews
  const copyTextToClipboard = (text) => {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).catch(() => {
        fallbackCopy(text);
      });
      return;
    }
    fallbackCopy(text);
  };

  const fallbackCopy = (text) => {
    try {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.focus();
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
    } catch (e) {
      console.warn('Fallback copy error:', e);
    }
  };

  const handleNativeShare = async () => {
    triggerHaptic(15);
    try {
      if (navigator.share) {
        await navigator.share({
          title: `Codexa Audit: ${job?.sourceIdentifier || 'Repository'}`,
          text: getShareSummary(),
          url: reportViewUrl,
        });
        setShareSuccess(true);
        setTimeout(() => setShareSuccess(false), 2500);
      } else {
        handleCopyContent();
      }
    } catch (err) {
      if (err.name !== 'AbortError') {
        handleCopyContent();
      }
    }
  };

  const handleCopyContent = () => {
    triggerHaptic(15);
    const text = getRawContent();
    copyTextToClipboard(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handleOpenBrowser = () => {
    triggerHaptic(10);
    const targetUrl = format === 'pdf' ? reportPrintUrl : reportViewUrl;
    window.open(targetUrl, '_blank');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200 select-none">
      <div 
        className="w-full sm:max-w-lg bg-white dark:bg-[#0B0F19] rounded-t-3xl sm:rounded-3xl border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden flex flex-col max-h-[90vh] pb-[max(1rem,env(safe-area-inset-bottom))]"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Handle bar on mobile */}
        <div className="w-12 h-1.5 bg-slate-300 dark:bg-slate-700 rounded-full mx-auto mt-3 sm:hidden" />

        {/* Modal Header */}
        <div className="flex items-center justify-between px-5 pt-4 pb-3 border-b border-slate-200 dark:border-slate-800/80">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-xl bg-blue-500/10 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold">
              <Download className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white font-display">
                Export &amp; Share Audit Report
              </h3>
              <p className="text-[11px] text-slate-500 dark:text-slate-400 font-mono truncate max-w-[240px]">
                {job?.sourceIdentifier || 'Codebase Report'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 hover:text-slate-900 dark:hover:text-white transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Format Selector Tabs */}
        <div className="px-5 pt-3 pb-2">
          <div className="grid grid-cols-4 gap-1.5 p-1 bg-slate-100 dark:bg-slate-900/80 rounded-2xl border border-slate-200/80 dark:border-slate-800">
            {[
              { id: 'pdf', label: 'PDF Print', icon: Printer, color: 'text-rose-500' },
              { id: 'md', label: 'Markdown', icon: FileText, color: 'text-violet-500' },
              { id: 'html', label: 'HTML', icon: FileText, color: 'text-blue-500' },
              { id: 'json', label: 'JSON Data', icon: FileText, color: 'text-emerald-500' }
            ].map(tab => {
              const TabIcon = tab.icon;
              const isActive = format === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setFormat(tab.id)}
                  className={`py-2 px-1 rounded-xl text-xs font-semibold flex flex-col items-center justify-center space-y-1 transition-all cursor-pointer ${
                    isActive 
                      ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm border border-slate-200/60 dark:border-slate-700' 
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                  }`}
                >
                  <TabIcon className={`w-4 h-4 ${tab.color}`} />
                  <span className="text-[10px] font-mono leading-tight">{tab.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Format Details Box */}
        <div className="px-5 py-2">
          <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800/80 space-y-1.5 text-left">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-900 dark:text-white font-display">
                {format === 'pdf' && 'High-Fidelity PDF Print Document'}
                {format === 'md' && 'GitHub-Flavored Markdown Report'}
                {format === 'html' && 'Self-Contained Standalone HTML'}
                {format === 'json' && 'Raw Machine-Readable Findings JSON'}
              </span>
              <span className="px-2 py-0.5 rounded-full text-[9px] font-mono font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20">
                Ready
              </span>
            </div>
            <p className="text-[11px] text-slate-600 dark:text-slate-400 leading-relaxed font-sans">
              {format === 'pdf' && 'Opens the dedicated printable audit layout in your browser where you can save as PDF or print directly.'}
              {format === 'md' && 'Complete summary table with rule breakdowns and code diff patches, ready to paste into GitHub PRs.'}
              {format === 'html' && 'Fully styled interactive audit view with dark/light themes and embedded finding diffs.'}
              {format === 'json' && 'Structured JSON payload including all metric indices, line numbers, and OWASP rule mappings.'}
            </p>
          </div>
        </div>

        {/* Action Buttons Grid */}
        <div className="px-5 py-3 space-y-2.5">
          {/* Primary Action 1: Native Mobile Share Sheet */}
          <button
            onClick={handleNativeShare}
            className="w-full py-3 px-4 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold font-display flex items-center justify-center space-x-2 shadow-md shadow-blue-500/20 active:scale-98 transition-all cursor-pointer"
          >
            {shareSuccess ? <Check className="w-4 h-4" /> : <Share2 className="w-4 h-4" />}
            <span>{shareSuccess ? 'Shared Successfully!' : 'Share via Mobile Apps (WhatsApp, Mail...)'}</span>
          </button>

          <div className="grid grid-cols-2 gap-2">
            {/* Copy Content / Summary Button */}
            <button
              onClick={handleCopyContent}
              className="py-2.5 px-3 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs font-semibold flex items-center justify-center space-x-1.5 border border-slate-200 dark:border-slate-700 active:scale-98 transition-all cursor-pointer"
            >
              {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
              <span>{copied ? 'Copied to Clipboard!' : 'Copy to Clipboard'}</span>
            </button>

            {/* Direct Open in Browser / Save PDF */}
            <button
              onClick={handleOpenBrowser}
              className="py-2.5 px-3 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs font-semibold flex items-center justify-center space-x-1.5 border border-slate-200 dark:border-slate-700 active:scale-98 transition-all cursor-pointer"
            >
              {format === 'pdf' ? (
                <Printer className="w-4 h-4 text-rose-500 shrink-0" />
              ) : (
                <ExternalLink className="w-4 h-4 text-blue-500 shrink-0" />
              )}
              <span>{format === 'pdf' ? 'Print / Save PDF' : 'Open in Browser'}</span>
            </button>
          </div>

          {/* Direct File Download Anchor Link (Guaranteed to work across all mobile webviews) */}
          <a
            href={exportUrl}
            target="_blank"
            rel="noopener noreferrer"
            download={`codexa-report-${jobId}.${format === 'markdown' || format === 'md' ? 'md' : format === 'pdf' ? 'html' : format}`}
            onClick={() => triggerHaptic(8)}
            className="w-full py-2 px-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-[11px] font-mono text-slate-600 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 flex items-center justify-center space-x-1.5 transition-colors active:scale-98"
          >
            <Download className="w-3.5 h-3.5 text-blue-500 shrink-0" />
            <span>Direct File Download (.{(format === 'markdown' || format === 'md' ? 'md' : format === 'pdf' ? 'html' : format)})</span>
          </a>
        </div>
      </div>
    </div>
  );
}
