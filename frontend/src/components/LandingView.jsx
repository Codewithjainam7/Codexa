import React, { useState } from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2, AlertTriangle, 
  Code2, Activity, GitBranch, Layers, Check, Copy, ExternalLink,
  ChevronRight, RefreshCw, EyeOff, Bug
} from "lucide-react";
import AuroraBackground from "./ui/AuroraBackground";
import GlowingEffect from "./ui/GlowingEffect";
import { CanvasText } from "./ui/canvas-text";

export default function LandingView({ onStartAnalysis }) {
  const [activeCodeTab, setActiveCodeTab] = useState("sqli");
  const [copied, setCopied] = useState(false);

  const codeDemos = {
    sqli: {
      title: "SQL Injection in auth_controller.ts",
      vulnType: "OWASP A03:2021 — Injection",
      severity: "CRITICAL",
      before: `// Vulnerable Code (Raw String Concat)
const query = "SELECT * FROM users WHERE id = '" + req.body.userId + "'";
const user = await db.raw(query);`,
      after: `// Remediated by Codexa (Parameterized Query)
const user = await db('users')
  .where({ id: req.body.userId })
  .first();`,
      explanation: "Replaced raw string concatenation with parameterized Knex query builder to prevent SQL injection payloads."
    },
    rce: {
      title: "Command Execution in file_utils.py",
      vulnType: "OWASP A03:2021 — Remote Code Exec",
      severity: "CRITICAL",
      before: `// Vulnerable Code (Shell Execution)
import os
os.system(f"ffmpeg -i {user_filename} output.mp4")`,
      after: `// Remediated by Codexa (Sandboxed Subprocess)
import subprocess, shlex
subprocess.run(["ffmpeg", "-i", shlex.quote(user_filename), "output.mp4"], check=True)`,
      explanation: "Used subprocess list arguments with shlex.quote sanitization to eliminate shell injection attack surfaces."
    },
    secrets: {
      title: "Hardcoded API Key in config.go",
      vulnType: "CWE-798 — Hardcoded Credentials",
      severity: "HIGH",
      before: `// Vulnerable Code (Leaked Secret)
const stripeKey = "SAMPLE_KEY_AKIAIOSFODNN7EXAMPLE";`,
      after: `// Remediated by Codexa (Environment Variable)
stripeKey := os.Getenv("STRIPE_SECRET_KEY")
if stripeKey == "" {
    log.Fatal("STRIPE_SECRET_KEY is required")
}`,
      explanation: "Masked production secret and refactored to read from secure environment configuration."
    }
  };

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <AuroraBackground className="relative space-y-16 sm:space-y-20 py-2 sm:py-4 font-sans bg-transparent">
      {/* Hero Section */}
      <section className="text-center max-w-4xl mx-auto space-y-6 relative z-10 pt-2 sm:pt-4">
        {/* Top Floating Badge */}
        <div className="inline-flex items-center space-x-2.5 px-4 py-1.5 rounded-full bg-slate-900/90 border border-cyan-500/30 text-cyan-300 text-xs font-semibold shadow-lg shadow-cyan-500/10 backdrop-blur-xl">
          <Sparkles className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
          <span className="font-display tracking-wide">
            Deterministic AST Engine + Nvidia Nemotron 550B Audit
          </span>
        </div>
        
        {/* Main Headline with CanvasText */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold font-display text-white tracking-tight leading-[1.12]">
          Is Your <br className="sm:hidden" />
          <CanvasText
            text="AI-Generated Code"
            colors={[
              "rgba(34, 211, 238, 1)",      // cyan-400
              "rgba(6, 182, 212, 0.95)",    // cyan-500
              "rgba(59, 130, 246, 0.9)",    // blue-500
              "rgba(99, 102, 241, 0.85)",   // indigo-500
              "rgba(168, 85, 247, 0.8)",    // violet-500
              "rgba(45, 212, 191, 0.75)",   // teal-400
              "rgba(255, 255, 255, 0.9)",   // white flash
            ]}
            animationSpeed={0.5}
            className="mx-1 my-1"
          />
          <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-slate-100 via-slate-200 to-slate-400">
            Ready for Production?
          </span>
        </h1>

        {/* Subtitle */}
        <p className="text-base sm:text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed font-sans">
          Codexa audits AI-generated applications, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by verified risk, and computes an explainable <strong className="text-white">Production Readiness Score (0–100)</strong>.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 pt-2">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-gradient-to-r from-cyan-500 via-blue-600 to-violet-600 text-white font-extrabold text-base flex items-center justify-center space-x-2.5 shadow-xl shadow-cyan-500/25 hover:shadow-cyan-500/40 transition-all transform hover:-translate-y-0.5 active:scale-95 cursor-pointer font-display tracking-tight"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5 text-white" />
          </button>
          
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-slate-900/80 hover:bg-slate-800 text-slate-200 hover:text-white border border-slate-700/80 font-semibold text-base transition-all flex items-center justify-center space-x-2 shadow-lg backdrop-blur-md font-display"
          >
            <GitBranch className="w-4 h-4 text-cyan-400" />
            <span>View GitHub Source</span>
          </a>
        </div>

        {/* Quick Capabilities Strip */}
        <div className="pt-4 grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-4xl mx-auto text-left">
          <div className="p-3.5 rounded-2xl bg-slate-900/70 border border-slate-800/90 backdrop-blur-md shadow-inner hover:border-cyan-500/30 transition-colors">
            <div className="flex items-center space-x-2 text-cyan-400 mb-1">
              <Shield className="w-3.5 h-3.5" />
              <span className="text-[11px] text-slate-400 font-mono">AST Engine</span>
            </div>
            <div className="text-sm font-bold text-white font-display">19+ Security Rules</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900/70 border border-slate-800/90 backdrop-blur-md shadow-inner hover:border-violet-500/30 transition-colors">
            <div className="flex items-center space-x-2 text-violet-400 mb-1">
              <Cpu className="w-3.5 h-3.5" />
              <span className="text-[11px] text-slate-400 font-mono">AI Cascade</span>
            </div>
            <div className="text-sm font-bold text-white font-display">Nemotron 550B</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900/70 border border-slate-800/90 backdrop-blur-md shadow-inner hover:border-blue-500/30 transition-colors">
            <div className="flex items-center space-x-2 text-blue-400 mb-1">
              <Zap className="w-3.5 h-3.5" />
              <span className="text-[11px] text-slate-400 font-mono">Benchmark</span>
            </div>
            <div className="text-sm font-bold text-slate-200 font-display">&lt; 100ms Parsing</div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900/70 border border-slate-800/90 backdrop-blur-md shadow-inner hover:border-teal-500/30 transition-colors">
            <div className="flex items-center space-x-2 text-teal-400 mb-1">
              <Lock className="w-3.5 h-3.5" />
              <span className="text-[11px] text-slate-400 font-mono">Sandboxing</span>
            </div>
            <div className="text-sm font-bold text-slate-200 font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Interactive Code Remediation Live Diff Teaser */}
      <section className="max-w-4xl mx-auto space-y-4 relative z-10">
        <div className="flex items-center justify-between px-1">
          <div className="flex items-center space-x-2">
            <Terminal className="w-4 h-4 text-cyan-400" />
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-400">
              Interactive Live Remediation Preview
            </h2>
          </div>
          <span className="text-[11px] font-mono text-cyan-400/90 bg-cyan-500/10 px-2.5 py-0.5 rounded-full border border-cyan-500/20">
            Realtime AST Diff
          </span>
        </div>

        {/* Code Terminal Box */}
        <div className="rounded-3xl border border-slate-800/90 bg-slate-950/90 backdrop-blur-2xl shadow-2xl overflow-hidden">
          {/* Terminal Tab Bar */}
          <div className="flex items-center justify-between px-4 py-3 bg-slate-900/80 border-b border-slate-800">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-rose-500/80" />
              <span className="w-3 h-3 rounded-full bg-amber-500/80" />
              <span className="w-3 h-3 rounded-full bg-emerald-500/80" />
              <span className="ml-2 text-xs font-mono text-slate-400 font-medium hidden sm:inline">
                {codeDemos[activeCodeTab].title}
              </span>
            </div>

            {/* Quick Demo Switchers */}
            <div className="flex items-center space-x-1.5">
              {Object.keys(codeDemos).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveCodeTab(tab)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-mono uppercase font-bold transition-all ${
                    activeCodeTab === tab
                      ? "bg-cyan-500/20 text-cyan-300 border border-cyan-500/30"
                      : "text-slate-400 hover:text-white"
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>
          </div>

          {/* Code Diff Display */}
          <div className="p-4 sm:p-6 grid grid-cols-1 md:grid-cols-2 gap-4 font-mono text-xs">
            {/* Before (Vulnerable) */}
            <div className="space-y-2 rounded-2xl bg-rose-950/20 border border-rose-900/30 p-4">
              <div className="flex items-center justify-between text-rose-400 font-bold pb-2 border-b border-rose-900/30">
                <span className="flex items-center space-x-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-400" />
                  <span>Vulnerable Code (Before)</span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-rose-500/10 border border-rose-500/20">
                  {codeDemos[activeCodeTab].severity}
                </span>
              </div>
              <pre className="text-rose-200/90 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1">
                {codeDemos[activeCodeTab].before}
              </pre>
            </div>

            {/* After (Remediated) */}
            <div className="space-y-2 rounded-2xl bg-emerald-950/20 border border-emerald-900/30 p-4">
              <div className="flex items-center justify-between text-emerald-400 font-bold pb-2 border-b border-emerald-900/30">
                <span className="flex items-center space-x-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Secure Remediated (After)</span>
                </span>
                <button
                  onClick={() => handleCopy(codeDemos[activeCodeTab].after)}
                  className="flex items-center space-x-1 text-[10px] text-emerald-300 hover:text-white bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20 transition-colors"
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
          <div className="px-5 py-3 bg-slate-900/60 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span className="flex items-center space-x-2">
              <Sparkles className="w-3.5 h-3.5 text-cyan-400 flex-shrink-0" />
              <span>{codeDemos[activeCodeTab].explanation}</span>
            </span>
            <span className="text-cyan-400 font-mono font-bold hidden sm:inline">0 Hallucination</span>
          </div>
        </div>
      </section>

      {/* Rich Bento Grid Section */}
      <section className="space-y-6 relative z-10 max-w-6xl mx-auto">
        <div className="flex items-center justify-between px-1">
          <div className="flex items-center space-x-2">
            <Sparkles className="w-4 h-4 text-cyan-400" />
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-400">
              Engine Architecture &amp; Capabilities
            </h2>
          </div>
          <span className="text-xs font-mono text-slate-500">5-Layer Security Shield</span>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-4 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/70 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-cyan-500/30 bg-cyan-500/10 p-2.5 text-cyan-400 shadow-inner">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">OWASP Top 10 SAST</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and deserialization flaws.
                    </p>
                  </div>
                </div>

                {/* Interactive vulnerability chips */}
                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-slate-800 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">SQLi Shield</span>
                  <span className="px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-300 border border-rose-500/20">RCE Blocked</span>
                  <span className="px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-300 border border-blue-500/20">XSS Sanitizer</span>
                  <span className="px-2 py-0.5 rounded-full bg-violet-500/10 text-violet-300 border border-violet-500/20">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 4, Row Span 2) */}
          <li className="md:col-span-4 md:row-span-2 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/70 border border-slate-800/60">
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-2.5 text-emerald-400 shadow-inner">
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

                {/* Mini IDE Terminal Code Window */}
                <div className="my-auto rounded-xl bg-slate-950 border border-slate-800 p-3 font-mono text-[11px] space-y-2 shadow-inner">
                  <div className="flex items-center justify-between pb-1.5 border-b border-slate-800 text-[10px] text-slate-500">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-rose-500/80" />
                      <span className="w-2 h-2 rounded-full bg-amber-500/80" />
                      <span className="w-2 h-2 rounded-full bg-emerald-500/80" />
                      <span className="ml-1 text-slate-300 font-sans">auth_handler.ts</span>
                    </div>
                    <span className="text-emerald-400 font-bold">Auto-Fixed</span>
                  </div>
                  <div className="space-y-1 text-xs">
                    <div className="text-rose-400 bg-rose-950/30 px-2 py-1 rounded border-l-2 border-rose-500 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-emerald-400 bg-emerald-950/30 px-2 py-1 rounded border-l-2 border-emerald-500 overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-[11px] text-slate-400 font-mono">
                  <span className="flex items-center space-x-1 text-slate-300">
                    <CheckCircle2 className="w-3.5 h-3.5 text-cyan-400" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-cyan-400 font-bold">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/70 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-violet-500/30 bg-violet-500/10 p-2.5 text-violet-400 shadow-inner">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-slate-800 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-violet-500/15 text-violet-300 border border-violet-500/30">Nemotron 550B</span>
                  <span className="px-2 py-0.5 rounded-full bg-blue-500/15 text-blue-300 border border-blue-500/30">Inkling Small</span>
                  <span className="px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/70 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-blue-500/30 bg-blue-500/10 p-2.5 text-blue-400 shadow-inner">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Readiness Score (0–100)</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                {/* Mini readiness meter */}
                <div className="space-y-1.5 pt-2 border-t border-slate-800">
                  <div className="flex items-center justify-between text-[10px] font-mono text-slate-400">
                    <span>Readiness Weighting</span>
                    <span className="text-cyan-400 font-bold">100% Deterministic</span>
                  </div>
                  <div className="h-2 w-full bg-slate-950 rounded-full overflow-hidden flex border border-slate-800">
                    <div className="bg-cyan-500 h-full w-[60%]" title="Security 60%" />
                    <div className="bg-blue-500 h-full w-[25%]" title="Quality 25%" />
                    <div className="bg-violet-500 h-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-slate-800/90 p-2 sm:p-3 bg-slate-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-slate-900/70 border border-slate-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-teal-500/30 bg-teal-500/10 p-2.5 text-teal-400 shadow-inner">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Zero-Execution Sandbox</h3>
                    <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-800 text-[10px] font-mono text-slate-400">
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-teal-400" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-teal-400" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>

      {/* 4-Stage Scan Architecture Pipeline */}
      <section className="max-w-5xl mx-auto space-y-6 relative z-10 px-2">
        <div className="text-center space-y-2">
          <div className="inline-flex items-center space-x-2 text-xs font-mono text-cyan-400 font-bold uppercase tracking-wider bg-cyan-500/10 px-3 py-1 rounded-full border border-cyan-500/20">
            <Layers className="w-3.5 h-3.5" />
            <span>Automated Analysis Pipeline</span>
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold font-display text-white">
            How Codexa Audits Your Code
          </h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 pt-2">
          {/* Stage 1 */}
          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md relative group hover:border-cyan-500/30 transition-all">
            <div className="w-8 h-8 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 font-bold font-mono text-xs mb-3">
              01
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Sandboxed Ingestion</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Safe extraction of ZIP archives or GitHub repos with Zip Slip, Bomb &amp; Depth verification.
            </p>
          </div>

          {/* Stage 2 */}
          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md relative group hover:border-blue-500/30 transition-all">
            <div className="w-8 h-8 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400 font-bold font-mono text-xs mb-3">
              02
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Deterministic AST Scan</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              JavaParser &amp; Multi-Lang regex pattern analyzers detect 19+ critical vulnerability rules.
            </p>
          </div>

          {/* Stage 3 */}
          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md relative group hover:border-violet-500/30 transition-all">
            <div className="w-8 h-8 rounded-xl bg-violet-500/10 border border-violet-500/20 flex items-center justify-center text-violet-400 font-bold font-mono text-xs mb-3">
              03
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Neural Cascade</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Nvidia Nemotron 550B generates explainable security insights and context-aware code patches.
            </p>
          </div>

          {/* Stage 4 */}
          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md relative group hover:border-teal-500/30 transition-all">
            <div className="w-8 h-8 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 font-bold font-mono text-xs mb-3">
              04
            </div>
            <h4 className="text-sm font-bold text-white font-display mb-1">Readiness Index</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Mathematical scoring formula produces clear 0–100 production-readiness rating &amp; PDF report.
            </p>
          </div>
        </div>
      </section>

      {/* Bottom CTA Glow Banner */}
      <section className="max-w-4xl mx-auto relative z-10 px-2">
        <div className="relative rounded-3xl border border-cyan-500/30 bg-gradient-to-r from-slate-900 via-slate-900/90 to-slate-900 p-8 sm:p-10 text-center space-y-5 overflow-hidden shadow-2xl shadow-cyan-500/10">
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-96 h-48 bg-cyan-500/15 rounded-full blur-3xl pointer-events-none" />
          
          <div className="relative space-y-2">
            <h3 className="text-2xl sm:text-4xl font-extrabold font-display text-white">
              Ready to Audit Your Codebase?
            </h3>
            <p className="text-sm sm:text-base text-slate-300 max-w-xl mx-auto">
              Scan your repository in seconds. Zero bytecode execution, 100% explainable security and production readiness score.
            </p>
          </div>

          <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3">
            <button
              onClick={onStartAnalysis}
              className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-gradient-to-r from-cyan-500 via-blue-600 to-violet-600 text-white font-extrabold text-sm flex items-center justify-center space-x-2 shadow-xl shadow-cyan-500/25 hover:shadow-cyan-500/40 transition-all cursor-pointer font-display"
            >
              <span>Launch New Audit</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </button>
            <a
              href="https://github.com/Codewithjainam7/Codexa"
              target="_blank"
              rel="noreferrer"
              className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-sm font-semibold transition-all flex items-center justify-center space-x-2 font-display"
            >
              <GitBranch className="w-4 h-4 text-cyan-400" />
              <span>Star on GitHub</span>
            </a>
          </div>
        </div>
      </section>
    </AuroraBackground>
  );
}
