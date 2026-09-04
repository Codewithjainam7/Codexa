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
    <div className="max-w-3xl mx-auto py-8 space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white">Start New Codebase Audit</h1>
        <p className="text-sm text-slate-400 mt-1">
          Upload your Java/Spring Boot codebase as a ZIP archive for static security and production readiness inspection.
        </p>
      </div>

      {/* Upload Box */}
      <div
        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleFileDrop}
        className={`border-2 border-dashed rounded-2xl p-10 text-center transition-all ${
          isDragging
            ? 'border-emerald-500 bg-emerald-500/10'
            : file
            ? 'border-emerald-500/50 bg-slate-900/60'
            : 'border-slate-800 bg-slate-900/30 hover:border-slate-700'
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

        <div className="flex flex-col items-center space-y-4">
          <div className="p-4 bg-emerald-500/10 text-emerald-400 rounded-2xl">
            {file ? <FileArchive className="w-10 h-10" /> : <UploadCloud className="w-10 h-10" />}
          </div>

          <div>
            {file ? (
              <div className="space-y-1">
                <p className="font-semibold text-white">{file.name}</p>
                <p className="text-xs text-slate-400">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
              </div>
            ) : (
              <div className="space-y-1">
                <p className="text-sm font-semibold text-white">
                  Drag and drop your project ZIP here, or{' '}
                  <label htmlFor="zipFileInput" className="text-emerald-400 hover:text-emerald-300 cursor-pointer underline underline-offset-2">
                    browse files
                  </label>
                </p>
                <p className="text-xs text-slate-500">Supports Spring Boot &amp; Java project archives</p>
              </div>
            )}
          </div>

          {file && (
            <div className="flex items-center gap-3 pt-2">
              <label
                htmlFor="zipFileInput"
                className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 cursor-pointer transition-colors"
              >
                Change File
              </label>
              <button
                onClick={handleUpload}
                disabled={isUploading}
                className="px-6 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-400 disabled:opacity-50 text-slate-950 text-xs font-bold flex items-center space-x-2 shadow-md shadow-emerald-500/20 transition-all"
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
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start space-x-3 text-rose-300 text-sm">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400 mt-0.5" />
          <div>
            <span className="font-semibold block">Ingestion Error</span>
            {error}
          </div>
        </div>
      )}

      {/* Limits and Safety Notice */}
      <div className="rounded-xl bg-slate-900/40 border border-slate-800/80 p-5 space-y-3">
        <div className="flex items-center space-x-2 text-xs font-bold text-slate-300 uppercase tracking-wider">
          <Info className="w-4 h-4 text-emerald-400" />
          <span>Security &amp; Ingestion Limits</span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div className="bg-slate-950/60 p-2.5 rounded-lg border border-slate-800/60">
            <span className="text-slate-500 block">Max Archive</span>
            <span className="text-slate-200 font-semibold">{limits?.maxCompressedSizeMb || 25} MB</span>
          </div>
          <div className="bg-slate-950/60 p-2.5 rounded-lg border border-slate-800/60">
            <span className="text-slate-500 block">Max Extracted</span>
            <span className="text-slate-200 font-semibold">{limits?.maxExtractedSizeMb || 100} MB</span>
          </div>
          <div className="bg-slate-950/60 p-2.5 rounded-lg border border-slate-800/60">
            <span className="text-slate-500 block">Max Files</span>
            <span className="text-slate-200 font-semibold">{limits?.maxFileCount || 1000} files</span>
          </div>
          <div className="bg-slate-950/60 p-2.5 rounded-lg border border-slate-800/60">
            <span className="text-slate-500 block">Max Depth</span>
            <span className="text-slate-200 font-semibold">{limits?.maxPathDepth || 15} levels</span>
          </div>
        </div>
        <p className="text-xs text-slate-500">
          Strict zero-execution policy enforced. Archives are sanitized against Zip Slip attacks, zip bombs, and binary payloads. Temporary directories are strictly sandboxed and deleted after analysis.
        </p>
      </div>
    </div>
  );
}
