import React, { useEffect, useState, useMemo } from 'react';
import { getAnalysisJob, getFindings } from '../api/client';
import CodeDiffViewer from './CodeDiffViewer';
import FindingsFilterBar from './FindingsFilterBar';
import FileTreeExplorer from './FileTreeExplorer';
import LiveReviewPulseLoader from './LiveReviewPulseLoader';
import { 
  CheckCircle, AlertTriangle, XCircle, Clock, Shield, 
  ArrowLeft, RefreshCw, FileText, ExternalLink, HelpCircle,
  LayoutGrid, ListFilter, Sparkles, FolderTree, Code
} from 'lucide-react';

export default function AnalysisDetailView({ jobId, onBack }) {
  const [job, setJob] = useState(null);
  const [findings, setFindings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [searchFilter, setSearchFilter] = useState('');
  const [selectedFile, setSelectedFile] = useState('');
  const [activeFindingId, setActiveFindingId] = useState(null);

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
        const allFindings = fData.content || [];
        setFindings(allFindings);
        if (allFindings.length > 0 && !activeFindingId) {
          setActiveFindingId(allFindings[0].id);
        }
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
    }, 1800);

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

  const getVerdictBadge = (verdict) => {
    switch (verdict) {
      case 'REVIEW_COMPLETE':
        return <span className="px-3.5 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full text-xs font-bold shadow-sm shadow-emerald-500/20">Review Complete / Low Risk</span>;
      case 'GENERALLY_PROMISING':
        return <span className="px-3.5 py-1 bg-teal-500/10 text-teal-400 border border-teal-500/30 rounded-full text-xs font-bold shadow-sm shadow-teal-500/20">Generally Promising</span>;
      case 'NEEDS_URGENT_FIXES':
        return <span className="px-3.5 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/30 rounded-full text-xs font-bold shadow-sm shadow-amber-500/20">Needs Urgent Fixes</span>;
      default:
        return <span className="px-3.5 py-1 bg-rose-500/10 text-rose-400 border border-rose-500/30 rounded-full text-xs font-bold shadow-sm shadow-rose-500/20">Not Ready / High Risk</span>;
    }
  };

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2.5 py-0.5 rounded-md text-[11px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/40 shadow-sm shadow-rose-500/20">CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2.5 py-0.5 rounded-md text-[11px] font-bold bg-orange-500/20 text-orange-300 border border-orange-500/40 shadow-sm shadow-orange-500/20">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 rounded-md text-[11px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40 shadow-sm shadow-amber-500/20">MEDIUM</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-md text-[11px] font-bold bg-blue-500/20 text-blue-300 border border-blue-500/40">LOW</span>;
    }
  };

  if (loading && !job) {
    return (
      <div className="py-24 text-center space-y-4">
        <RefreshCw className="w-10 h-10 animate-spin text-emerald-400 mx-auto" />
        <p className="text-slate-300 text-sm font-medium">Connecting to Codexa Inspection Engine...</p>
      </div>
    );
  }

  const isScanning = job && job.status !== 'COMPLETED' && job.status !== 'FAILED';

  return (
    <div className="max-w-7xl mx-auto py-8 space-y-8">
      {/* Top Navigation & Report Exporters */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <button
          onClick={onBack}
          className="flex items-center space-x-2 px-3.5 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:border-slate-700 transition-all shadow-sm"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </button>

        {job?.status === 'COMPLETED' && (
          <div className="flex items-center space-x-2">
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=html`}
              target="_blank"
              rel="noreferrer"
              className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-xl flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-emerald-400" />
              <span>Export HTML</span>
            </a>
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=markdown`}
              target="_blank"
              rel="noreferrer"
              className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-xl flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-teal-400" />
              <span>Export Markdown</span>
            </a>
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=json`}
              target="_blank"
              rel="noreferrer"
              className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-xl flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-blue-400" />
              <span>Export JSON</span>
            </a>
          </div>
        )}
      </div>

      {/* When In Scanning Mode -> Display Animated Pulse Loader */}
      {isScanning && (
        <LiveReviewPulseLoader job={job} />
      )}

      {/* Completed Inspection Dashboard */}
      {job?.status === 'COMPLETED' && (
        <>
          {/* Main Scorecard Banner */}
          <div className="bg-slate-900/80 border border-slate-800/90 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl backdrop-blur-md">
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pb-6 border-b border-slate-800">
              <div className="space-y-1">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">{job?.sourceIdentifier}</h1>
                  <span className="text-[11px] px-2.5 py-0.5 bg-slate-800 text-emerald-400 rounded-md uppercase font-mono font-bold border border-slate-700">
                    {job?.sourceType}
                  </span>
                </div>
                <p className="text-xs text-slate-500 font-mono">Job ID: {jobId} &bull; Total Files: {job?.metrics?.totalFiles || 0}</p>
              </div>

              <div className="flex flex-wrap items-center gap-3">
                {job?.verdict && getVerdictBadge(job.verdict)}
                <span className="px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  COMPLETED
                </span>
              </div>
            </div>

            {/* Score Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl relative overflow-hidden">
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Overall Score</div>
                <div className={`text-4xl font-black mt-2 font-mono ${
                  (job?.overallScore ?? 100) >= 75 ? 'text-emerald-400' :
                  (job?.overallScore ?? 100) >= 50 ? 'text-amber-400' : 'text-rose-400'
                }`}>
                  {job?.overallScore ?? 100}
                  <span className="text-base text-slate-500 font-normal">/100</span>
                </div>
                <div className="mt-2 text-[11px] text-slate-500 font-medium">Production Readiness Index</div>
              </div>

              <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl">
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Security (60%)</div>
                <div className="text-3xl font-black text-slate-100 mt-2 font-mono">
                  {job?.metrics?.securityScore ?? 100}
                  <span className="text-base text-slate-500 font-normal">/100</span>
                </div>
                <div className="mt-2 flex items-center space-x-2 text-[11px] font-mono">
                  <span className="text-rose-400 font-bold">{job?.metrics?.criticalCount || 0} Critical</span>
                  <span className="text-slate-600">&bull;</span>
                  <span className="text-orange-400 font-bold">{job?.metrics?.highCount || 0} High</span>
                </div>
              </div>

              <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl">
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Quality (25%)</div>
                <div className="text-3xl font-black text-slate-100 mt-2 font-mono">
                  {job?.metrics?.qualityScore ?? 100}
                  <span className="text-base text-slate-500 font-normal">/100</span>
                </div>
                <div className="mt-2 text-[11px] text-slate-500">AST Code Smells &amp; Error Handling</div>
              </div>

              <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl">
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Operations (15%)</div>
                <div className="text-3xl font-black text-slate-100 mt-2 font-mono">
                  {job?.metrics?.operationsScore ?? 100}
                  <span className="text-base text-slate-500 font-normal">/100</span>
                </div>
                <div className="mt-2 text-[11px] text-slate-500">Logging &amp; Deployment Hardening</div>
              </div>
            </div>
          </div>

          {/* Filter Bar */}
          <FindingsFilterBar
            category={categoryFilter} setCategory={setCategoryFilter}
            severity={severityFilter} setSeverity={setSeverityFilter}
            search={searchFilter} setSearch={setSearchFilter}
          />

          {/* Workspace Layout: Left (File Explorer Tree) | Right (Detailed Findings) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            {/* Left Column: Repository Folder Structure with Severity Color Badges */}
            <div className="lg:col-span-4 sticky top-6">
              <FileTreeExplorer
                findings={findings}
                selectedFile={selectedFile}
                onSelectFile={setSelectedFile}
              />
            </div>

            {/* Right Column: Code Findings & AI Remediation Diffs */}
            <div className="lg:col-span-8 space-y-4">
              <div className="flex items-center justify-between pb-1">
                <h3 className="text-base font-bold text-white flex items-center space-x-2">
                  <span>Detected Findings</span>
                  <span className="px-2 py-0.5 rounded-full bg-slate-800 text-xs text-emerald-400 font-mono">
                    {filteredFindings.length}
                  </span>
                </h3>
                {selectedFile && (
                  <span className="text-xs text-slate-400 font-mono truncate max-w-xs">
                    Showing issues for: <strong className="text-emerald-400">{selectedFile}</strong>
                  </span>
                )}
              </div>

              {filteredFindings.length === 0 ? (
                <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-12 text-center space-y-3">
                  <CheckCircle className="w-12 h-12 text-emerald-400 mx-auto" />
                  <p className="text-slate-200 font-bold text-base">No issues found matching criteria!</p>
                  <p className="text-xs text-slate-500">All scanned AST rules and heuristics passed for this selection.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {filteredFindings.map((f) => (
                    <div 
                      key={f.id} 
                      className="bg-slate-900/60 border border-slate-800 hover:border-slate-700 rounded-3xl p-6 sm:p-7 space-y-5 transition-all shadow-md"
                    >
                      {/* Finding Title & Severity Badge */}
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                        <div className="flex items-center space-x-2.5">
                          <span className="text-xs font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded-lg border border-emerald-500/20">
                            {f.ruleId}
                          </span>
                          <h4 className="text-base font-bold text-white tracking-tight">{f.title}</h4>
                        </div>

                        <div className="flex items-center space-x-2.5 shrink-0">
                          {f.requiresManualReview && (
                            <span className="px-2.5 py-0.5 text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 rounded-md">
                              Review Required
                            </span>
                          )}
                          <span className="text-xs font-mono text-slate-400">
                            Priority: {f.priorityScore}
                          </span>
                          {getSeverityBadge(f.severity)}
                        </div>
                      </div>

                      {/* File Path & OWASP Mapping */}
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-slate-400">
                        <span className="font-mono text-slate-200 bg-slate-950 px-2.5 py-1 rounded-md border border-slate-800">
                          📄 {f.filePath}:{f.startLine}
                        </span>
                        {f.owaspMapping && (
                          <span className="text-emerald-400 font-medium flex items-center space-x-1">
                            <span>🛡️</span>
                            <span>{f.owaspMapping}</span>
                          </span>
                        )}
                      </div>

                      {/* Plain-English Explanation */}
                      <p className="text-xs text-slate-300 leading-relaxed bg-slate-950/40 p-3.5 rounded-xl border border-slate-800/60">
                        {f.description}
                      </p>

                      {/* Potential Risk Impact */}
                      {f.impact && (
                        <div className="p-3.5 bg-rose-950/20 border border-rose-500/20 rounded-xl text-xs text-slate-300">
                          <strong className="text-rose-400 font-bold block mb-1">Potential Security &amp; Operational Impact:</strong>
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
                        <div className="pt-3 border-t border-slate-800 flex flex-wrap gap-2 text-[11px]">
                          <span className="text-slate-500 font-medium">Security References:</span>
                          {f.references.map((ref, idx) => (
                            <a
                              key={idx}
                              href={ref}
                              target="_blank"
                              rel="noreferrer"
                              className="text-emerald-400 hover:text-emerald-300 underline underline-offset-2 flex items-center space-x-1"
                            >
                              <span>OWASP Guideline</span>
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
          </div>
        </>
      )}
    </div>
  );
}
