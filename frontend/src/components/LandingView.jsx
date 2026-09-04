"use client";

import React from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2, AlertTriangle, Code2, Activity
} from "lucide-react";
import DottedGlowBackground from "./ui/DottedGlowBackground";
import GlowingEffect from "./ui/GlowingEffect";
import { FlipWords } from "./ui/flip-words";

export default function LandingView({ onStartAnalysis }) {
  const codeTargets = [
    "AI-Generated Code",
    "Next.js & React Apps",
    "Node & Python APIs",
    "Enterprise Backends",
    "Cloud Microservices"
  ];

  return (
    <div className="relative space-y-16 py-6 sm:py-8 font-sans">
      {/* Background Dotted Matrix */}
      <DottedGlowBackground
        className="opacity-40"
        gap={20}
        radius={1.4}
        colorDarkVar="#1e293b"
        glowColorDarkVar="#10b981"
        speedScale={0.8}
      />

      {/* Hero Section */}
      <section className="text-center max-w-4xl mx-auto space-y-6 relative z-10 pt-2">
        <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold shadow-sm backdrop-blur-md">
          <Shield className="w-3.5 h-3.5 animate-pulse" />
          <span>Deterministic AST Rules + Nvidia Nemotron 550B Audit</span>
        </div>
        
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold font-display text-white tracking-tight leading-[1.12]">
          Is Your <br className="sm:hidden" />
          <FlipWords
            words={codeTargets}
            className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400 font-extrabold px-1.5 inline-block drop-shadow-[0_0_25px_rgba(52,211,153,0.35)]"
          />
          <br />
          <span className="text-white">
            Ready for Production?
          </span>
        </h1>

        <p className="text-base sm:text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed">
          Codexa audits AI-generated applications, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by real risk, and computes an explainable <strong>Production Readiness Score</strong>.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-2">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-base flex items-center justify-center space-x-2 shadow-xl shadow-emerald-500/25 transition-all transform hover:-translate-y-0.5 active:scale-95 cursor-pointer font-display tracking-tight"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5" />
          </button>
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-slate-900/90 hover:bg-slate-800 text-slate-200 border border-slate-800 font-semibold text-base transition-colors flex items-center justify-center space-x-2 shadow-lg font-display"
          >
            <span>View GitHub Source</span>
          </a>
        </div>

        {/* Quick Capabilities Strip */}
        <div className="pt-4 grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-3xl mx-auto text-left">
          <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/80 backdrop-blur-sm">
            <div className="text-[11px] text-slate-400 font-mono">AST Engine</div>
            <div className="text-sm font-bold text-white font-display">19+ Security Rules</div>
          </div>
          <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/80 backdrop-blur-sm">
            <div className="text-[11px] text-slate-400 font-mono">AI Models</div>
            <div className="text-sm font-bold text-emerald-400 font-display">Nemotron 550B</div>
          </div>
          <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/80 backdrop-blur-sm">
            <div className="text-[11px] text-slate-400 font-mono">Benchmark</div>
            <div className="text-sm font-bold text-teal-300 font-display">&lt; 100ms Parsing</div>
          </div>
          <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/80 backdrop-blur-sm">
            <div className="text-[11px] text-slate-400 font-mono">Sandboxing</div>
            <div className="text-sm font-bold text-cyan-300 font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Rich Aceternity Bento Grid (No Empty Spaces) */}
      <section className="space-y-5 relative z-10">
        <div className="flex items-center space-x-2 px-1">
          <Sparkles className="w-4 h-4 text-emerald-400" />
          <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-400">
            Engine Architecture &amp; Capabilities
          </h2>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-4 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/60 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">OWASP Top 10 Security</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and cryptographic flaws.
                    </p>
                  </div>
                </div>

                {/* Rich interactive visual preview */}
                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-slate-800/60 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-300 border border-rose-500/20">SQLi Shield</span>
                  <span className="px-2 py-0.5 rounded-full bg-orange-500/10 text-orange-300 border border-orange-500/20">RCE Blocked</span>
                  <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">XSS Sanitizer</span>
                  <span className="px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-300 border border-blue-500/20">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 4, Row Span 2) */}
          <li className="md:col-span-4 md:row-span-2 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/60 border border-slate-800/60">
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner">
                      <FileCode className="h-5 w-5" />
                    </div>
                    <span className="text-[10px] font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                      LIVE PATCHING
                    </span>
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Before &amp; After Fix Diffs</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Side-by-side IDE terminal diff comparison with automatic secret masking and copyable secure fixes.
                    </p>
                  </div>
                </div>

                {/* Mini IDE Terminal Code Window taking up vertical space */}
                <div className="my-auto rounded-xl bg-slate-950 border border-slate-800 p-3 font-mono text-[11px] space-y-2 shadow-inner">
                  <div className="flex items-center justify-between pb-1.5 border-b border-slate-800 text-[10px] text-slate-500">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-rose-500/60" />
                      <span className="w-2 h-2 rounded-full bg-amber-500/60" />
                      <span className="w-2 h-2 rounded-full bg-emerald-500/60" />
                      <span className="ml-1 text-slate-400 font-sans">auth_handler.ts</span>
                    </div>
                    <span className="text-emerald-400 font-bold">Auto-Fixed</span>
                  </div>
                  <div className="space-y-1 text-xs">
                    <div className="text-rose-400/90 bg-rose-950/30 px-2 py-1 rounded border-l-2 border-rose-500 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-emerald-400 bg-emerald-950/30 px-2 py-1 rounded border-l-2 border-emerald-500 overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-800/60 flex items-center justify-between text-[11px] text-slate-400 font-mono">
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-emerald-400 font-bold">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/60 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-slate-800/60 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Nemotron 550B</span>
                  <span className="px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/20">Inkling Small</span>
                  <span className="px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-300 border border-purple-500/20">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/60 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Readiness Scoring (0–100)</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                {/* Mini readiness meter */}
                <div className="space-y-1.5 pt-2 border-t border-slate-800/60">
                  <div className="flex items-center justify-between text-[10px] font-mono text-slate-400">
                    <span>Readiness Weighting</span>
                    <span className="text-emerald-400 font-bold">100% Deterministic</span>
                  </div>
                  <div className="h-2 w-full bg-slate-950 rounded-full overflow-hidden flex">
                    <div className="bg-emerald-500 h-full w-[60%]" title="Security 60%" />
                    <div className="bg-teal-400 h-full w-[25%]" title="Quality 25%" />
                    <div className="bg-blue-400 h-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/60 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Zero-Execution Sandboxing</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 text-[10px] font-mono text-slate-400">
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>
    </div>
  );
}
