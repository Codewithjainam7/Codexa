import React, { useState } from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2, AlertTriangle, 
  Code2, Activity, GitBranch, Layers, Check, Copy, ExternalLink,
  ChevronRight, RefreshCw, EyeOff, Bug, Star, FileSearch, ShieldAlert,
  Sliders, Gauge, Fingerprint, Radio
} from "lucide-react";
import AuroraBackground from "./ui/AuroraBackground";
import GlowingEffect from "./ui/GlowingEffect";
import { CanvasText } from "./ui/canvas-text";

export default function LandingView({ onStartAnalysis }) {
  const [activeCodeTab, setActiveCodeTab] = useState("sqli");
  const [copied, setCopied] = useState(false);

  const codeDemos = {
    sqli: {
      title: "auth_controller.ts — SQL Injection Check",
      vulnType: "OWASP A03:2021 — SQL Injection",
      severity: "CRITICAL",
      before: `// ❌ Vulnerable (Raw Concat)
const q = "SELECT * FROM users WHERE id = '" + req.body.userId + "'";
const user = await db.raw(q);`,
      after: `// ✅ Remediated (Parameterized)
const user = await db('users')
  .where({ id: req.body.userId })
  .first();`,
      explanation: "Replaced raw string concatenation with parameterized Knex query builder to neutralize SQL injection attack vectors."
    },
    rce: {
      title: "file_utils.py — Shell Execution Check",
      vulnType: "OWASP A03:2021 — Remote Code Exec",
      severity: "CRITICAL",
      before: `// ❌ Vulnerable (Shell Exec)
import os
os.system(f"ffmpeg -i {user_filename} output.mp4")`,
      after: `// ✅ Remediated (Sandboxed)
import subprocess, shlex
subprocess.run(["ffmpeg", "-i", shlex.quote(user_filename), "output.mp4"], check=True)`,
      explanation: "Used subprocess argument list with shlex.quote sanitization to eliminate shell injection attack surfaces."
    },
    secrets: {
      title: "config.go — Hardcoded Credentials Check",
      vulnType: "CWE-798 — Hardcoded Secrets",
      severity: "HIGH",
      before: `// ❌ Vulnerable (Leaked Secret)
const stripeKey = "SAMPLE_KEY_AKIAIOSFODNN7EXAMPLE";`,
      after: `// ✅ Remediated (Env Variable)
stripeKey := os.Getenv("STRIPE_SECRET_KEY")
if stripeKey == "" {
    log.Fatal("STRIPE_SECRET_KEY is required")
}`,
      explanation: "Masked production secret and refactored code to securely load credentials from environment variables."
    }
  };

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <AuroraBackground className="relative py-12 sm:py-16 font-sans bg-transparent">
      {/* 1. HERO SECTION WITH ROYAL PURPLE & SUNSET CORAL GLASS THEME */}
      <section className="text-center max-w-5xl mx-auto pt-6 sm:pt-12 pb-16 sm:pb-24 px-4 relative z-10">
        {/* Floating VisionOS Glass Chip */}
        <div className="inline-flex items-center space-x-3 px-5 py-2 rounded-full ios-glass-pill text-purple-300 text-xs font-semibold backdrop-blur-3xl mb-8 transform hover:scale-105 transition-all cursor-default">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-rose-400 shadow-[0_0_10px_#f43f5e]" />
          </span>
          <span className="font-display tracking-wide text-slate-200">
            Nvidia Nemotron 550B <span className="text-rose-400 font-bold">&bull;</span> Deterministic AST Engine
          </span>
        </div>
        
        {/* Main Headline with CanvasText */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold font-display text-white tracking-tight leading-[1.12] mb-8">
          Is Your <br className="sm:hidden" />
          <CanvasText
            text="AI-Generated Code"
            colors={[
              "rgba(192, 132, 252, 1)",   // purple-400
              "rgba(168, 85, 247, 0.95)",  // purple-500
              "rgba(244, 63, 94, 0.9)",    // rose-500
              "rgba(251, 113, 133, 0.85)", // rose-400
              "rgba(245, 158, 11, 0.85)",  // amber-500
              "rgba(99, 102, 241, 0.8)",   // indigo-500
              "rgba(56, 189, 248, 0.75)",  // sky-400
              "rgba(255, 255, 255, 0.95)", // white flash
            ]}
            animationSpeed={0.6}
            className="mx-1 my-1"
          />
          <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-slate-100 via-purple-200 to-rose-200">
            Ready for Production?
          </span>
        </h1>

        {/* Subtitle */}
        <p className="text-base sm:text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed font-sans mb-10">
          Codexa audits AI-generated applications, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by verified risk, and computes an explainable <strong className="text-white">Production Readiness Score (0–100)</strong>.
        </p>

        {/* Dual Actions */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-9 py-4 rounded-2xl bg-gradient-to-r from-purple-600 via-rose-500 to-amber-500 text-white font-extrabold text-base flex items-center justify-center space-x-3 shadow-[0_15px_35px_rgba(244,63,94,0.4)] hover:shadow-[0_20px_45px_rgba(244,63,94,0.6)] border border-white/25 transition-all transform hover:scale-105 active:scale-95 cursor-pointer font-display tracking-tight"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5 text-white" />
          </button>
          
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-8 py-4 rounded-2xl ios-glass-pill hover:bg-white/10 text-slate-200 hover:text-white font-semibold text-base transition-all flex items-center justify-center space-x-2.5 shadow-lg backdrop-blur-3xl font-display hover:border-purple-400/40"
          >
            <GitBranch className="w-4 h-4 text-purple-400" />
            <span>View GitHub Source</span>
          </a>
        </div>

        {/* Floating Telemetry Glass Dock */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-4xl mx-auto text-left">
          <div className="p-5 rounded-3xl ios-glass-card shadow-xl hover:border-purple-500/40 transition-all group">
            <div className="flex items-center space-x-2 text-purple-400 mb-2">
              <div className="p-2 rounded-xl bg-purple-500/15 border border-purple-500/20">
                <Shield className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">AST Engine</span>
            </div>
            <div className="text-base font-bold text-white font-display">19+ Security Rules</div>
          </div>

          <div className="p-5 rounded-3xl ios-glass-card shadow-xl hover:border-rose-500/40 transition-all group">
            <div className="flex items-center space-x-2 text-rose-400 mb-2">
              <div className="p-2 rounded-xl bg-rose-500/15 border border-rose-500/20">
                <Cpu className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">AI Cascade</span>
            </div>
            <div className="text-base font-bold text-white font-display">Nemotron 550B</div>
          </div>

          <div className="p-5 rounded-3xl ios-glass-card shadow-xl hover:border-amber-500/40 transition-all group">
            <div className="flex items-center space-x-2 text-amber-400 mb-2">
              <div className="p-2 rounded-xl bg-amber-500/15 border border-amber-500/20">
                <Zap className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">Benchmark</span>
            </div>
            <div className="text-base font-bold text-slate-200 font-display">&lt; 100ms Parsing</div>
          </div>

          <div className="p-5 rounded-3xl ios-glass-card shadow-xl hover:border-indigo-500/40 transition-all group">
            <div className="flex items-center space-x-2 text-indigo-400 mb-2">
              <div className="p-2 rounded-xl bg-indigo-500/15 border border-indigo-500/20">
                <Lock className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">Sandboxing</span>
            </div>
            <div className="text-base font-bold text-slate-200 font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-6xl mx-auto px-4 my-12 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/15 to-transparent" />
      </div>

      {/* 2. MACOS / IOS 26 LIQUID GLASS REMEDIATION TERMINAL SECTION */}
      <section className="max-w-4xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 px-2">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-lg bg-purple-500/20 border border-purple-400/30">
              <Terminal className="w-4 h-4 text-purple-300" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-300">
              Interactive Live Remediation Preview
            </h2>
          </div>
          <span className="w-fit text-[11px] font-mono text-purple-300 bg-purple-500/15 px-3 py-1 rounded-full border border-purple-500/30 backdrop-blur-xl shadow-sm">
            Realtime AST Diff
          </span>
        </div>

        {/* Frosted Glass Window */}
        <div className="rounded-[32px] ios-glass p-1 shadow-[0_30px_70px_rgba(0,0,0,0.6)] overflow-hidden">
          {/* Window Header */}
          <div className="flex items-center justify-between px-5 py-4 bg-white/[0.03] border-b border-white/10">
            <div className="flex items-center space-x-2.5">
              <span className="w-3.5 h-3.5 rounded-full bg-rose-500/80 border border-rose-400/50 shadow-sm" />
              <span className="w-3.5 h-3.5 rounded-full bg-amber-500/80 border border-amber-400/50 shadow-sm" />
              <span className="w-3.5 h-3.5 rounded-full bg-emerald-500/80 border border-emerald-400/50 shadow-sm" />
              <span className="ml-2 text-xs font-mono text-slate-300 font-medium hidden sm:inline">
                {codeDemos[activeCodeTab].title}
              </span>
            </div>

            {/* Segmented Switcher Capsule */}
            <div className="flex items-center p-1 bg-black/40 border border-white/10 rounded-xl backdrop-blur-xl">
              {Object.keys(codeDemos).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveCodeTab(tab)}
                  className={`px-3 py-1 rounded-lg text-xs font-mono uppercase font-bold transition-all ${
                    activeCodeTab === tab
                      ? "bg-purple-500/30 text-purple-200 border border-purple-400/40 shadow-sm"
                      : "text-slate-400 hover:text-white"
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>
          </div>

          {/* Diff Grid */}
          <div className="p-4 sm:p-6 grid grid-cols-1 md:grid-cols-2 gap-4 font-mono text-xs">
            {/* Before (Vulnerable) */}
            <div className="space-y-3 rounded-2xl bg-rose-950/20 border border-rose-500/25 p-4 backdrop-blur-xl">
              <div className="flex items-center justify-between text-rose-400 font-bold pb-2 border-b border-rose-500/20">
                <span className="flex items-center space-x-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-400" />
                  <span>Vulnerable (Before)</span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-rose-500/15 border border-rose-500/30 font-bold">
                  {codeDemos[activeCodeTab].severity}
                </span>
              </div>
              <pre className="text-rose-200/90 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1">
                {codeDemos[activeCodeTab].before}
              </pre>
            </div>

            {/* After (Remediated) */}
            <div className="space-y-3 rounded-2xl bg-emerald-950/20 border border-emerald-500/25 p-4 backdrop-blur-xl">
              <div className="flex items-center justify-between text-emerald-400 font-bold pb-2 border-b border-emerald-500/20">
                <span className="flex items-center space-x-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Secure (After)</span>
                </span>
                <button
                  onClick={() => handleCopy(codeDemos[activeCodeTab].after)}
                  className="flex items-center space-x-1 text-[10px] text-emerald-300 hover:text-white bg-emerald-500/15 px-2.5 py-0.5 rounded-full border border-emerald-500/30 transition-colors cursor-pointer"
                >
                  {copied ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                  <span>{copied ? "Copied" : "Copy Fix"}</span>
                </button>
              </div>
              <pre className="text-emerald-200/90 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1">
                {codeDemos[activeCodeTab].after}
              </pre>
            </div>
          </div>

          {/* Explanation Footer */}
          <div className="px-5 py-4 bg-white/[0.02] border-t border-white/10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs text-slate-300">
            <span className="flex items-center space-x-2">
              <Sparkles className="w-3.5 h-3.5 text-purple-400 flex-shrink-0" />
              <span>{codeDemos[activeCodeTab].explanation}</span>
            </span>
            <span className="text-purple-400 font-mono font-bold">0 Hallucination</span>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-6xl mx-auto px-4 my-12 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/15 to-transparent" />
      </div>

      {/* 3. 5-LAYER VISIONOS BENTO GRID SECTION */}
      <section className="max-w-6xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-8">
        <div className="flex items-center justify-between px-2">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-lg bg-purple-500/20 border border-purple-400/30">
              <Sparkles className="w-4 h-4 text-purple-300" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-300">
              Engine Architecture &amp; Capabilities
            </h2>
          </div>
          <span className="text-xs font-mono text-slate-400">5-Layer Security Shield</span>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-6 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-[30px] ios-glass p-2 sm:p-3 group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-[22px] p-6 bg-[#0f0728]/60 border border-white/10 backdrop-blur-2xl">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-2xl border border-purple-500/30 bg-purple-500/15 p-3 text-purple-300 shadow-inner">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">OWASP Top 10 SAST</h3>
                    <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and deserialization flaws.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-3 border-t border-white/10 text-[10px] font-mono">
                  <span className="px-2.5 py-0.5 rounded-full bg-purple-500/15 text-purple-300 border border-purple-500/30">SQLi Shield</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-rose-500/15 text-rose-300 border border-rose-500/30">RCE Blocked</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-indigo-500/15 text-indigo-300 border border-indigo-500/30">XSS Sanitizer</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-amber-500/15 text-amber-300 border border-amber-500/30">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 4, Row Span 2) */}
          <li className="md:col-span-4 md:row-span-2 list-none">
            <div className="relative h-full rounded-[30px] ios-glass p-2 sm:p-3 group transition-all">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-[22px] p-6 bg-[#0f0728]/60 border border-white/10 backdrop-blur-2xl">
                <div className="space-y-3.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-2xl border border-emerald-500/30 bg-emerald-500/15 p-3 text-emerald-300 shadow-inner">
                      <FileCode className="h-5 w-5" />
                    </div>
                    <span className="text-[10px] font-mono font-bold text-emerald-400 bg-emerald-500/15 px-3 py-1 rounded-full border border-emerald-500/30">
                      LIVE PATCHING
                    </span>
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Before &amp; After Fix Diffs</h3>
                    <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
                      Side-by-side IDE terminal diff comparison with automatic secret masking and copyable secure fixes.
                    </p>
                  </div>
                </div>

                {/* Mini IDE Terminal Window */}
                <div className="my-auto rounded-2xl bg-black/60 border border-white/10 p-4 font-mono text-[11px] space-y-3 shadow-inner backdrop-blur-2xl">
                  <div className="flex items-center justify-between pb-2 border-b border-white/10 text-[10px] text-slate-500">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2.5 h-2.5 rounded-full bg-rose-500/80" />
                      <span className="w-2.5 h-2.5 rounded-full bg-amber-500/80" />
                      <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/80" />
                      <span className="ml-1 text-slate-300 font-sans">auth_handler.ts</span>
                    </div>
                    <span className="text-emerald-400 font-bold">Auto-Fixed</span>
                  </div>
                  <div className="space-y-2 text-xs">
                    <div className="text-rose-400 bg-rose-950/40 px-3 py-1.5 rounded-xl border-l-2 border-rose-500 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-emerald-400 bg-emerald-950/40 px-3 py-1.5 rounded-xl border-l-2 border-emerald-500 overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-white/10 flex items-center justify-between text-[11px] text-slate-400 font-mono">
                  <span className="flex items-center space-x-1.5 text-slate-300">
                    <CheckCircle2 className="w-3.5 h-3.5 text-purple-400" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-purple-400 font-bold">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-[30px] ios-glass p-2 sm:p-3 group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-[22px] p-6 bg-[#0f0728]/60 border border-white/10 backdrop-blur-2xl">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-2xl border border-rose-500/30 bg-rose-500/15 p-3 text-rose-300 shadow-inner">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-3 border-t border-white/10 text-[10px] font-mono">
                  <span className="px-2.5 py-0.5 rounded-full bg-rose-500/20 text-rose-300 border border-rose-500/30">Nemotron 550B</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-purple-500/20 text-purple-300 border border-purple-500/30">Inkling Small</span>
                  <span className="px-2.5 py-0.5 rounded-full bg-white/10 text-slate-300 border border-white/15">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-[30px] ios-glass p-2 sm:p-3 group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-[22px] p-6 bg-[#0f0728]/60 border border-white/10 backdrop-blur-2xl">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-2xl border border-amber-500/30 bg-amber-500/15 p-3 text-amber-300 shadow-inner">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Readiness Score (0–100)</h3>
                    <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                <div className="space-y-2.5 pt-3 border-t border-white/10">
                  <div className="flex items-center justify-between text-[10px] font-mono text-slate-400">
                    <span>Readiness Weighting</span>
                    <span className="text-amber-400 font-bold">100% Deterministic</span>
                  </div>
                  <div className="h-2.5 w-full bg-black/60 rounded-full overflow-hidden flex border border-white/10 p-0.5">
                    <div className="bg-purple-500 h-full rounded-full w-[60%]" title="Security 60%" />
                    <div className="bg-rose-500 h-full rounded-full w-[25%]" title="Quality 25%" />
                    <div className="bg-amber-500 h-full rounded-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-[30px] ios-glass p-2 sm:p-3 group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-[22px] p-6 bg-[#0f0728]/60 border border-white/10 backdrop-blur-2xl">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-2xl border border-indigo-500/30 bg-indigo-500/15 p-3 text-indigo-300 shadow-inner">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Zero-Execution Sandbox</h3>
                    <p className="text-xs text-slate-400 mt-1.5 leading-relaxed">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-3 border-t border-white/10 text-[10px] font-mono text-slate-400">
                  <span className="flex items-center space-x-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-6xl mx-auto px-4 my-12 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/15 to-transparent" />
      </div>

      {/* 4. SCAN PIPELINE ARCHITECTURE */}
      <section className="max-w-5xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-10">
        <div className="text-center space-y-3">
          <div className="inline-flex items-center space-x-2 text-xs font-mono text-purple-300 font-bold uppercase tracking-wider ios-glass-pill px-4 py-1.5 rounded-full">
            <Layers className="w-3.5 h-3.5 text-purple-400" />
            <span>Automated Analysis Pipeline</span>
          </div>
          <h2 className="text-2xl sm:text-4xl font-extrabold font-display text-white">
            How Codexa Audits Your Code
          </h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 pt-2">
          {/* Stage 1 */}
          <div className="p-6 rounded-[28px] ios-glass-card hover:border-purple-500/50 transition-all group shadow-xl">
            <div className="w-10 h-10 rounded-2xl bg-purple-500/15 border border-purple-400/30 flex items-center justify-center text-purple-300 font-bold font-mono text-xs mb-4">
              01
            </div>
            <h4 className="text-base font-bold text-white font-display mb-1.5">Sandboxed Ingestion</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Safe extraction of ZIP archives or GitHub repos with Zip Slip, Bomb &amp; Depth verification.
            </p>
          </div>

          {/* Stage 2 */}
          <div className="p-6 rounded-[28px] ios-glass-card hover:border-rose-500/50 transition-all group shadow-xl">
            <div className="w-10 h-10 rounded-2xl bg-rose-500/15 border border-rose-400/30 flex items-center justify-center text-rose-300 font-bold font-mono text-xs mb-4">
              02
            </div>
            <h4 className="text-base font-bold text-white font-display mb-1.5">Deterministic AST Scan</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              JavaParser &amp; Multi-Lang regex pattern analyzers detect 19+ critical vulnerability rules.
            </p>
          </div>

          {/* Stage 3 */}
          <div className="p-6 rounded-[28px] ios-glass-card hover:border-amber-500/50 transition-all group shadow-xl">
            <div className="w-10 h-10 rounded-2xl bg-amber-500/15 border border-amber-400/30 flex items-center justify-center text-amber-300 font-bold font-mono text-xs mb-4">
              03
            </div>
            <h4 className="text-base font-bold text-white font-display mb-1.5">Neural Cascade</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Nvidia Nemotron 550B generates explainable security insights and context-aware code patches.
            </p>
          </div>

          {/* Stage 4 */}
          <div className="p-6 rounded-[28px] ios-glass-card hover:border-indigo-500/50 transition-all group shadow-xl">
            <div className="w-10 h-10 rounded-2xl bg-indigo-500/15 border border-indigo-400/30 flex items-center justify-center text-indigo-300 font-bold font-mono text-xs mb-4">
              04
            </div>
            <h4 className="text-base font-bold text-white font-display mb-1.5">Readiness Index</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Mathematical scoring formula produces clear 0–100 production-readiness rating &amp; report.
            </p>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-6xl mx-auto px-4 my-12 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/15 to-transparent" />
      </div>

      {/* 5. BOTTOM CTA LAUNCH BANNER */}
      <section className="max-w-4xl mx-auto py-8 sm:py-16 px-4 relative z-10">
        <div className="relative rounded-[36px] ios-glass p-8 sm:p-14 text-center space-y-6 overflow-hidden shadow-[0_30px_80px_rgba(0,0,0,0.7)]">
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-[500px] h-60 bg-rose-500/20 rounded-full blur-3xl pointer-events-none" />
          
          <div className="relative space-y-3">
            <h3 className="text-3xl sm:text-5xl font-extrabold font-display text-white">
              Ready to Audit Your Codebase?
            </h3>
            <p className="text-sm sm:text-base text-slate-300 max-w-xl mx-auto font-sans leading-relaxed">
              Scan your repository in seconds. Zero bytecode execution, 100% explainable security and production readiness score.
            </p>
          </div>

          <div className="pt-3 flex flex-col sm:flex-row items-center justify-center gap-4">
            <button
              onClick={onStartAnalysis}
              className="w-full sm:w-auto px-9 py-4 rounded-2xl bg-gradient-to-r from-purple-600 via-rose-500 to-amber-500 text-white font-extrabold text-sm flex items-center justify-center space-x-2.5 shadow-[0_15px_35px_rgba(244,63,94,0.4)] hover:shadow-[0_20px_45px_rgba(244,63,94,0.6)] border border-white/25 transition-all cursor-pointer font-display transform hover:scale-105 active:scale-95"
            >
              <span>Launch New Audit</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </button>
            <a
              href="https://github.com/Codewithjainam7/Codexa"
              target="_blank"
              rel="noreferrer"
              className="w-full sm:w-auto px-8 py-4 rounded-2xl ios-glass-pill hover:bg-white/10 text-slate-200 border border-white/15 text-sm font-semibold transition-all flex items-center justify-center space-x-2 font-display"
            >
              <GitBranch className="w-4 h-4 text-purple-400" />
              <span>Star on GitHub</span>
            </a>
          </div>
        </div>
      </section>
    </AuroraBackground>
  );
}
