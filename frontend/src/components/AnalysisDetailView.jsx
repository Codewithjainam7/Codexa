import React, { useEffect, useState } from 'react';
import { getAnalysisJob, getFindings } from '../api/client';
import CodeDiffViewer from './CodeDiffViewer';
import FindingsFilterBar from './FindingsFilterBar';
import { CheckCircle, AlertTriangle, XCircle, Clock, Shield, ArrowLeft, RefreshCw, FileText, ExternalLink, HelpCircle } from 'lucide-react';

export default function AnalysisDetailView({ jobId, onBack }) {
  const [job, setJob] = useState(null);
  const [findings, setFindings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [searchFilter, setSearchFilter] = useState('');

  const fetchJobData = async () => {
    try {
      const data = await getAnalysisJob(jobId);
      setJob(data);

      if (data.status === 'COMPLETED' || data.status === 'FAILED') {
        const fData = await getFindings(jobId, {
          category: categoryFilter,
          severity: severityFilter,
          search: searchFilter
        });
        setFindings(fData.content || []);
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
    }, 2000);

    return () => clearInterval(interval);
  }, [jobId, job?.status, categoryFilter, severityFilter, searchFilter]);

  const getVerdictBadge = (verdict) => {
    switch (verdict) {
      case 'REVIEW_COMPLETE':
        return <span className="px-3 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full text-xs font-bold">Review Complete / Low Risk</span>;
      case 'GENERALLY_PROMISING':
        return <span className="px-3 py-1 bg-teal-500/10 text-teal-400 border border-teal-500/30 rounded-full text-xs font-bold">Generally Promising</span>;
      case 'NEEDS_URGENT_FIXES':
        return <span className="px-3 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/30 rounded-full text-xs font-bold">Needs Urgent Fixes</span>;
      default:
        return <span className="px-3 py-1 bg-rose-500/10 text-rose-400 border border-rose-500/30 rounded-full text-xs font-bold">Not Ready</span>;
    }
  };

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2.5 py-0.5 rounded text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2.5 py-0.5 rounded text-[11px] font-bold bg-orange-500/15 text-orange-400 border border-orange-500/30">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 rounded text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">MEDIUM</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded text-[11px] font-bold bg-blue-500/15 text-blue-400 border border-blue-500/30">LOW</span>;
    }
  };

  if (loading && !job) {
    return (
      <div className="py-20 text-center space-y-4">
        <RefreshCw className="w-8 h-8 animate-spin text-emerald-400 mx-auto" />
        <p className="text-slate-400 text-sm">Initializing analysis pipeline...</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-8 space-y-8">
      {/* Top Navigation */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="flex items-center space-x-2 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Overview</span>
        </button>
        <div className="flex items-center space-x-2">
          <a
            href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=html`}
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium rounded-lg flex items-center space-x-1.5 transition-colors border border-slate-700"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Export HTML</span>
          </a>
          <a
            href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=markdown`}
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium rounded-lg flex items-center space-x-1.5 transition-colors border border-slate-700"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Export Markdown</span>
          </a>
          <a
            href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=json`}
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium rounded-lg flex items-center space-x-1.5 transition-colors border border-slate-700"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Export JSON</span>
          </a>
        </div>
      </div>

      {/* Main Scorecard Header */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-800">
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-xl font-bold text-white">{job?.sourceIdentifier}</h2>
              <span className="text-xs px-2 py-0.5 bg-slate-800 text-slate-300 rounded uppercase font-semibold">
                {job?.sourceType}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-1 font-mono">Job ID: {jobId}</p>
          </div>

          <div className="flex items-center space-x-3">
            {job?.verdict && getVerdictBadge(job.verdict)}
            <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${
              job?.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30' :
              job?.status === 'FAILED' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/30' :
              'bg-blue-500/10 text-blue-400 border border-blue-500/30 animate-pulse'
            }`}>
              {job?.status}
            </span>
          </div>
        </div>

        {/* Progress bar during running */}
        {job?.status !== 'COMPLETED' && job?.status !== 'FAILED' && (
          <div className="space-y-2">
            <div className="flex justify-between text-xs font-semibold">
              <span className="text-slate-300">Stage: {job?.progressStage}</span>
              <span className="text-emerald-400">{job?.progressPercent}%</span>
            </div>
            <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
              <div
                className="bg-gradient-to-r from-emerald-500 to-teal-400 h-full transition-all duration-500"
                style={{ width: `${job?.progressPercent || 10}%` }}
              />
            </div>
          </div>
        )}

        {/* Scores Breakdown */}
        {job?.status === 'COMPLETED' && (
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 pt-2">
            <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
              <div className="text-xs font-medium text-slate-400 uppercase tracking-wider">Overall Score</div>
              <div className={`text-3xl font-black mt-1 ${
                (job?.overallScore ?? 100) >= 75 ? 'text-emerald-400' :
                (job?.overallScore ?? 100) >= 50 ? 'text-amber-400' : 'text-rose-400'
              }`}>
                {job?.overallScore ?? 100}/100
              </div>
            </div>
            <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
              <div className="text-xs font-medium text-slate-400 uppercase tracking-wider">Security (60%)</div>
              <div className="text-2xl font-bold text-slate-200 mt-1">{job?.metrics?.securityScore ?? 100}/100</div>
            </div>
            <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
              <div className="text-xs font-medium text-slate-400 uppercase tracking-wider">Quality (25%)</div>
              <div className="text-2xl font-bold text-slate-200 mt-1">{job?.metrics?.qualityScore ?? 100}/100</div>
            </div>
            <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
              <div className="text-xs font-medium text-slate-400 uppercase tracking-wider">Operations (15%)</div>
              <div className="text-2xl font-bold text-slate-200 mt-1">{job?.metrics?.operationsScore ?? 100}/100</div>
            </div>
          </div>
        )}
      </div>

      {/* Filterable Findings Panel */}
      {job?.status === 'COMPLETED' && (
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <h3 className="text-lg font-bold text-white">
              Detected Findings ({findings.length})
            </h3>
          </div>

          <FindingsFilterBar
            category={categoryFilter} setCategory={setCategoryFilter}
            severity={severityFilter} setSeverity={setSeverityFilter}
            search={searchFilter} setSearch={setSearchFilter}
          />

          {findings.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-10 text-center space-y-2">
              <CheckCircle className="w-10 h-10 text-emerald-400 mx-auto" />
              <p className="text-slate-200 font-semibold">No issues matched the active filter criteria.</p>
              <p className="text-xs text-slate-500">All scanned AST rules reported clean results.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {findings.map((f) => (
                <div key={f.id} className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 space-y-4 transition-all hover:border-slate-700">
                  {/* Finding Title Row */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                        {f.ruleId}
                      </span>
                      <h4 className="text-sm font-bold text-white">{f.title}</h4>
                    </div>

                    <div className="flex items-center space-x-2">
                      {f.requiresManualReview && (
                        <span className="px-2 py-0.5 text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 rounded">
                          Review Required
                        </span>
                      )}
                      <span className="text-[11px] font-mono text-slate-400">
                        Priority: {f.priorityScore}
                      </span>
                      {getSeverityBadge(f.severity)}
                    </div>
                  </div>

                  {/* File & OWASP Meta */}
                  <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400">
                    <span className="font-mono text-slate-300">
                      📄 {f.filePath}:{f.startLine}
                    </span>
                    {f.owaspMapping && (
                      <span className="text-emerald-400/90 font-medium">
                        🛡️ {f.owaspMapping}
                      </span>
                    )}
                  </div>

                  {/* Explanation & Impact */}
                  <p className="text-xs text-slate-300 leading-relaxed">{f.description}</p>
                  {f.impact && (
                    <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl text-xs text-slate-400">
                      <strong className="text-rose-400 font-semibold block mb-0.5">Potential Impact:</strong>
                      {f.impact}
                    </div>
                  )}

                  {/* Before / After Diff Comparison */}
                  <CodeDiffViewer
                    originalCode={f.evidenceMasked}
                    suggestedFix={f.suggestedFix}
                    ruleId={f.ruleId}
                  />

                  {/* References & Links */}
                  {f.references && f.references.length > 0 && (
                    <div className="pt-2 border-t border-slate-800 flex flex-wrap gap-2 text-[11px]">
                      <span className="text-slate-500">Security References:</span>
                      {f.references.map((ref, idx) => (
                        <a
                          key={idx}
                          href={ref}
                          target="_blank"
                          rel="noreferrer"
                          className="text-emerald-400 hover:text-emerald-300 underline underline-offset-2 flex items-center space-x-1"
                        >
                          <span>OWASP Reference</span>
                          <ExternalLink className="w-3 h-3 inline" />
                        </a>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
