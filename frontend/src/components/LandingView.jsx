import React from 'react';
import { Shield, Zap, FileCode, CheckCircle2, Lock, Terminal, ArrowRight } from 'lucide-react';
import DottedGlowBackground from './ui/DottedGlowBackground';

export default function LandingView({ onStartAnalysis }) {
  return (
    <div className="relative space-y-16 py-8">
      {/* Background Dotted Matrix for Landing */}
      <DottedGlowBackground
        className="opacity-50"
        gap={20}
        radius={1.4}
        colorDarkVar="#1e293b"
        glowColorDarkVar="#10b981"
        speedScale={0.8}
      />

      {/* Hero Section */}
      <section className="text-center max-w-4xl mx-auto space-y-6 relative z-10">
        <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-medium">
          <Shield className="w-3.5 h-3.5" />
          <span>Zero-Execution Deterministic SAST + AI Explanations</span>
        </div>
        <h1 className="text-4xl sm:text-6xl font-black text-white tracking-tight leading-tight">
          Is Your AI-Generated Code <br className="hidden sm:inline" />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-300">
            Ready for Production?
          </span>
        </h1>
        <p className="text-lg text-slate-400 max-w-2xl mx-auto">
          Codexa audits "vibe-coded" Spring Boot applications, detects OWASP security vulnerabilities &amp; architectural flaws, ranks findings by real risk, and computes an explainable <strong>Production Readiness Score</strong>.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-base flex items-center justify-center space-x-2 shadow-lg shadow-emerald-500/25 transition-all transform hover:-translate-y-0.5"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5" />
          </button>
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-800 font-semibold text-base transition-colors"
          >
            View GitHub Source
          </a>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 rounded-2xl bg-slate-900/50 border border-slate-800 space-y-3">
          <div className="p-3 bg-emerald-500/10 text-emerald-400 rounded-xl w-fit">
            <Lock className="w-6 h-6" />
          </div>
          <h3 className="text-lg font-bold text-white">OWASP Static Security</h3>
          <p className="text-sm text-slate-400 leading-relaxed">
            Deterministic AST checking for SQL injection, command execution, hardcoded credentials, broken access control, and cryptographic flaws.
          </p>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/50 border border-slate-800 space-y-3">
          <div className="p-3 bg-teal-500/10 text-teal-400 rounded-xl w-fit">
            <Zap className="w-6 h-6" />
          </div>
          <h3 className="text-lg font-bold text-white">Readiness Scoring (0–100)</h3>
          <p className="text-sm text-slate-400 leading-relaxed">
            Transparent scoring model weighting Security (60%), Quality (25%), and Operations (15%) with strict caps for critical injection and auth defects.
          </p>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/50 border border-slate-800 space-y-3">
          <div className="p-3 bg-blue-500/10 text-blue-400 rounded-xl w-fit">
            <FileCode className="w-6 h-6" />
          </div>
          <h3 className="text-lg font-bold text-white">Before &amp; After Fix Diffs</h3>
          <p className="text-sm text-slate-400 leading-relaxed">
            Structured before/after code diffs and plain-English explanations with secret masking and copyable secure implementations.
          </p>
        </div>
      </section>

      {/* Pipeline Stage Architecture */}
      <section className="rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8 space-y-6">
        <h2 className="text-xl font-bold text-white">Multi-Stage Analysis Pipeline</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800">
            <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">Stage 1</div>
            <div className="text-sm font-semibold text-slate-200">Secure Sandboxed Ingestion</div>
            <p className="text-xs text-slate-500 mt-1">Zip Slip prevention, bomb quotas, and file filtering without execution.</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800">
            <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">Stage 2</div>
            <div className="text-sm font-semibold text-slate-200">JavaParser AST Inspection</div>
            <p className="text-xs text-slate-500 mt-1">Deep AST visitor analysis for syntax and data-flow patterns.</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800">
            <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">Stage 3</div>
            <div className="text-sm font-semibold text-slate-200">Explainable Prioritization</div>
            <p className="text-xs text-slate-500 mt-1">Mathematical ranking factoring severity, confidence, exposure &amp; impact.</p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800">
            <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">Stage 4</div>
            <div className="text-sm font-semibold text-slate-200">AI Remediation Diffs</div>
            <p className="text-xs text-slate-500 mt-1">Contextual remediation, secret masking, and exportable audit reports.</p>
          </div>
        </div>
      </section>
    </div>
  );
}
