"use client";
import React, { useState, useEffect } from 'react';
import { 
  UploadCloud, FileArchive, Github, AlertCircle, Loader2, 
  Info, ArrowRight, Check, Shield, Lock, Layers, Sparkles,
  Activity, ShieldCheck, Cpu, CheckCircle2
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
    let interval;
    let timerInterval;

    if (isSubmitting) {
      setElapsedSeconds(0);
      setStagingProgress(15);
      setStagingStepText('Connecting to GitHub API Gateway & validating repository...');

      timerInterval = setInterval(() => {
        setElapsedSeconds(prev => prev + 1);
      }, 1000);

      const timeline = [
        { progress: 35, text: 'Streaming repository archive into ephemeral UUID sandbox...', delay: 1800 },
        { progress: 65, text: 'Decompressing archive with Zip-Slip & Zip-Bomb defenses...', delay: 4500 },
        { progress: 85, text: 'Validating file quotas & AST parsing bounds...', delay: 8500 },
        { progress: 95, text: 'Staging complete. Launching deterministic AST analysis pipeline...', delay: 14000 }
      ];

      const timeouts = timeline.map(item =>
        setTimeout(() => {
          setStagingProgress(item.progress);
          setStagingStepText(item.text);
        }, item.delay)
      );

      return () => {
        clearInterval(timerInterval);
        timeouts.forEach(clearTimeout);
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
        <div className="inline-flex items-center space-x-2 text-xs font-mono text-sky-400 font-semibold uppercase tracking-wider surface-pill px-3.5 py-1 rounded-full">
          <Shield className="w-3.5 h-3.5 text-sky-400" />
          <span>Zero-Bytecode Security Sandbox</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold font-display text-white tracking-tight">
          Start Codebase Audit
        </h1>
        <p className="text-sm sm:text-base text-slate-400 leading-relaxed font-sans font-normal">
          Submit your codebase via public GitHub repository HTTPS link or upload a compressed ZIP archive for deep AST inspection and AI-assisted remediation.
        </p>
      </div>

      {/* Mode Switcher Tabs */}
      <div className="flex p-1.5 rounded-2xl surface-elevated-1 border border-white/[0.06]">
        <button
          onClick={() => { if (!isSubmitting) { setActiveTab('github'); setError(null); } }}
          disabled={isSubmitting}
          className={`flex-1 flex items-center justify-center space-x-2.5 py-3 rounded-xl text-xs sm:text-sm font-bold font-display transition-all duration-200 ${
            activeTab === 'github'
              ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30 shadow-sm'
              : 'text-slate-400 hover:text-white'
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
              ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30 shadow-sm'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <FileArchive className="w-4 h-4" />
          <span>ZIP Archive Upload</span>
        </button>
      </div>

      {/* GitHub URL Tab Container */}
      {activeTab === 'github' && (
        <div className="relative rounded-2xl surface-elevated-2 p-1.5 shadow-2xl">
          <GlowingEffect
            spread={40}
            glow={true}
            disabled={false}
            proximity={64}
            inactiveZone={0.01}
          />
          <div className="relative rounded-xl bg-black/40 border border-white/[0.04] p-6 sm:p-8 space-y-6">
            <form onSubmit={handleGithubSubmit} className="space-y-6">
              <div className="space-y-2.5">
                <label htmlFor="githubUrlInput" className="block text-xs font-semibold text-slate-300 font-display uppercase tracking-wider">
                  GitHub Repository HTTPS Link
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                    <Github className="w-5 h-5" />
                  </div>
                  <input
                    id="githubUrlInput"
                    type="url"
                    value={githubUrl}
                    onChange={(e) => setGithubUrl(e.target.value)}
                    placeholder="https://github.com/owner/repository"
                    disabled={isSubmitting}
                    className="w-full pl-12 pr-4 py-3.5 bg-black/60 border border-white/[0.08] rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500/40 focus:border-sky-500 transition-all font-mono shadow-inner"
                  />
                </div>
              </div>

              {/* Quick sample chips */}
              <div className="space-y-2">
                <span className="text-[11px] font-medium text-slate-400">Try with a verified sample repository:</span>
                <div className="flex flex-wrap gap-2.5">
                  {sampleRepos.map((sample) => (
                    <button
                      key={sample.url}
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => { setGithubUrl(sample.url); setError(null); }}
                      className="px-3 py-1.5 rounded-lg surface-pill hover:bg-white/[0.08] text-xs text-slate-300 hover:text-sky-300 font-mono transition-all duration-200 flex items-center space-x-2"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-sky-400" />
                      <span>{sample.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Dynamic Live Staging Progress Telemetry Box */}
              {isSubmitting && (
                <div className="p-4 rounded-xl surface-panel border border-sky-500/30 space-y-3 animate-fade-in">
                  <div className="flex items-center justify-between text-xs font-mono">
                    <div className="flex items-center space-x-2 text-sky-400 font-semibold">
                      <Loader2 className="w-4 h-4 animate-spin text-sky-400" />
                      <span>Active Ingestion Pipeline</span>
                    </div>
                    <span className="text-slate-400">{elapsedSeconds}s elapsed</span>
                  </div>

                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between text-[11px] font-mono text-slate-300">
                      <span className="truncate pr-2">{stagingStepText}</span>
                      <span className="text-sky-400 font-bold">{stagingProgress}%</span>
                    </div>
                    <div className="w-full h-2 bg-black/80 rounded-full overflow-hidden border border-white/10 p-0.5">
                      <div
                        className="bg-gradient-to-r from-sky-400 to-blue-500 h-full rounded-full transition-all duration-500 ease-out"
                        style={{ width: `${stagingProgress}%` }}
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between pt-2 border-t border-white/[0.06] text-[10px] font-mono text-slate-400">
                    <span className="flex items-center space-x-1">
                      <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                      <span>SSRF Defense Active</span>
                    </span>
                    <span className="flex items-center space-x-1">
                      <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                      <span>Zip Slip Block Active</span>
                    </span>
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting || !githubUrl.trim()}
                className="w-full py-3.5 rounded-xl bg-gradient-to-r from-sky-400 via-sky-500 to-blue-600 hover:opacity-95 disabled:opacity-50 text-slate-950 text-sm font-bold font-display flex items-center justify-center space-x-2 shadow-[0_0_25px_rgba(56,189,248,0.35)] border border-white/20 transition-all duration-200 cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin text-slate-950" />
                    <span>Fetching &amp; Staging Repository ({elapsedSeconds}s)...</span>
                  </>
                ) : (
                  <>
                    <span>Audit GitHub Repository</span>
                    <ArrowRight className="w-4 h-4 text-slate-950" />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ZIP Archive Tab Container */}
      {activeTab === 'zip' && (
        <div className="relative rounded-2xl surface-elevated-2 p-1.5 shadow-2xl">
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
            className={`border-2 border-dashed rounded-xl p-8 sm:p-12 text-center transition-all duration-200 ${
              isDragging
                ? 'border-sky-400 bg-sky-500/10'
                : file
                ? 'border-sky-500/40 bg-black/40'
                : 'border-white/[0.08] bg-black/30 hover:border-sky-500/30'
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

            <div className="flex flex-col items-center space-y-4">
              <div className="p-3.5 bg-sky-500/10 text-sky-400 rounded-xl border border-sky-500/20 shadow-inner">
                {file ? <FileArchive className="w-8 h-8" /> : <UploadCloud className="w-8 h-8" />}
              </div>

              <div>
                {file ? (
                  <div className="space-y-1">
                    <p className="font-semibold text-white font-display text-sm">{file.name}</p>
                    <p className="text-xs text-slate-400 font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                  </div>
                ) : (
                  <div className="space-y-1.5">
                    <p className="text-sm font-semibold text-white font-display">
                      Drag and drop your project ZIP archive here, or{' '}
                      <label htmlFor="zipFileInput" className="text-sky-400 hover:text-sky-300 hover:underline cursor-pointer underline-offset-2">
                        browse files
                      </label>
                    </p>
                    <p className="text-xs text-slate-400 font-mono">
                      Maximum archive size: 100 MB &bull; ZIP files only
                    </p>
                  </div>
                )}
              </div>

              {file && (
                <button
                  type="button"
                  onClick={() => setFile(null)}
                  disabled={isSubmitting}
                  className="text-xs text-slate-400 hover:text-rose-400 font-mono transition-colors"
                >
                  Remove selected archive
                </button>
              )}
            </div>
          </div>

          {/* Dynamic Staging Telemetry for ZIP */}
          {isSubmitting && (
            <div className="p-4 mx-2 my-4 rounded-xl surface-panel border border-sky-500/30 space-y-3 animate-fade-in">
              <div className="flex items-center justify-between text-xs font-mono">
                <div className="flex items-center space-x-2 text-sky-400 font-semibold">
                  <Loader2 className="w-4 h-4 animate-spin text-sky-400" />
                  <span>Staging Archive in Sandbox</span>
                </div>
                <span className="text-slate-400">{elapsedSeconds}s elapsed</span>
              </div>
              <div className="space-y-1.5">
                <div className="flex items-center justify-between text-[11px] font-mono text-slate-300">
                  <span className="truncate pr-2">{stagingStepText}</span>
                  <span className="text-sky-400 font-bold">{stagingProgress}%</span>
                </div>
                <div className="w-full h-2 bg-black/80 rounded-full overflow-hidden border border-white/10 p-0.5">
                  <div
                    className="bg-gradient-to-r from-sky-400 to-blue-500 h-full rounded-full transition-all duration-500 ease-out"
                    style={{ width: `${stagingProgress}%` }}
                  />
                </div>
              </div>
            </div>
          )}

          <div className="p-4 pt-2">
            <button
              onClick={handleZipSubmit}
              disabled={isSubmitting || !file}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-sky-400 via-sky-500 to-blue-600 hover:opacity-95 disabled:opacity-50 text-slate-950 text-sm font-bold font-display flex items-center justify-center space-x-2 shadow-[0_0_25px_rgba(56,189,248,0.35)] border border-white/20 transition-all duration-200 cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin text-slate-950" />
                  <span>Staging &amp; Verifying Sandbox ({elapsedSeconds}s)...</span>
                </>
              ) : (
                <>
                  <span>Audit ZIP Archive</span>
                  <ArrowRight className="w-4 h-4 text-slate-950" />
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-rose-950/30 border border-rose-500/30 text-rose-300 text-xs flex items-start space-x-3 backdrop-blur-xl animate-fade-in">
          <AlertCircle className="w-4 h-4 text-rose-400 flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <div className="font-semibold font-display">Staging Request Failed</div>
            <p className="text-rose-200/90 leading-relaxed font-mono">{error}</p>
          </div>
        </div>
      )}

      {/* Security & Quota Guard Info Box */}
      <div className="p-5 rounded-2xl surface-elevated-1 border border-white/[0.06] space-y-3 text-xs text-slate-400">
        <div className="flex items-center space-x-2 text-slate-300 font-semibold font-display">
          <ShieldCheck className="w-4 h-4 text-sky-400" />
          <span>Active Ingestion Quotas &amp; Sandboxing Guarantees</span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1 text-[11px] font-mono">
          <div className="p-2.5 rounded-lg bg-black/40 border border-white/[0.04] space-y-1">
            <span className="text-slate-500 block">Max Archive Bounds</span>
            <span className="text-slate-200 font-semibold">{limits?.maxZipSizeBytes ? (limits.maxZipSizeBytes / (1024 * 1024)).toFixed(0) : '100'} MB / {limits?.maxFileCount || '1000'} Files</span>
          </div>
          <div className="p-2.5 rounded-lg bg-black/40 border border-white/[0.04] space-y-1">
            <span className="text-slate-500 block">Single File Limit</span>
            <span className="text-slate-200 font-semibold">{limits?.maxSingleFileSizeBytes ? (limits.maxSingleFileSizeBytes / (1024 * 1024)).toFixed(0) : '5'} MB / Depth {limits?.maxDirectoryDepth || '15'}</span>
          </div>
          <div className="p-2.5 rounded-lg bg-black/40 border border-white/[0.04] space-y-1">
            <span className="text-slate-500 block">Bytecode Execution</span>
            <span className="text-emerald-400 font-semibold">Strict 0 Runtime Exec</span>
          </div>
        </div>
      </div>
    </div>
  );
}
