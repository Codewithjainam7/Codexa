import React, { useState } from 'react';
import { UploadCloud, FileArchive, Github, AlertCircle, Loader2, Info, ArrowRight, Check } from 'lucide-react';
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
    { name: 'Spring PetClinic', url: 'https://github.com/spring-projects/spring-petclinic' },
    { name: 'Spring Boot REST Sample', url: 'https://github.com/spring-guides/gs-rest-service' }
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
    <div className="max-w-3xl mx-auto py-8 space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white">Start New Codebase Audit</h1>
        <p className="text-sm text-slate-400 mt-1">
          Submit your codebase via public GitHub repository URL or upload a ZIP archive for static security and production readiness inspection.
        </p>
      </div>

      {/* Mode Switcher Tabs */}
      <div className="flex bg-neutral-900 p-1.5 rounded-2xl border border-neutral-800">
        <button
          onClick={() => { setActiveTab('github'); setError(null); }}
          className={`flex-1 flex items-center justify-center space-x-2 py-2.5 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'github'
              ? 'bg-white text-black shadow-md shadow-white/10'
              : 'text-neutral-400 hover:text-white'
          }`}
        >
          <Github className="w-4 h-4" />
          <span>Public GitHub URL</span>
        </button>

        <button
          onClick={() => { setActiveTab('zip'); setError(null); }}
          className={`flex-1 flex items-center justify-center space-x-2 py-2.5 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'zip'
              ? 'bg-white text-black shadow-md shadow-white/10'
              : 'text-neutral-400 hover:text-white'
          }`}
        >
          <FileArchive className="w-4 h-4" />
          <span>ZIP Archive Upload</span>
        </button>
      </div>

      {/* GitHub URL Tab Container with Glowing Effect */}
      {activeTab === 'github' && (
        <div className="relative rounded-3xl border border-neutral-800/90 p-2 md:p-3 bg-neutral-950/80 backdrop-blur-xl">
          <GlowingEffect
            spread={40}
            glow={true}
            disabled={false}
            proximity={64}
            inactiveZone={0.01}
          />
          <div className="relative rounded-2xl bg-neutral-900/70 border border-neutral-800/80 p-6 sm:p-8 space-y-6">
            <form onSubmit={handleGithubSubmit} className="space-y-4">
              <div className="space-y-2">
                <label htmlFor="githubUrlInput" className="block text-xs font-semibold text-neutral-300 uppercase tracking-wider">
                  GitHub Repository HTTPS Link
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-neutral-500">
                    <Github className="w-5 h-5" />
                  </div>
                  <input
                    id="githubUrlInput"
                    type="url"
                    value={githubUrl}
                    onChange={(e) => setGithubUrl(e.target.value)}
                    placeholder="https://github.com/owner/repository"
                    disabled={isSubmitting}
                    className="w-full pl-11 pr-4 py-3 bg-black border border-neutral-800 rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-white transition-all font-mono"
                  />
                </div>
              </div>

              {/* Quick sample chips */}
              <div className="space-y-1.5">
                <span className="text-[11px] font-medium text-neutral-500">Try with a sample repository:</span>
                <div className="flex flex-wrap gap-2">
                  {sampleRepos.map((sample) => (
                    <button
                      key={sample.url}
                      type="button"
                      onClick={() => { setGithubUrl(sample.url); setError(null); }}
                      className="px-2.5 py-1 rounded-md bg-neutral-800 hover:bg-neutral-700 text-[11px] text-neutral-300 border border-neutral-700 font-mono transition-colors"
                    >
                      {sample.name}
                    </button>
                  ))}
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting || !githubUrl.trim()}
                className="w-full py-3.5 rounded-xl bg-white hover:bg-neutral-200 disabled:opacity-50 text-black text-sm font-extrabold flex items-center justify-center space-x-2 shadow-lg shadow-white/10 transition-all cursor-pointer active:scale-98"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Fetching &amp; Staging Repository...</span>
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

      {/* ZIP Archive Tab Container with Glowing Effect */}
      {activeTab === 'zip' && (
        <div className="relative rounded-3xl border border-neutral-800/90 p-2 md:p-3 bg-neutral-950/80 backdrop-blur-xl">
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
            className={`border-2 border-dashed rounded-2xl p-8 sm:p-10 text-center transition-all ${
              isDragging
                ? 'border-white bg-white/10'
                : file
                ? 'border-white/50 bg-neutral-900/60'
                : 'border-neutral-800 bg-neutral-900/40 hover:border-neutral-700'
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
              <div className="p-4 bg-white/10 text-white rounded-2xl border border-white/20 shadow-inner">
                {file ? <FileArchive className="w-10 h-10" /> : <UploadCloud className="w-10 h-10" />}
              </div>

              <div>
                {file ? (
                  <div className="space-y-1">
                    <p className="font-semibold text-white">{file.name}</p>
                    <p className="text-xs text-neutral-400 font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                  </div>
                ) : (
                  <div className="space-y-1">
                    <p className="text-sm font-semibold text-white">
                      Drag and drop your project ZIP here, or{' '}
                      <label htmlFor="zipFileInput" className="text-white hover:underline cursor-pointer underline-offset-2">
                        browse files
                      </label>
                    </p>
                    <p className="text-xs text-neutral-500">Supports Java, JavaScript, Python, TypeScript &amp; Go project archives</p>
                  </div>
                )}
              </div>

              {file && (
                <div className="flex items-center gap-3 pt-2">
                  <label
                    htmlFor="zipFileInput"
                    className="px-4 py-2 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-xs font-semibold text-neutral-300 cursor-pointer transition-colors border border-neutral-700"
                  >
                    Change File
                  </label>
                  <button
                    onClick={handleZipSubmit}
                    disabled={isSubmitting}
                    className="px-6 py-2 rounded-xl bg-white hover:bg-neutral-200 disabled:opacity-50 text-black text-xs font-bold flex items-center space-x-2 shadow-md shadow-white/10 transition-all cursor-pointer active:scale-95"
                  >
                    {isSubmitting ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        <span>Staging Codebase...</span>
                      </>
                    ) : (
                      <>
                        <span>Submit for Audit</span>
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
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start space-x-3 text-rose-300 text-sm shadow-lg">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400 mt-0.5" />
          <div>
            <span className="font-semibold block">Ingestion Error</span>
            {error}
          </div>
        </div>
      )}

      {/* Limits and Safety Notice Container with Glowing Effect */}
      <div className="relative rounded-3xl border border-neutral-800/90 p-2 md:p-3 bg-neutral-950/80 backdrop-blur-xl">
        <GlowingEffect
          spread={40}
          glow={true}
          disabled={false}
          proximity={64}
          inactiveZone={0.01}
        />
        <div className="relative rounded-2xl bg-neutral-900/60 border border-neutral-800/60 p-5 sm:p-6 space-y-4">
          <div className="flex items-center space-x-2 text-xs font-bold text-neutral-300 uppercase tracking-wider">
            <Info className="w-4 h-4 text-white" />
            <span>Security &amp; Ingestion Limits</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div className="bg-black p-3 rounded-xl border border-neutral-800">
              <span className="text-neutral-500 block text-[11px]">Max Archive</span>
              <span className="text-neutral-200 font-bold font-mono">{limits?.maxCompressedSizeMb || 25} MB</span>
            </div>
            <div className="bg-black p-3 rounded-xl border border-neutral-800">
              <span className="text-neutral-500 block text-[11px]">Max Extracted</span>
              <span className="text-neutral-200 font-bold font-mono">{limits?.maxExtractedSizeMb || 100} MB</span>
            </div>
            <div className="bg-black p-3 rounded-xl border border-neutral-800">
              <span className="text-neutral-500 block text-[11px]">Max Files</span>
              <span className="text-neutral-200 font-bold font-mono">{limits?.maxFileCount || 1000} files</span>
            </div>
            <div className="bg-black p-3 rounded-xl border border-neutral-800">
              <span className="text-neutral-500 block text-[11px]">Max Depth</span>
              <span className="text-neutral-200 font-bold font-mono">{limits?.maxPathDepth || 15} levels</span>
            </div>
          </div>
          <p className="text-xs text-neutral-400 leading-relaxed">
            Strict zero-execution policy enforced. Archives are sanitized against Zip Slip attacks, zip bombs, and binary payloads. Temporary staging directories are strictly sandboxed and deleted after analysis.
          </p>
        </div>
      </div>
    </div>
  );
}
