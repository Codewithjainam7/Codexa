import React, { useState } from 'react';
import { UploadCloud, FileArchive, AlertCircle, CheckCircle2, Loader2, Info } from 'lucide-react';
import { submitZip } from '../api/client';

export default function ZipUploadView({ limits, onJobCreated }) {
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState(null);

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

  const handleUpload = async () => {
    if (!file) return;
    setIsUploading(true);
    setError(null);

    try {
      const result = await submitZip(file);
      onJobCreated(result);
    } catch (err) {
      setError(err.message || 'Failed to upload and stage archive.');
      setIsUploading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-8 px-4 font-sans">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold font-display text-[var(--text-primary)]">Start New Codebase Audit</h1>
        <p className="text-sm text-[var(--text-secondary)] mt-1">
          Upload your Java/Spring Boot codebase as a ZIP archive for static security and production readiness inspection.
        </p>
      </div>

      {/* Upload Box */}
      <div
        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleFileDrop}
        onClick={() => {
          if (!isUploading) {
            document.getElementById('zipFileInput')?.click();
          }
        }}
        className={`border-2 border-dashed rounded-2xl p-8 sm:p-10 text-center transition-all cursor-pointer select-none ${
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
          disabled={isUploading}
        />

        <div className="flex flex-col items-center space-y-4 pointer-events-none">
          <div className="p-4 bg-[var(--accent-glow)] text-[var(--accent-primary)] rounded-2xl border border-[var(--accent-border)]">
            {file ? <FileArchive className="w-10 h-10" /> : <UploadCloud className="w-10 h-10" />}
          </div>

          <div>
            {file ? (
              <div className="space-y-1">
                <p className="font-semibold text-[var(--text-primary)]">{file.name}</p>
                <p className="text-xs text-[var(--text-muted)] font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                <p className="text-xs text-[var(--accent-primary)] font-medium mt-1">Click anywhere to change file</p>
              </div>
            ) : (
              <div className="space-y-1">
                <p className="text-sm font-semibold text-[var(--text-primary)]">
                  Drag and drop your project ZIP here, or{' '}
                  <span className="text-[var(--accent-primary)] underline underline-offset-2">
                    browse files
                  </span>
                </p>
                <p className="text-xs text-[var(--text-muted)] font-mono">Supports Java, Spring Boot &amp; Multi-lang project archives (up to 500 MB)</p>
              </div>
            )}
          </div>

          {file && (
            <div className="flex items-center gap-3 pt-2 pointer-events-auto">
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  setFile(null);
                }}
                className="px-4 py-2 rounded-xl bg-[var(--bg-card)] hover:bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-xs font-semibold text-[var(--text-secondary)] cursor-pointer transition-colors"
              >
                Remove File
              </button>
              <button
                onClick={handleUpload}
                disabled={isUploading}
                className="cdx-btn-primary px-6 py-2 rounded-xl text-xs font-bold flex items-center space-x-2 shadow-md shadow-blue-500/20 transition-all cursor-pointer"
              >
                {isUploading ? (
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

      {/* Error alert */}
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start space-x-3 text-rose-600 dark:text-rose-400 text-sm">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-500 mt-0.5" />
          <div>
            <span className="font-semibold block">Ingestion Error</span>
            {error}
          </div>
        </div>
      )}

      {/* Limits and Safety Notice */}
      <div className="rounded-2xl cdx-card border border-[var(--border-subtle)] p-5 space-y-3">
        <div className="flex items-center space-x-2 text-xs font-bold text-[var(--text-primary)] uppercase tracking-wider font-display">
          <Info className="w-4 h-4 text-[var(--accent-primary)]" />
          <span>Security &amp; Ingestion Limits</span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs font-mono">
          <div className="cdx-recessed p-2.5 rounded-xl border border-[var(--border-subtle)]">
            <span className="text-[var(--text-muted)] block text-[11px]">Max Archive</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxCompressedSizeMb || 500} MB</span>
          </div>
          <div className="cdx-recessed p-2.5 rounded-xl border border-[var(--border-subtle)]">
            <span className="text-[var(--text-muted)] block text-[11px]">Max Extracted</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxExtractedSizeMb || 1000} MB</span>
          </div>
          <div className="cdx-recessed p-2.5 rounded-xl border border-[var(--border-subtle)]">
            <span className="text-[var(--text-muted)] block text-[11px]">Max Files</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxFileCount || 20000} files</span>
          </div>
          <div className="cdx-recessed p-2.5 rounded-xl border border-[var(--border-subtle)]">
            <span className="text-[var(--text-muted)] block text-[11px]">Max Depth</span>
            <span className="text-[var(--text-primary)] font-semibold">{limits?.maxPathDepth || 30} levels</span>
          </div>
        </div>
        <p className="text-xs text-[var(--text-muted)]">
          Strict zero-execution policy enforced. Archives are sanitized against Zip Slip attacks, zip bombs, and binary payloads. Temporary directories are strictly sandboxed and deleted after analysis.
        </p>
      </div>
    </div>
  );
}
