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

  const getVerdictBadge = (verdict) => {
    switch (verdict) {
      case 'REVIEW_COMPLETE':
        return <span className="px-3.5 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full text-xs font-bold shadow-sm shadow-emerald-500/20">Review Complete / Low Risk</span>;
      case 'GENERALLY_PROMISING':
        return <span className="px-3.5 py-1 bg-cyan-500/10 text-cyan-300 border border-cyan-500/30 rounded-full text-xs font-bold shadow-sm shadow-cyan-500/20">Generally Promising</span>;
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
        <RefreshCw className="w-10 h-10 animate-spin text-cyan-400 mx-auto" />
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
    <div className="max-w-7xl mx-auto py-8 space-y-8">
      {/* Top Navigation & Report Exporters */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <button
          onClick={onBack}
          className="flex items-center space-x-2 px-4 py-2 rounded-full bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:border-slate-700 transition-all shadow-sm active:scale-95 cursor-pointer font-display"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </button>

        {!isScanning && job?.status === 'COMPLETED' && (
          <div className="flex items-center space-x-2">
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=html`}
              target="_blank"
              rel="noreferrer"
              className="px-3.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-full flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-cyan-400" />
              <span>Export HTML</span>
            </a>
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=markdown`}
              target="_blank"
              rel="noreferrer"
              className="px-3.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-full flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-violet-400" />
              <span>Export Markdown</span>
            </a>
            <a
              href={`http://localhost:8080/api/v1/analyses/${jobId}/report?format=json`}
              target="_blank"
              rel="noreferrer"
              className="px-3.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold rounded-full flex items-center space-x-1.5 transition-colors border border-slate-800 shadow-sm"
            >
              <FileText className="w-3.5 h-3.5 text-blue-400" />
              <span>Export JSON</span>
            </a>
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
          <div className="relative rounded-3xl border border-slate-800/90 p-2 md:p-3 bg-slate-950/80 backdrop-blur-xl">
            <GlowingEffect
              spread={45}
              glow={true}
              disabled={false}
              proximity={64}
              inactiveZone={0.01}
            />
            <div className="relative bg-slate-900/70 border border-slate-800/80 rounded-2xl p-6 sm:p-8 space-y-6">
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pb-6 border-b border-slate-800">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2.5">
                    <h1 className="text-xl sm:text-2xl font-black text-white font-display tracking-tight">{job?.sourceIdentifier}</h1>
                    <span className="text-[11px] px-3 py-0.5 bg-cyan-500/10 text-cyan-300 rounded-full uppercase font-mono font-bold border border-cyan-500/30">
                      {job?.sourceType}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 font-mono">Job ID: {jobId} &bull; Total Files: {job?.metrics?.totalFiles || 0}</p>
                </div>

                <div className="flex flex-wrap items-center gap-3">
                  {job?.verdict && getVerdictBadge(job.verdict)}
                  <span className="px-3.5 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
                    COMPLETED
                  </span>
                </div>
              </div>

              {/* Score Cards Grid with Ambient Glows */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl relative overflow-hidden group hover:border-cyan-500/40 transition-all">
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider font-display">Overall Score</div>
                  <div className={`text-4xl font-black mt-2 font-mono ${
                    (job?.overallScore ?? 100) >= 75 ? 'text-emerald-400' :
                    (job?.overallScore ?? 100) >= 50 ? 'text-amber-400' : 'text-rose-400'
                  }`}>
                    {job?.overallScore ?? 100}
                    <span className="text-base text-slate-500 font-normal">/100</span>
                  </div>
                  <div className="mt-2 text-[11px] text-slate-500 font-medium">Production Readiness Index</div>
                </div>

                <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl group hover:border-cyan-500/40 transition-all">
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider font-display">Security (60%)</div>
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

                <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl group hover:border-cyan-500/40 transition-all">
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider font-display">Quality (25%)</div>
                  <div className="text-3xl font-black text-slate-100 mt-2 font-mono">
                    {job?.metrics?.qualityScore ?? 100}
                    <span className="text-base text-slate-500 font-normal">/100</span>
                  </div>
                  <div className="mt-2 text-[11px] text-slate-500">AST Code Smells &amp; Error Handling</div>
                </div>

                <div className="p-5 bg-slate-950/80 border border-slate-800/90 rounded-2xl group hover:border-cyan-500/40 transition-all">
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider font-display">Operations (15%)</div>
                  <div className="text-3xl font-black text-slate-100 mt-2 font-mono">
                    {job?.metrics?.operationsScore ?? 100}
                    <span className="text-base text-slate-500 font-normal">/100</span>
                  </div>
                  <div className="mt-2 text-[11px] text-slate-500">Logging &amp; Deployment Hardening</div>
                </div>
              </div>
            </div>
          </div>

          {/* Filter Bar */}
          <FindingsFilterBar
            category={categoryFilter} setCategory={setCategoryFilter}
            severity={severityFilter} setSeverity={setSeverityFilter}
            search={searchFilter} setSearch={setSearchFilter}
          />

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
                <h3 className="text-base font-bold text-white flex items-center space-x-2 font-display">
                  <span>Detected Findings</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-xs text-cyan-300 font-mono font-bold border border-cyan-500/20">
                    {filteredFindings.length}
                  </span>
                </h3>

                {selectedFile && (
                  <span className="text-xs text-slate-400 font-mono truncate max-w-xs">
                    Filtered: <strong className="text-cyan-300">{selectedFile}</strong>
                  </span>
                )}
              </div>

              {filteredFindings.length === 0 ? (
                <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-12 text-center space-y-3">
                  <CheckCircle className="w-12 h-12 text-cyan-400 mx-auto" />
                  <p className="text-slate-200 font-bold text-base font-display">No issues found matching criteria!</p>
                  <p className="text-xs text-slate-500">All scanned AST rules and heuristics passed for this selection.</p>
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
