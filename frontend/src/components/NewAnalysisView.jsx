import React, { useState } from 'react';
import { 
  UploadCloud, FileArchive, Github, AlertCircle, Loader2, 
  Info, ArrowRight, Check, Shield, Lock, Layers, Sparkles 
} from 'lucide-react';
import { submitZip, submitGitHubUrl } from '../api/client';
import GlowingEffect from './ui/GlowingEffect';

export default function NewAnalysisView({ limits, onJobCreated }) {
  const [activeTab, setActiveTab] = useState('github'); // default to github or zip
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [githubUrl, setGithubUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

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
    if (!/^https:\/\/github\.com\/[a-zA-Z0-9_.-]+\/[a-zA-Z0-9_.-]+(?:\.git)?$/.test(trimmed)) {
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
    <div className="max-w-3xl mx-auto py-10 sm:py-14 space-y-10 px-4">
      {/* Header section */}
      <div className="space-y-3 text-left">
        <div className="inline-flex items-center space-x-2 text-xs font-mono text-cyan-300 font-bold uppercase tracking-wider ios-glass-pill px-3.5 py-1.5 rounded-full">
          <Shield className="w-3.5 h-3.5 text-cyan-400" />
          <span>Zero-Bytecode Security Sandbox</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold font-display text-white tracking-tight">
          Start Codebase Audit
        </h1>
        <p className="text-sm sm:text-base text-slate-300 leading-relaxed font-sans">
          Submit your codebase via public GitHub repository HTTPS link or upload a compressed ZIP archive for deep AST inspection and AI-assisted remediation.
        </p>
      </div>

      {/* Mode Switcher Tabs */}
      <div className="flex p-1.5 rounded-[22px] bg-black/50 border border-white/10 backdrop-blur-2xl shadow-inner">
        <button
          onClick={() => { setActiveTab('github'); setError(null); }}
          className={`flex-1 flex items-center justify-center space-x-2.5 py-3.5 rounded-2xl text-xs sm:text-sm font-bold font-display transition-all duration-300 ${
            activeTab === 'github'
              ? 'bg-gradient-to-r from-cyan-500/90 via-blue-600/90 to-violet-600/90 text-white shadow-[0_8px_25px_rgba(6,182,212,0.35)] border border-white/20'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Github className="w-4 h-4" />
          <span>Public GitHub Repository</span>
        </button>

        <button
          onClick={() => { setActiveTab('zip'); setError(null); }}
          className={`flex-1 flex items-center justify-center space-x-2.5 py-3.5 rounded-2xl text-xs sm:text-sm font-bold font-display transition-all duration-300 ${
            activeTab === 'zip'
              ? 'bg-gradient-to-r from-cyan-500/90 via-blue-600/90 to-violet-600/90 text-white shadow-[0_8px_25px_rgba(6,182,212,0.35)] border border-white/20'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <FileArchive className="w-4 h-4" />
          <span>ZIP Archive Upload</span>
        </button>
      </div>

      {/* GitHub URL Tab Container with Glowing Effect */}
      {activeTab === 'github' && (
        <div className="relative rounded-[32px] ios-glass p-2 md:p-3 shadow-2xl">
          <GlowingEffect
            spread={40}
            glow={true}
            disabled={false}
            proximity={64}
            inactiveZone={0.01}
          />
          <div className="relative rounded-[24px] bg-slate-900/40 border border-white/10 p-6 sm:p-8 space-y-6 backdrop-blur-2xl">
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
                    className="w-full pl-12 pr-4 py-4 bg-black/50 border border-white/10 rounded-2xl text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-400/50 focus:border-cyan-400 transition-all font-mono shadow-inner backdrop-blur-xl"
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
                      onClick={() => { setGithubUrl(sample.url); setError(null); }}
                      className="px-3.5 py-1.5 rounded-xl ios-glass-pill hover:bg-white/10 text-xs text-slate-300 hover:text-cyan-300 font-mono transition-all flex items-center space-x-2"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                      <span>{sample.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting || !githubUrl.trim()}
                className="w-full py-4 rounded-2xl bg-gradient-to-r from-cyan-500 via-blue-600 to-violet-600 hover:opacity-95 disabled:opacity-50 text-white text-sm font-extrabold font-display flex items-center justify-center space-x-2.5 shadow-[0_15px_35px_rgba(6,182,212,0.35)] border border-white/20 transition-all cursor-pointer transform hover:scale-[1.01] active:scale-98"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin text-white" />
                    <span>Fetching &amp; Staging Repository...</span>
                  </>
                ) : (
                  <>
                    <span>Audit GitHub Repository</span>
                    <ArrowRight className="w-4 h-4 text-white" />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ZIP Archive Tab Container with Glowing Effect */}
      {activeTab === 'zip' && (
        <div className="relative rounded-[32px] ios-glass p-2 md:p-3 shadow-2xl">
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
            className={`border-2 border-dashed rounded-[24px] p-8 sm:p-12 text-center transition-all ${
              isDragging
                ? 'border-cyan-400 bg-cyan-500/15'
                : file
                ? 'border-cyan-500/50 bg-black/40'
                : 'border-white/15 bg-black/30 hover:border-white/30'
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
              <div className="p-4 bg-cyan-500/15 text-cyan-300 rounded-2xl border border-cyan-400/30 shadow-inner backdrop-blur-xl">
                {file ? <FileArchive className="w-10 h-10" /> : <UploadCloud className="w-10 h-10" />}
              </div>

              <div>
                {file ? (
                  <div className="space-y-1">
                    <p className="font-semibold text-white font-display text-base">{file.name}</p>
                    <p className="text-xs text-slate-400 font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                  </div>
                ) : (
                  <div className="space-y-1.5">
                    <p className="text-sm font-semibold text-white font-display">
                      Drag and drop your project ZIP archive here, or{' '}
                      <label htmlFor="zipFileInput" className="text-cyan-400 hover:text-cyan-300 hover:underline cursor-pointer underline-offset-2">
                        browse files
                      </label>
                    </p>
                    <p className="text-xs text-slate-400">Supports Java, JavaScript, Python, TypeScript &amp; Go source archives</p>
                  </div>
                )}
              </div>

              {file && (
                <div className="flex items-center gap-3 pt-2">
                  <label
                    htmlFor="zipFileInput"
                    className="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-xs font-semibold text-slate-200 cursor-pointer transition-colors border border-white/15"
                  >
                    Change File
                  </label>
                  <button
                    onClick={handleZipSubmit}
                    disabled={isSubmitting}
                    className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 via-blue-600 to-violet-600 hover:opacity-95 disabled:opacity-50 text-white text-xs font-bold font-display flex items-center space-x-2 shadow-lg shadow-cyan-500/25 border border-white/20 transition-all cursor-pointer active:scale-95"
                  >
                    {isSubmitting ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        <span>Staging Codebase...</span>
                      </>
                    ) : (
                      <>
                        <span>Submit for Audit</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </>
                    )}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Error alert */}
      {error && (
        <div className="p-4 bg-rose-500/15 border border-rose-500/30 rounded-2xl flex items-start space-x-3 text-rose-300 text-sm shadow-xl backdrop-blur-xl">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400 mt-0.5" />
          <div>
            <span className="font-semibold block font-display">Ingestion Error</span>
            {error}
          </div>
        </div>
      )}

      {/* Limits and Safety Notice Container with Glowing Effect */}
      <div className="relative rounded-[32px] ios-glass p-2 md:p-3 shadow-2xl">
        <GlowingEffect
          spread={40}
          glow={true}
          disabled={false}
          proximity={64}
          inactiveZone={0.01}
        />
        <div className="relative rounded-[24px] bg-slate-900/40 border border-white/10 p-6 sm:p-7 space-y-4 backdrop-blur-2xl">
          <div className="flex items-center space-x-2 text-xs font-bold text-slate-300 font-display uppercase tracking-wider">
            <Info className="w-4 h-4 text-cyan-400" />
            <span>Security &amp; Ingestion Limits</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div className="bg-black/40 p-3.5 rounded-2xl border border-white/10 backdrop-blur-xl">
              <span className="text-slate-400 block text-[11px]">Max Archive</span>
              <span className="text-cyan-300 font-bold font-mono text-sm">{limits?.maxCompressedSizeMb || 25} MB</span>
            </div>
            <div className="bg-black/40 p-3.5 rounded-2xl border border-white/10 backdrop-blur-xl">
              <span className="text-slate-400 block text-[11px]">Max Extracted</span>
              <span className="text-cyan-300 font-bold font-mono text-sm">{limits?.maxExtractedSizeMb || 100} MB</span>
            </div>
            <div className="bg-black/40 p-3.5 rounded-2xl border border-white/10 backdrop-blur-xl">
              <span className="text-slate-400 block text-[11px]">Max Files</span>
              <span className="text-cyan-300 font-bold font-mono text-sm">{limits?.maxFileCount || 1000} files</span>
            </div>
            <div className="bg-black/40 p-3.5 rounded-2xl border border-white/10 backdrop-blur-xl">
              <span className="text-slate-400 block text-[11px]">Max Depth</span>
              <span className="text-cyan-300 font-bold font-mono text-sm">{limits?.maxPathDepth || 15} levels</span>
            </div>
          </div>
          <p className="text-xs text-slate-400 leading-relaxed font-sans">
            Strict zero-execution policy enforced. Archives are sanitized against Zip Slip attacks, zip bombs, and binary payloads. Temporary staging directories are sandboxed and securely deleted after analysis.
          </p>
        </div>
      </div>
    </div>
  );
}
