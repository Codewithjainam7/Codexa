"use client";
import React, { useState, useEffect } from 'react';
import { 
  UploadCloud, FileArchive, Github, AlertCircle, Loader2, 
  Info, ArrowRight, Check, Shield, Lock, Layers,
  Activity, ShieldCheck, Cpu, CheckCircle2, GitBranch
} from 'lucide-react';
import { submitZip, submitGitHubUrl } from '../api/client';
import GlowingEffect from './ui/GlowingEffect';

export default function NewAnalysisView({ limits, onJobCreated }) {
  const [activeTab, setActiveTab] = useState('github');
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [githubUrl, setGithubUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Dynamic Live Staging Telemetry
  const [stagingProgress, setStagingProgress] = useState(15);
  const [stagingStepText, setStagingStepText] = useState('Connecting to GitHub API Gateway...');
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  useEffect(() => {
    let timerInterval;
    let progressInterval;

    if (isSubmitting) {
      setElapsedSeconds(0);
      setStagingProgress(15);
      setStagingStepText('Connecting to GitHub API Gateway & validating repository...');

      timerInterval = setInterval(() => {
        setElapsedSeconds(prev => prev + 1);
      }, 1000);

      // Smooth non-linear progress ticker that constantly makes visible forward progress
      progressInterval = setInterval(() => {
        setStagingProgress(prev => {
          let next;
          if (prev < 40) {
            next = prev + 2.5;
          } else if (prev < 65) {
            next = prev + 1.8;
          } else if (prev < 82) {
            next = prev + 1.2;
          } else if (prev < 94) {
            next = prev + 0.6;
          } else {
            next = Math.min(prev + 0.2, 98);
          }

          // Dynamically update status text based on active progress
          if (next >= 85) {
            setStagingStepText('Finalizing staging & launching deterministic AST pipeline...');
          } else if (next >= 68) {
            setStagingStepText('Validating file quotas & AST parsing bounds...');
          } else if (next >= 42) {
            setStagingStepText('Decompressing archive with Zip-Slip & safety validation...');
          } else if (next >= 22) {
            setStagingStepText('Streaming repository archive into ephemeral UUID sandbox...');
          }

          return Math.round(next * 10) / 10;
        });
      }, 180);

      return () => {
        clearInterval(timerInterval);
        clearInterval(progressInterval);
      };
    } else {
      setStagingProgress(0);
    }
  }, [isSubmitting]);

  const sampleRepos = [
    { name: 'Spring PetClinic (Java)', url: 'https://github.com/spring-projects/spring-petclinic' },
    { name: 'REST Service (Spring)', url: 'https://github.com/spring-guides/gs-rest-service' },
  ];

  const handleFileDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const selected = e.dataTransfer.files[0];
      if (selected.name.toLowerCase().endsWith('.zip')) {
        setFile(selected);
        setError(null);
      } else {
        setError('Only .zip archives are supported.');
      }
    }
  };

  const handleFileInput = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      const selected = e.target.files[0];
      if (selected.name.toLowerCase().endsWith('.zip')) {
        setFile(selected);
        setError(null);
      } else {
        setError('Only .zip archives are supported.');
      }
    }
  };

  const handleZipSubmit = async () => {
    if (!file) return;
    setIsSubmitting(true);
    setError(null);

    try {
      const result = await submitZip(file);
      onJobCreated(result);
    } catch (err) {
      setError(err.message || 'Failed to upload and stage archive.');
      setIsSubmitting(false);
    }
  };

  const handleGithubSubmit = async (e) => {
    e?.preventDefault();
    if (!githubUrl || !githubUrl.trim()) {
      setError('Please enter a valid GitHub repository URL.');
      return;
    }

    const trimmed = githubUrl.trim();
    if (!/^https:\/\/github\.com\/[a-zA-Z0-9_.-]+\/[a-zA-Z0-9_.-]+(?:\.git)?\/?$/.test(trimmed)) {
      setError('Invalid GitHub URL format. Example: https://github.com/owner/repository');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const result = await submitGitHubUrl(trimmed);
      onJobCreated(result);
    } catch (err) {
      setError(err.message || 'Failed to fetch and analyze GitHub repository.');
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto py-10 sm:py-14 space-y-8 px-4 font-sans">
      {/* Header section */}
      <div className="space-y-3 text-left">
        <div className="inline-flex items-center space-x-2 text-xs font-mono text-[var(--accent-primary)] font-semibold uppercase tracking-wider cdx-pill px-3.5 py-1 rounded-full">
          <Shield className="w-3.5 h-3.5 text-[var(--accent-primary)]" />
          <span>Zero-Bytecode Security Sandbox</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold font-display text-[var(--text-primary)] tracking-tight">
          Start Codebase Audit
        </h1>
        <p className="text-sm sm:text-base text-[var(--text-secondary)] leading-relaxed font-sans font-normal">
          Submit your codebase via public GitHub repository HTTPS link or upload a compressed ZIP archive for deep AST inspection and AI-assisted remediation.
        </p>
      </div>

      {/* Mode Switcher Tabs */}
      <div className="flex p-1.5 rounded-2xl cdx-glass-card border border-[var(--border-subtle)]">
        <button
          onClick={() => { if (!isSubmitting) { setActiveTab('github'); setError(null); } }}
          disabled={isSubmitting}
          className={`flex-1 flex items-center justify-center space-x-2.5 py-3 rounded-xl text-xs sm:text-sm font-bold font-display transition-all duration-200 ${
            activeTab === 'github'
              ? 'bg-[var(--accent-glow)] text-[var(--accent-primary)] border border-[var(--accent-border)] shadow-sm'
              : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'
          }`}
        >
          <Github className="w-4 h-4" />
          <span>Public GitHub Repository</span>
        </button>

        <button
          onClick={() => { if (!isSubmitting) { setActiveTab('zip'); setError(null); } }}
          disabled={isSubmitting}
          className={`flex-1 flex items-center justify-center space-x-2.5 py-3 rounded-xl text-xs sm:text-sm font-bold font-display transition-all duration-200 ${
            activeTab === 'zip'
              ? 'bg-[var(--accent-glow)] text-[var(--accent-primary)] border border-[var(--accent-border)] shadow-sm'
              : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'
          }`}
        >
          <FileArchive className="w-4 h-4" />
          <span>ZIP Archive Upload</span>
        </button>
      </div>

      {/* GitHub URL Tab Container */}
      {activeTab === 'github' && (
        <div className="relative rounded-3xl cdx-glass-card p-6 sm:p-8 shadow-2xl space-y-6">
          <GlowingEffect
            spread={40}
            glow={true}
            disabled={false}
            proximity={64}
            inactiveZone={0.01}
          />
          <div className="relative z-10 space-y-6">
            <form onSubmit={handleGithubSubmit} className="space-y-6">
              <div className="space-y-2.5">
                <label htmlFor="githubUrlInput" className="block text-xs font-semibold text-[var(--text-secondary)] font-display uppercase tracking-wider">
                  GitHub Repository HTTPS Link
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-[var(--text-muted)]">
                    <Github className="w-5 h-5" />
                  </div>
                  <input
                    id="githubUrlInput"
                    type="url"
                    value={githubUrl}
                    onChange={(e) => setGithubUrl(e.target.value)}
                    placeholder="https://github.com/owner/repository"
                    disabled={isSubmitting}
                    className="w-full pl-12 pr-4 py-3.5 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl text-sm text-[var(--text-primary)] placeholder-[var(--text-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-primary)]/40 focus:border-[var(--accent-primary)] transition-all font-mono shadow-inner"
                  />
                </div>
              </div>

              {/* Quick sample chips */}
              <div className="space-y-2">
                <span className="text-[11px] font-medium text-[var(--text-muted)]">Try with a verified sample repository:</span>
                <div className="flex flex-wrap gap-2.5">
                  {sampleRepos.map((sample) => (
                    <button
                      key={sample.url}
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => { setGithubUrl(sample.url); setError(null); }}
                      className="px-3 py-1.5 rounded-lg cdx-pill hover:border-[var(--accent-border)] text-xs text-[var(--text-secondary)] hover:text-[var(--accent-primary)] font-mono transition-all duration-200 flex items-center space-x-2"
                    >
                      <GitBranch className="w-3.5 h-3.5 text-[var(--accent-primary)]" />
                      <span>{sample.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Dynamic Live Staging Progress Telemetry Box */}
              {isSubmitting && (
                <div className="p-4 rounded-xl bg-[var(--bg-recessed)] border border-[var(--accent-border)] space-y-3 animate-fade-in">
                  <div className="flex items-center justify-between text-xs font-mono">
                    <div className="flex items-center space-x-2 text-[var(--accent-primary)] font-semibold">
                      <Loader2 className="w-4 h-4 animate-spin text-[var(--accent-primary)]" />
                      <span>Active Ingestion Pipeline</span>
                    </div>
                    <span className="text-[var(--text-muted)]">{elapsedSeconds}s elapsed</span>
                  </div>

                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between text-[11px] font-mono text-[var(--text-secondary)]">
                      <span className="truncate pr-2">{stagingStepText}</span>
                      <span className="text-[var(--accent-primary)] font-bold">{Math.round(stagingProgress)}%</span>
                    </div>
                    <div className="w-full h-2 bg-[var(--bg-base)] rounded-full overflow-hidden border border-[var(--border-subtle)] p-0.5">
                      <div
                        className="bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-500 h-full rounded-full transition-all duration-300 ease-out"
                        style={{ width: `${Math.min(stagingProgress, 100)}%` }}
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between pt-2 border-t border-[var(--border-subtle)] text-[10px] font-mono text-[var(--text-muted)]">
                    <span className="flex items-center space-x-1">
                      <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                      <span>SSRF Defense Active</span>
                    </span>
                    <span className="flex items-center space-x-1">
                      <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                      <span>Zip Slip Block Active</span>
                    </span>
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting || !githubUrl.trim()}
                className="w-full py-3.5 rounded-xl cdx-btn-primary disabled:opacity-50 text-sm font-bold font-display flex items-center justify-center space-x-2 transition-all duration-200 cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Fetching &amp; Staging Repository ({elapsedSeconds}s)...</span>
                  </>
                ) : (
                  <>
                    <span>Audit GitHub Repository</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ZIP Archive Tab Container */}
      {activeTab === 'zip' && (
        <div className="relative rounded-3xl cdx-glass-card p-6 sm:p-8 shadow-2xl space-y-6">
          <GlowingEffect
            spread={40}
            glow={true}
            disabled={false}
            proximity={64}
            inactiveZone={0.01}
          />
          <div
            onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleFileDrop}
            onClick={(e) => {
              if (!isSubmitting) {
                document.getElementById('zipFileInput')?.click();
              }
            }}
            className={`relative z-10 border-2 border-dashed rounded-2xl p-8 sm:p-12 text-center transition-all duration-200 cursor-pointer select-none ${
              isDragging
                ? 'border-[var(--accent-primary)] bg-[var(--accent-glow)]'
                : file
                ? 'border-[var(--accent-border)] bg-[var(--bg-recessed)]'
                : 'border-[var(--border-medium)] bg-[var(--bg-recessed)] hover:border-[var(--accent-border)]'
            }`}
          >
            <input
              type="file"
              id="zipFileInput"
              accept=".zip"
              onChange={handleFileInput}
              className="hidden"
              disabled={isSubmitting}
            />

            <div className="flex flex-col items-center space-y-4 pointer-events-none">
              <div className="p-3.5 bg-[var(--accent-glow)] text-[var(--accent-primary)] rounded-xl border border-[var(--accent-border)] shadow-inner">
                {file ? <FileArchive className="w-8 h-8" /> : <UploadCloud className="w-8 h-8" />}
              </div>

              <div>
                {file ? (
                  <div className="space-y-1">
                    <p className="font-semibold text-[var(--text-primary)] font-display text-sm">{file.name}</p>
                    <p className="text-xs text-[var(--text-muted)] font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                    <p className="text-[11px] text-[var(--accent-primary)] font-semibold mt-1">Click anywhere to choose a different file</p>
                  </div>
                ) : (
                  <div className="space-y-1.5">
                    <p className="text-sm font-semibold text-[var(--text-primary)] font-display">
                      Drag and drop your project ZIP archive here, or{' '}
                      <span className="text-[var(--accent-primary)] underline underline-offset-2">
                        browse files
                      </span>
                    </p>
                    <p className="text-xs text-[var(--text-muted)] font-mono">
                      Maximum archive size: 250 MB &bull; ZIP files only
                    </p>
                  </div>
                )}
              </div>

              {file && (
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    setFile(null);
                  }}
                  disabled={isSubmitting}
                  className="pointer-events-auto text-xs text-[var(--text-muted)] hover:text-rose-500 font-mono transition-colors cursor-pointer"
                >
                  Remove selected archive
                </button>
              )}
            </div>
          </div>

          {/* Dynamic Staging Telemetry for ZIP */}
          {isSubmitting && (
            <div className="p-4 mx-2 my-4 rounded-xl bg-[var(--bg-recessed)] border border-[var(--accent-border)] space-y-3 animate-fade-in">
              <div className="flex items-center justify-between text-xs font-mono">
                <div className="flex items-center space-x-2 text-[var(--accent-primary)] font-semibold">
                  <Loader2 className="w-4 h-4 animate-spin text-[var(--accent-primary)]" />
                  <span>Staging Archive in Sandbox</span>
                </div>
                <span className="text-[var(--text-muted)]">{elapsedSeconds}s elapsed</span>
              </div>
              <div className="space-y-1.5">
                <div className="flex items-center justify-between text-[11px] font-mono text-[var(--text-secondary)]">
                  <span className="truncate pr-2">{stagingStepText}</span>
                  <span className="text-[var(--accent-primary)] font-bold">{Math.round(stagingProgress)}%</span>
                </div>
                <div className="w-full h-2 bg-[var(--bg-base)] rounded-full overflow-hidden border border-[var(--border-subtle)] p-0.5">
                  <div
                    className="bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-500 h-full rounded-full transition-all duration-300 ease-out"
                    style={{ width: `${Math.min(stagingProgress, 100)}%` }}
                  />
                </div>
              </div>
            </div>
          )}

          <div className="p-4 pt-2">
            <button
              onClick={handleZipSubmit}
              disabled={isSubmitting || !file}
              className="w-full py-3.5 rounded-xl cdx-btn-primary disabled:opacity-50 text-sm font-bold font-display flex items-center justify-center space-x-2 transition-all duration-200 cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Staging &amp; Verifying Sandbox ({elapsedSeconds}s)...</span>
                </>
              ) : (
                <>
                  <span>Audit ZIP Archive</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-600 dark:text-rose-400 text-xs flex items-start space-x-3 backdrop-blur-xl animate-fade-in">
          <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <div className="font-semibold font-display">Staging Request Failed</div>
            <p className="leading-relaxed font-mono">{error}</p>
          </div>
        </div>
      )}

      {/* Security & Quota Guard Info Box */}
      <div className="p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-3 text-xs text-[var(--text-secondary)]">
        <div className="flex items-center space-x-2 text-[var(--text-primary)] font-semibold font-display">
          <ShieldCheck className="w-4 h-4 text-[var(--accent-primary)]" />
          <span>Active Ingestion Quotas &amp; Sandboxing Guarantees</span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1 text-[11px] font-mono">
          <div className="p-2.5 rounded-lg cdx-recessed border border-[var(--border-subtle)] space-y-1">
            <span className="text-[var(--text-muted)] block">Max Archive Bounds</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxZipSizeBytes ? (limits.maxZipSizeBytes / (1024 * 1024)).toFixed(0) : '250'} MB / {limits?.maxFileCount || '5000'} Files</span>
          </div>
          <div className="p-2.5 rounded-lg cdx-recessed border border-[var(--border-subtle)] space-y-1">
            <span className="text-[var(--text-muted)] block">Single File Limit</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxSingleFileSizeBytes ? (limits.maxSingleFileSizeBytes / (1024 * 1024)).toFixed(0) : '50'} MB / Depth {limits?.maxDirectoryDepth || '30'}</span>
          </div>
          <div className="p-2.5 rounded-lg cdx-recessed border border-[var(--border-subtle)] space-y-1">
            <span className="text-[var(--text-muted)] block">Bytecode Execution</span>
            <span className="text-emerald-500 font-semibold">Strict 0 Runtime Exec</span>
          </div>
        </div>
      </div>
    </div>
  );
}
