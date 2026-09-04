"use client";
import React, { useState } from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2, AlertTriangle, 
  Code2, Activity, GitBranch, Layers, Check, Copy, ExternalLink,
  ChevronRight, RefreshCw, EyeOff, Bug, Star, FileSearch, ShieldAlert,
  Sliders, Gauge, Fingerprint, Radio, Play, CheckCircle
} from "lucide-react";
import FlickeringGrid from "./ui/FlickeringGrid";
import GlowingEffect from "./ui/GlowingEffect";
import { CanvasText } from "./ui/canvas-text";

export default function LandingView({ onStartAnalysis }) {
  const [activeCodeTab, setActiveCodeTab] = useState("sqli");
  const [copied, setCopied] = useState(false);
  const [isScanningActive, setIsScanningActive] = useState(true);

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
    <div className="relative min-h-screen py-10 sm:py-16 font-sans bg-transparent">
      {/* 1. Global Viewport Flickering Grid Background */}
      <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden">
        <FlickeringGrid
          className="w-full h-full"
          squareSize={4}
          gridGap={6}
          color="#60A5FA"
          maxOpacity={0.35}
          flickerChance={0.08}
        />
      </div>

      {/* ========================================================================= */}
      {/* 1. HERO SECTION (COMMAND CENTER & LIVE SCANNER)                           */}
      {/* ========================================================================= */}
      <section className="text-center max-w-5xl mx-auto pt-6 sm:pt-14 pb-20 sm:pb-28 px-4 relative z-10">
        {/* Spotlight Flickering Grid right behind the Hero */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] sm:w-[900px] sm:h-[900px] pointer-events-none -z-10 overflow-hidden opacity-90">
          <FlickeringGrid
            className="w-full h-full [mask-image:radial-gradient(450px_circle_at_center,white,transparent)]"
            squareSize={4}
            gridGap={6}
            color="#60A5FA"
            maxOpacity={0.65}
            flickerChance={0.12}
            width={900}
            height={900}
          />
        </div>

        {/* High-End Security Status Beacon Badge */}
        <div className="inline-flex items-center space-x-2.5 px-4 py-1.5 rounded-full surface-pill text-xs font-semibold mb-8 transform hover:scale-[1.02] transition-all duration-300 cursor-default">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-sky-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-sky-400" />
          </span>
          <span className="font-mono text-[11px] tracking-wide text-slate-300">
            Deterministic AST Engine <span className="text-sky-400 font-bold mx-1">&bull;</span> Nvidia Nemotron 550B Audit
          </span>
        </div>
        
        {/* Main Headline with CanvasText */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold font-display text-white tracking-tight sm:tracking-tighter leading-[1.08] mb-6 sm:mb-8">
          Audit &amp; Secure <br className="sm:hidden" />
          <CanvasText
            text="AI-Generated Code"
            colors={[
              "rgba(56, 189, 248, 1)",      // Sky 400
              "rgba(14, 165, 233, 0.95)",   // Sky 500
              "rgba(255, 255, 255, 0.95)",  // Pure White
              "rgba(129, 140, 248, 0.9)",   // Indigo 400
              "rgba(56, 189, 248, 0.85)",   // Sky 400
              "rgba(255, 255, 255, 0.9)",   // White
            ]}
            animationSpeed={0.5}
            className="mx-1 my-1"
          />
          <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-white via-slate-200 to-slate-400">
            Before Production.
          </span>
        </h1>

        {/* Subtitle */}
        <p className="text-base sm:text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed font-sans mb-10 font-normal">
          Codexa inspects AI-generated repositories, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by verified risk, and computes an explainable <strong className="text-slate-100 font-semibold">Production Readiness Score (0–100)</strong>.
        </p>

        {/* Dual Primary Action Triggers */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 mb-16">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-gradient-to-r from-sky-400 via-sky-500 to-blue-600 text-slate-950 font-bold text-sm flex items-center justify-center space-x-2.5 shadow-[0_0_25px_rgba(56,189,248,0.35)] hover:shadow-[0_0_35px_rgba(56,189,248,0.5)] border border-white/20 transition-all duration-200 transform hover:-translate-y-0.5 active:translate-y-0 cursor-pointer font-display tracking-tight"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-4 h-4 text-slate-950" />
          </button>
          
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-7 py-3.5 rounded-xl surface-pill hover:bg-white/[0.06] text-slate-300 hover:text-white font-semibold text-sm transition-all duration-200 flex items-center justify-center space-x-2 font-display hover:border-white/20"
          >
            <GitBranch className="w-4 h-4 text-slate-400" />
            <span>View GitHub Source</span>
          </a>
        </div>

        {/* Live Interactive Hero Code Inspector Preview */}
        <div className="mb-14 rounded-2xl surface-elevated-2 p-3 sm:p-5 text-left relative overflow-hidden group">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3.5 border-b border-white/[0.06]">
            <div className="flex items-center space-x-2.5">
              <span className="w-2.5 h-2.5 rounded-full bg-rose-500/80" />
              <span className="w-2.5 h-2.5 rounded-full bg-amber-500/80" />
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/80" />
              <span className="ml-2 text-xs font-mono text-slate-300 font-semibold">
                Codexa Neural AST Inspector — Live Telemetry
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-[10px] font-mono font-medium bg-sky-500/10 text-sky-400 px-2.5 py-0.5 rounded-md border border-sky-500/20">
                100% AST AST PARSE
              </span>
              <span className="text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 px-2.5 py-0.5 rounded-md border border-emerald-500/20">
                0 BYTECODE EXEC
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 pt-4 font-mono text-xs">
            {/* Telemetry Stat 1 - Vulnerability Guard (Amber / Sky Accent) */}
            <div className="p-4 rounded-xl surface-panel border border-white/[0.04] space-y-1.5">
              <div className="text-[11px] text-amber-400 font-semibold flex items-center justify-between">
                <span>Vulnerability Guard</span>
                <ShieldCheck className="w-4 h-4 text-amber-400" />
              </div>
              <div className="text-white font-bold text-sm">19+ OWASP Rules Active</div>
              <div className="text-[10px] text-slate-400">SQLi, RCE, SSRF, Deserialization, CSRF</div>
            </div>

            {/* Telemetry Stat 2 - Readiness Score (Emerald Accent) */}
            <div className="p-4 rounded-xl surface-panel border border-white/[0.04] space-y-1.5">
              <div className="text-[11px] text-emerald-400 font-semibold flex items-center justify-between">
                <span>Readiness Score</span>
                <Zap className="w-4 h-4 text-emerald-400" />
              </div>
              <div className="text-white font-bold text-sm">98.5 / 100 Grade A</div>
              <div className="text-[10px] text-slate-400">Security 60% &bull; Quality 25% &bull; Ops 15%</div>
            </div>

            {/* Telemetry Stat 3 - AI Cascader (Indigo / Violet Accent) */}
            <div className="p-4 rounded-xl surface-panel border border-white/[0.04] space-y-1.5">
              <div className="text-[11px] text-indigo-400 font-semibold flex items-center justify-between">
                <span>AI Cascader</span>
                <Cpu className="w-4 h-4 text-indigo-400" />
              </div>
              <div className="text-white font-bold text-sm">Nvidia Nemotron 550B</div>
              <div className="text-[10px] text-slate-400">Contextual patches &amp; zero hallucination</div>
            </div>
          </div>
        </div>

        {/* Floating Telemetry Metric Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5 max-w-4xl mx-auto text-left">
          <div className="p-4 rounded-2xl surface-elevated-1 hover:border-sky-500/30 transition-all duration-300 group transform hover:-translate-y-0.5">
            <div className="flex items-center space-x-2 text-sky-400 mb-1.5">
              <div className="p-1.5 rounded-lg bg-sky-500/10 border border-sky-500/20">
                <Shield className="w-3.5 h-3.5 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">AST Engine</span>
            </div>
            <div className="text-sm font-bold text-white font-display">19+ Security Rules</div>
          </div>

          <div className="p-4 rounded-2xl surface-elevated-1 hover:border-indigo-500/30 transition-all duration-300 group transform hover:-translate-y-0.5">
            <div className="flex items-center space-x-2 text-indigo-400 mb-1.5">
              <div className="p-1.5 rounded-lg bg-indigo-500/10 border border-indigo-500/20">
                <Cpu className="w-3.5 h-3.5 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">AI Cascade</span>
            </div>
            <div className="text-sm font-bold text-white font-display">Nemotron 550B</div>
          </div>

          <div className="p-4 rounded-2xl surface-elevated-1 hover:border-emerald-500/30 transition-all duration-300 group transform hover:-translate-y-0.5">
            <div className="flex items-center space-x-2 text-emerald-400 mb-1.5">
              <div className="p-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
                <Zap className="w-3.5 h-3.5 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">Benchmark</span>
            </div>
            <div className="text-sm font-bold text-white font-display">&lt; 100ms Parsing</div>
          </div>

          <div className="p-4 rounded-2xl surface-elevated-1 hover:border-amber-500/30 transition-all duration-300 group transform hover:-translate-y-0.5">
            <div className="flex items-center space-x-2 text-amber-400 mb-1.5">
              <div className="p-1.5 rounded-lg bg-amber-500/10 border border-amber-500/20">
                <Lock className="w-3.5 h-3.5 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-400 font-mono">Sandboxing</span>
            </div>
            <div className="text-sm font-bold text-white font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Elegant Hairline Divider with Spacing */}
      <div className="max-w-5xl mx-auto px-4 my-16 sm:my-24">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 2. REMEDIATION TERMINAL SECTION                                           */}
      {/* ========================================================================= */}
      <section className="max-w-4xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 px-1">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-md bg-white/[0.05] border border-white/[0.08]">
              <Terminal className="w-4 h-4 text-sky-400" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-300">
              Interactive Live Remediation Preview
            </h2>
          </div>
          <span className="w-fit text-[11px] font-mono text-sky-400 bg-sky-500/10 px-3 py-0.5 rounded-md border border-sky-500/20">
            Realtime AST Diff
          </span>
        </div>

        {/* Realistic Terminal Shell */}
        <div className="rounded-2xl surface-elevated-2 p-1 overflow-hidden">
          {/* Window Header */}
          <div className="flex items-center justify-between px-5 py-3.5 bg-black/40 border-b border-white/[0.06]">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-rose-500/80 border border-rose-400/40" />
              <span className="w-3 h-3 rounded-full bg-amber-500/80 border border-amber-400/40" />
              <span className="w-3 h-3 rounded-full bg-emerald-500/80 border border-emerald-400/40" />
              <span className="ml-3 text-xs font-mono text-slate-400 font-medium hidden sm:inline">
                {codeDemos[activeCodeTab].title}
              </span>
            </div>

            {/* Segmented Switcher Capsule */}
            <div className="flex items-center p-0.5 bg-black/60 border border-white/[0.08] rounded-lg">
              {Object.keys(codeDemos).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveCodeTab(tab)}
                  className={`px-3 py-1 rounded-md text-xs font-mono uppercase font-semibold transition-all duration-150 ${
                    activeCodeTab === tab
                      ? "bg-sky-500/20 text-sky-400 border border-sky-500/30 shadow-sm"
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
            <div className="space-y-3 rounded-xl bg-rose-950/15 border border-rose-500/20 p-4">
              <div className="flex items-center justify-between text-rose-400 font-bold pb-2 border-b border-rose-500/15">
                <span className="flex items-center space-x-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-400" />
                  <span>Vulnerable (Before)</span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-md bg-rose-500/15 border border-rose-500/25 font-bold">
                  {codeDemos[activeCodeTab].severity}
                </span>
              </div>
              <pre className="text-rose-200/90 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1">
                {codeDemos[activeCodeTab].before}
              </pre>
            </div>

            {/* After (Remediated) */}
            <div className="space-y-3 rounded-xl bg-emerald-950/15 border border-emerald-500/20 p-4">
              <div className="flex items-center justify-between text-emerald-400 font-bold pb-2 border-b border-emerald-500/15">
                <span className="flex items-center space-x-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Secure (After)</span>
                </span>
                <button
                  onClick={() => handleCopy(codeDemos[activeCodeTab].after)}
                  className="flex items-center space-x-1 text-[10px] text-emerald-300 hover:text-white bg-emerald-500/15 px-2.5 py-0.5 rounded-md border border-emerald-500/25 transition-colors cursor-pointer"
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
          <div className="px-5 py-3.5 bg-black/30 border-t border-white/[0.06] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs text-slate-400">
            <span className="flex items-center space-x-2">
              <Sparkles className="w-3.5 h-3.5 text-sky-400 flex-shrink-0" />
              <span>{codeDemos[activeCodeTab].explanation}</span>
            </span>
            <span className="text-sky-400 font-mono font-medium text-[11px]">0 Hallucination</span>
          </div>
        </div>
      </section>

      {/* Elegant Hairline Divider with Spacing */}
      <div className="max-w-5xl mx-auto px-4 my-16 sm:my-24">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 3. 5-LAYER SECURITY BENTO GRID SECTION (ASYMMETRICAL LAYOUT)               */}
      {/* ========================================================================= */}
      <section className="max-w-6xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-8">
        <div className="flex items-center justify-between px-1">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-md bg-white/[0.05] border border-white/[0.08]">
              <Sparkles className="w-4 h-4 text-sky-400" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-300">
              Engine Architecture &amp; Capabilities
            </h2>
          </div>
          <span className="text-xs font-mono text-slate-500">5-Layer Security Shield</span>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-5 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 7) - Flagship Feature */}
          <li className="md:col-span-7 list-none">
            <div className="relative h-full rounded-2xl surface-elevated-1 p-1.5 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-black/40 border border-white/[0.04]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-sky-500/20 bg-sky-500/10 p-2.5 text-sky-400">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">OWASP Top 10 SAST</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and deserialization flaws.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 pt-4 border-t border-white/[0.06] text-[11px] font-mono">
                  <span className="px-2.5 py-1 rounded-md bg-sky-500/10 text-sky-300 border border-sky-500/20">SQLi Shield</span>
                  <span className="px-2.5 py-1 rounded-md bg-rose-500/10 text-rose-300 border border-rose-500/20">RCE Blocked</span>
                  <span className="px-2.5 py-1 rounded-md bg-amber-500/10 text-amber-300 border border-amber-500/20">XSS Sanitizer</span>
                  <span className="px-2.5 py-1 rounded-md bg-slate-800/60 text-slate-300 border border-white/10">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 5, Row Span 2) */}
          <li className="md:col-span-5 md:row-span-2 list-none">
            <div className="relative h-full rounded-2xl surface-elevated-2 p-1.5 group transition-all duration-300">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-black/50 border border-white/[0.04]">
                <div className="space-y-3.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-xl border border-emerald-500/20 bg-emerald-500/10 p-2.5 text-emerald-400">
                      <FileCode className="h-5 w-5" />
                    </div>
                    <span className="text-[10px] font-mono font-semibold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-md border border-emerald-500/20">
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

                {/* Mini IDE Terminal Window */}
                <div className="my-auto rounded-xl bg-black/80 border border-white/[0.08] p-3.5 font-mono text-[11px] space-y-2.5">
                  <div className="flex items-center justify-between pb-2 border-b border-white/[0.06] text-[10px] text-slate-500">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-rose-500/80" />
                      <span className="w-2 h-2 rounded-full bg-amber-500/80" />
                      <span className="w-2 h-2 rounded-full bg-emerald-500/80" />
                      <span className="ml-1 text-slate-400 font-sans">auth_handler.ts</span>
                    </div>
                    <span className="text-emerald-400 font-medium">Auto-Fixed</span>
                  </div>
                  <div className="space-y-1.5 text-xs">
                    <div className="text-rose-400 bg-rose-950/30 px-2.5 py-1.5 rounded-lg border-l-2 border-rose-500 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-emerald-400 bg-emerald-950/30 px-2.5 py-1.5 rounded-lg border-l-2 border-emerald-500 overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-3.5 border-t border-white/[0.06] flex items-center justify-between text-[11px] text-slate-400 font-mono">
                  <span className="flex items-center space-x-1.5 text-slate-300">
                    <CheckCircle2 className="w-3.5 h-3.5 text-sky-400" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-sky-400 font-medium">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 7) */}
          <li className="md:col-span-7 list-none">
            <div className="relative h-full rounded-2xl surface-elevated-1 p-1.5 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-black/40 border border-white/[0.04]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-indigo-500/20 bg-indigo-500/10 p-2.5 text-indigo-400">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 pt-4 border-t border-white/[0.06] text-[11px] font-mono">
                  <span className="px-2.5 py-1 rounded-md bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">Nemotron 550B</span>
                  <span className="px-2.5 py-1 rounded-md bg-sky-500/10 text-sky-300 border border-sky-500/20">Inkling Small</span>
                  <span className="px-2.5 py-1 rounded-md bg-slate-800/60 text-slate-300 border border-white/10">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 6) */}
          <li className="md:col-span-6 list-none">
            <div className="relative h-full rounded-2xl surface-elevated-1 p-1.5 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-black/40 border border-white/[0.04]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-amber-500/20 bg-amber-500/10 p-2.5 text-amber-400">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Readiness Score (0–100)</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                <div className="space-y-2.5 pt-4 border-t border-white/[0.06]">
                  <div className="flex items-center justify-between text-[10px] font-mono text-slate-400">
                    <span>Readiness Weighting</span>
                    <span className="text-amber-400 font-semibold">100% Deterministic</span>
                  </div>
                  <div className="h-2 w-full bg-black/80 rounded-full overflow-hidden flex border border-white/10 p-0.5">
                    <div className="bg-sky-400 h-full rounded-full w-[60%]" title="Security 60%" />
                    <div className="bg-indigo-400 h-full rounded-full w-[25%]" title="Quality 25%" />
                    <div className="bg-amber-400 h-full rounded-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 6) */}
          <li className="md:col-span-6 list-none">
            <div className="relative h-full rounded-2xl surface-elevated-1 p-1.5 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-black/40 border border-white/[0.04]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-emerald-500/20 bg-emerald-500/10 p-2.5 text-emerald-400">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Zero-Execution Sandbox</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t border-white/[0.06] text-[11px] font-mono text-slate-400">
                  <span className="flex items-center space-x-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>

      {/* Elegant Hairline Divider with Spacing */}
      <div className="max-w-5xl mx-auto px-4 my-16 sm:my-24">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 4. SCAN PIPELINE ARCHITECTURE                                             */}
      {/* ========================================================================= */}
      <section className="max-w-5xl mx-auto py-8 sm:py-16 px-4 relative z-10 space-y-10">
        <div className="text-center space-y-3">
          <div className="inline-flex items-center space-x-2 text-xs font-mono text-sky-400 font-semibold uppercase tracking-wider surface-pill px-3.5 py-1 rounded-full">
            <Layers className="w-3.5 h-3.5 text-sky-400" />
            <span>Automated Analysis Pipeline</span>
          </div>
          <h2 className="text-2xl sm:text-4xl font-extrabold font-display text-white tracking-tight">
            How Codexa Audits Your Code
          </h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 pt-2">
          {/* Stage 1 */}
          <div className="p-5 rounded-2xl surface-elevated-1 hover:border-sky-500/30 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 font-bold font-mono text-xs mb-3.5">
              01
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Sandboxed Ingestion</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Safe extraction of ZIP archives or GitHub repos with Zip Slip, Bomb &amp; Depth verification.
            </p>
          </div>

          {/* Stage 2 */}
          <div className="p-5 rounded-2xl surface-elevated-1 hover:border-indigo-500/30 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 font-bold font-mono text-xs mb-3.5">
              02
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Deterministic AST Scan</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              JavaParser &amp; Multi-Lang regex pattern analyzers detect 19+ critical vulnerability rules.
            </p>
          </div>

          {/* Stage 3 */}
          <div className="p-5 rounded-2xl surface-elevated-1 hover:border-sky-500/30 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 font-bold font-mono text-xs mb-3.5">
              03
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Neural Cascade</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Nvidia Nemotron 550B generates explainable security insights and context-aware code patches.
            </p>
          </div>

          {/* Stage 4 */}
          <div className="p-5 rounded-2xl surface-elevated-1 hover:border-emerald-500/30 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-400/20 flex items-center justify-center text-emerald-400 font-bold font-mono text-xs mb-3.5">
              04
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Readiness Index</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Mathematical scoring formula produces clear 0–100 production-readiness rating &amp; report.
            </p>
          </div>
        </div>
      </section>

      {/* Elegant Hairline Divider with Spacing */}
      <div className="max-w-5xl mx-auto px-4 my-16 sm:my-24">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 5. BOTTOM CTA LAUNCH BANNER                                               */}
      {/* ========================================================================= */}
      <section className="max-w-4xl mx-auto py-8 sm:py-16 px-4 relative z-10">
        <div className="relative rounded-3xl surface-elevated-2 p-8 sm:p-12 text-center space-y-6 overflow-hidden">
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-[400px] h-48 bg-sky-500/15 rounded-full blur-3xl pointer-events-none" />
          
          <div className="relative space-y-2.5">
            <h3 className="text-2xl sm:text-4xl font-extrabold font-display text-white tracking-tight">
              Ready to Audit Your Codebase?
            </h3>
            <p className="text-sm sm:text-base text-slate-400 max-w-xl mx-auto font-sans leading-relaxed">
              Scan your repository in seconds. Zero bytecode execution, 100% explainable security and production readiness score.
            </p>
          </div>

          <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3.5">
            <button
              onClick={onStartAnalysis}
              className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-gradient-to-r from-sky-400 via-sky-500 to-blue-600 text-slate-950 font-bold text-sm flex items-center justify-center space-x-2 shadow-[0_0_25px_rgba(56,189,248,0.35)] hover:shadow-[0_0_35px_rgba(56,189,248,0.5)] border border-white/20 transition-all duration-200 cursor-pointer font-display transform hover:-translate-y-0.5 active:translate-y-0"
            >
              <span>Launch New Audit</span>
              <ArrowRight className="w-4 h-4 text-slate-950" />
            </button>
            <a
              href="https://github.com/Codewithjainam7/Codexa"
              target="_blank"
              rel="noreferrer"
              className="w-full sm:w-auto px-7 py-3.5 rounded-xl surface-pill hover:bg-white/[0.06] text-slate-300 hover:text-white border border-white/10 text-sm font-semibold transition-all duration-200 flex items-center justify-center space-x-2 font-display"
            >
              <GitBranch className="w-4 h-4 text-slate-400" />
              <span>Star on GitHub</span>
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}
