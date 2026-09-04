import React, { useEffect, useState } from 'react';
import { getAnalysisJob, getFindings } from '../api/client';
import { CheckCircle, AlertTriangle, XCircle, Clock, Shield, ArrowLeft, RefreshCw, FileText } from 'lucide-react';

export default function AnalysisDetailView({ jobId, onBack }) {
  const [job, setJob] = useState(null);
  const [findings, setFindings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchJobData = async () => {
    try {
      const data = await getAnalysisJob(jobId);
      setJob(data);

      if (data.status === 'COMPLETED' || data.status === 'FAILED') {
        const fData = await getFindings(jobId);
        setFindings(fData.content || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to load analysis details.');
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
  }, [jobId, job?.status]);

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

  if (loading && !job) {
    return (
      <div className="py-20 text-center space-y-4">
        <RefreshCw className="w-8 h-8 animate-spin text-emerald-400 mx-auto" />
        <p className="text-slate-400 text-sm">Loading audit job status...</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="flex items-center space-x-2 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Overview</span>
        </button>
        <div className="flex items-center space-x-3">
          <a
            href={`/api/v1/analyses/${jobId}/report?format=html`}
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium rounded-lg flex items-center space-x-1.5 transition-colors"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Export HTML Report</span>
          </a>
        </div>
      </div>

      {/* Overview Card */}
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

        {/* Progress bar */}
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
              <div className="text-3xl font-black text-emerald-400 mt-1">{job?.overallScore ?? 100}/100</div>
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

      {/* Findings Section */}
      {job?.status === 'COMPLETED' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white">Detected Findings ({findings.length})</h3>
          </div>

          {findings.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-8 text-center space-y-2">
              <CheckCircle className="w-10 h-10 text-emerald-400 mx-auto" />
              <p className="text-slate-200 font-semibold">No critical issues detected in this stage.</p>
              <p className="text-xs text-slate-500">Note: Rules will be expanded in Milestone 4.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {findings.map((f) => (
                <div key={f.id} className="bg-slate-900/50 border border-slate-800 rounded-xl p-5 space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="space-y-1">
                      <div className="flex items-center space-x-2">
                        <span className="text-xs font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                          {f.ruleId}
                        </span>
                        <span className="text-sm font-bold text-white">{f.title}</span>
                      </div>
                      <p className="text-xs text-slate-400 font-mono">{f.filePath}:{f.startLine}</p>
                    </div>
                    <span className="px-2 py-1 text-xs font-bold bg-rose-500/10 text-rose-400 border border-rose-500/30 rounded">
                      {f.severity}
                    </span>
                  </div>

                  <p className="text-xs text-slate-300">{f.description}</p>

                  {f.evidenceMasked && (
                    <div className="bg-slate-950 p-3 rounded-lg border border-slate-800 text-xs font-mono text-amber-300 overflow-x-auto">
                      <code>{f.evidenceMasked}</code>
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
