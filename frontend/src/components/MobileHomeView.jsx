"use client";
import React from 'react';
import { 
  ShieldCheck, PlusCircle, ArrowRight, Zap, 
  Lock, Cpu, FileCode2, Activity, CheckCircle2, ChevronRight,
  UploadCloud, Sparkles
} from 'lucide-react';

export default function MobileHomeView({ onStartAnalysis, onViewResults, activeJobId, isConnected }) {
  return (
    <div className="space-y-4 pb-6 pt-1 text-left select-none">
      {/* 1. App Header Greeting Card */}
      <div className="cdx-glass-card rounded-2xl p-4 border border-[var(--border-subtle)] relative overflow-hidden shadow-sm">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-sm">
              <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center p-1 overflow-hidden">
                <img src="/logo.png" alt="CODEXA" className="w-full h-full object-contain" />
              </div>
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <h2 className="text-base font-black font-display text-[var(--text-primary)] tracking-tight">
                  CODEXA Mobile
                </h2>
                <span className="px-1.5 py-0.5 text-[8px] font-mono font-bold uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/25 rounded-md">
                  v2.4
                </span>
              </div>
              <p className="text-[11px] text-slate-500 font-mono mt-0.5">
                AI Code Security & Audit Console
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-1 px-2 py-1 rounded-full bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-[10px] font-mono font-bold">
            <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]' : 'bg-rose-500'}`} />
            <span className={isConnected ? 'text-emerald-500' : 'text-rose-500'}>
              {isConnected ? 'ONLINE' : 'OFFLINE'}
            </span>
          </div>
        </div>
      </div>

      {/* 2. Primary Action Card: Launch Code Audit */}
      <div 
        onClick={onStartAnalysis}
        className="rounded-2xl p-5 bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-700 text-white shadow-lg shadow-blue-500/25 cursor-pointer active:scale-[0.98] transition-all relative overflow-hidden group"
      >
        {/* Ambient background watermark icon */}
        <ShieldCheck className="absolute -bottom-4 -right-4 w-32 h-32 text-white/10 pointer-events-none" />

        <div className="relative z-10 space-y-3">
          <div className="flex items-center space-x-2">
            <span className="p-1.5 rounded-lg bg-white/20 backdrop-blur-sm">
              <Sparkles className="w-4 h-4 text-white" />
            </span>
            <span className="text-xs font-mono font-bold uppercase tracking-wider text-blue-100">
              Deterministic AST &bull; Nemotron 550B
            </span>
          </div>

          <div>
            <h3 className="text-lg font-bold font-display tracking-tight leading-tight">
              Start Codebase Audit
            </h3>
            <p className="text-xs text-blue-100/90 mt-1 leading-relaxed">
              Upload a ZIP archive or analyze a public GitHub repository for OWASP vulnerabilities & AI fixes.
            </p>
          </div>

          <div className="pt-2 flex items-center justify-between">
            <span className="inline-flex items-center space-x-2 px-3.5 py-2 rounded-xl bg-white text-blue-700 font-display font-bold text-xs shadow-md">
              <PlusCircle className="w-4 h-4 stroke-[2.5]" />
              <span>New Audit</span>
            </span>

            <span className="text-[11px] font-mono text-blue-200">
              Takes ~15-30s &rarr;
            </span>
          </div>
        </div>
      </div>

      {/* 3. If Active Audit Exists: Quick Shortcut Card */}
      {activeJobId && (
        <div 
          onClick={onViewResults}
          className="cdx-glass-card rounded-2xl p-4 border border-blue-500/40 bg-blue-500/10 cursor-pointer active:scale-[0.98] transition-all flex items-center justify-between shadow-sm"
        >
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-blue-500 text-white flex items-center justify-center shadow-sm">
              <Activity className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold font-display text-[var(--text-primary)]">
                Latest Audit Results Ready
              </div>
              <div className="text-[10px] font-mono text-blue-500 mt-0.5">
                Job ID: {activeJobId.substring(0, 8)}... &bull; Tap to inspect
              </div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-blue-500" />
        </div>
      )}

      {/* 4. Quick Metrics 2x2 Grid */}
      <div className="space-y-2">
        <span className="text-xs font-mono font-bold uppercase tracking-wider text-[var(--text-muted)] px-1">
          Security Capabilities
        </span>

        <div className="grid grid-cols-2 gap-2.5">
          <div className="cdx-glass-card rounded-xl p-3 border border-[var(--border-subtle)] space-y-1.5">
            <div className="w-7 h-7 rounded-lg bg-blue-500/10 text-blue-500 flex items-center justify-center">
              <Lock className="w-3.5 h-3.5" />
            </div>
            <div className="text-xs font-bold text-[var(--text-primary)] font-display">
              OWASP Top 10
            </div>
            <p className="text-[10px] text-slate-500 leading-snug">
              SQLi, RCE, IDOR, SSRF, & Hardcoded Secrets detection
            </p>
          </div>

          <div className="cdx-glass-card rounded-xl p-3 border border-[var(--border-subtle)] space-y-1.5">
            <div className="w-7 h-7 rounded-lg bg-indigo-500/10 text-indigo-500 flex items-center justify-center">
              <Cpu className="w-3.5 h-3.5" />
            </div>
            <div className="text-xs font-bold text-[var(--text-primary)] font-display">
              Nemotron 550B
            </div>
            <p className="text-[10px] text-slate-500 leading-snug">
              Neural AI generating production-ready code diffs
            </p>
          </div>

          <div className="cdx-glass-card rounded-xl p-3 border border-[var(--border-subtle)] space-y-1.5">
            <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-500 flex items-center justify-center">
              <FileCode2 className="w-3.5 h-3.5" />
            </div>
            <div className="text-xs font-bold text-[var(--text-primary)] font-display">
              Deterministic AST
            </div>
            <p className="text-[10px] text-slate-500 leading-snug">
              Zero-hallucination syntax graph verification
            </p>
          </div>

          <div className="cdx-glass-card rounded-xl p-3 border border-[var(--border-subtle)] space-y-1.5">
            <div className="w-7 h-7 rounded-lg bg-amber-500/10 text-amber-500 flex items-center justify-center">
              <Zap className="w-3.5 h-3.5" />
            </div>
            <div className="text-xs font-bold text-[var(--text-primary)] font-display">
              Readiness Score
            </div>
            <p className="text-[10px] text-slate-500 leading-snug">
              Mathematical scoring for release certification
            </p>
          </div>
        </div>
      </div>

      {/* 5. Supported Upload Types Card */}
      <div className="cdx-glass-card rounded-2xl p-3.5 border border-[var(--border-subtle)] space-y-2">
        <div className="flex items-center justify-between text-xs">
          <span className="font-bold text-[var(--text-primary)] font-display">Supported Repositories</span>
          <span className="text-[10px] font-mono text-slate-500">Max 250MB ZIP</span>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {['Java', 'Spring Boot', 'TypeScript', 'Node.js', 'Python', 'Go', 'PHP'].map((tech) => (
            <span key={tech} className="px-2 py-0.5 rounded-md bg-[var(--bg-recessed)] text-[10px] font-mono font-medium text-slate-600 dark:text-slate-300">
              {tech}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
