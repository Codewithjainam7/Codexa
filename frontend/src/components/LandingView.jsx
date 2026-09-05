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
import BackgroundRippleEffect from "./ui/BackgroundRippleEffect";
import { CanvasText } from "./ui/canvas-text";
import { useTheme } from "../context/ThemeContext";

export default function LandingView({ onStartAnalysis }) {
  const { theme } = useTheme();
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

  const isDark = theme === 'dark';

  return (
    <div className="relative min-h-screen py-8 sm:py-14 font-sans bg-transparent">
      {/* 1. Viewport Flickering Grid Background Effect */}
      <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden">
        <FlickeringGrid
          className="w-full h-full"
          squareSize={4}
          gridGap={6}
          color={isDark ? "#3B82F6" : "#2563EB"}
          maxOpacity={isDark ? 0.28 : 0.20}
          flickerChance={0.14}
        />
        {/* Soft Radial Ambient Vignette */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(37,99,235,0.06),rgba(255,255,255,0))] dark:bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(59,130,246,0.10),rgba(0,0,0,0))] pointer-events-none" />
      </div>

      {/* Ambient background ripple waves with multi-focal centers */}
      <BackgroundRippleEffect
        numCircles={9}
        mainCircleSize={260}
        mainCircleOpacity={isDark ? 0.16 : 0.11}
        interactive={true}
        focalPoints={[
          { x: "50%", y: "14%", size: 320, scale: 1.2 },
          { x: "84%", y: "24%", size: 240, scale: 0.9 },
          { x: "16%", y: "42%", size: 260, scale: 1.0 },
          { x: "50%", y: "60%", size: 300, scale: 1.1 },
          { x: "80%", y: "75%", size: 240, scale: 0.95 }
        ]}
      />

      {/* Ambient background light orbs that shine through frosted glass cards */}
      <div className="fixed top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[750px] h-[450px] bg-blue-600/8 dark:bg-blue-600/14 rounded-full blur-[140px] pointer-events-none -z-10" />
      <div className="fixed bottom-1/3 right-1/4 w-[550px] h-[380px] bg-indigo-600/6 dark:bg-indigo-600/10 rounded-full blur-[130px] pointer-events-none -z-10" />
      <div className="fixed top-2/3 left-1/4 w-[480px] h-[320px] bg-sky-500/5 dark:bg-sky-500/8 rounded-full blur-[130px] pointer-events-none -z-10" />

      {/* ========================================================================= */}
      {/* 1. HERO SECTION (EDITORIAL ASYMMETRIC COMMAND CENTER)                     */}
      {/* ========================================================================= */}
      <section className="pt-6 sm:pt-10 pb-12 sm:pb-18 px-4 relative z-10">
        <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          
          {/* Left Hero Column: Commanding Value Prop & CTAs */}
          <div className="lg:col-span-7 space-y-6 text-left">
            {/* Status Beacon Badge */}
            <div className="inline-flex items-center space-x-2.5 px-3.5 py-1.5 rounded-full cdx-pill text-xs font-semibold">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-500 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-600 dark:bg-blue-400 shadow-[0_0_10px_#3b82f6]" />
              </span>
              <span className="font-mono text-[11px] tracking-wide text-slate-700 dark:text-slate-300">
                Deterministic AST Engine <span className="text-blue-600 dark:text-blue-400 font-bold mx-1">&bull;</span> Nvidia Nemotron 550B Audit
              </span>
            </div>
            
            {/* Hero Main Headline - 100% visible, crisp, vibrant gradient */}
            <h1 className="text-4xl sm:text-6xl lg:text-[68px] font-black font-display text-slate-950 dark:text-white tracking-tight sm:tracking-tighter leading-[1.05]">
              Audit &amp; Secure <br />
              <span className="inline-block text-transparent bg-clip-text bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-500 dark:from-blue-400 dark:via-sky-300 dark:to-indigo-400 drop-shadow-sm">
                AI-Generated Code
              </span> <br />
              <span className="text-slate-900 dark:text-slate-100">
                Before Production.
              </span>
            </h1>

            {/* Subtitle */}
            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-300 max-w-xl leading-relaxed font-sans font-normal">
              Codexa inspects AI-generated repositories, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by verified risk, and computes an explainable <strong className="text-slate-950 dark:text-white font-semibold">Production Readiness Score (0–100)</strong>.
            </p>

            {/* Dual Primary Action Triggers */}
            <div className="flex flex-col sm:flex-row items-center gap-3.5 pt-2">
              <button
                onClick={onStartAnalysis}
                className="cdx-btn-primary w-full sm:w-auto px-7 py-3.5 rounded-xl font-display font-bold text-sm flex items-center justify-center space-x-2.5 cursor-pointer shadow-lg shadow-blue-500/25"
              >
                <span>Start Codebase Audit</span>
                <ArrowRight className="w-4 h-4 stroke-[2.5]" />
              </button>
              
              <a
                href="https://github.com/Codewithjainam7/Codexa"
                target="_blank"
                rel="noreferrer"
                className="cdx-btn-secondary w-full sm:w-auto px-6 py-3.5 rounded-xl font-semibold text-sm flex items-center justify-center space-x-2 font-display text-slate-900 dark:text-white"
              >
                <GitBranch className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                <span>View GitHub Source</span>
              </a>
            </div>

            {/* Trust Badges */}
            <div className="pt-3 flex flex-wrap items-center gap-4 text-xs font-mono text-slate-600 dark:text-slate-400 font-medium">
              <span className="flex items-center space-x-1.5">
                <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 stroke-[2.5]" />
                <span>Zero Bytecode Exec</span>
              </span>
              <span className="flex items-center space-x-1.5">
                <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 stroke-[2.5]" />
                <span>19+ AST Rules</span>
              </span>
              <span className="flex items-center space-x-1.5">
                <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 stroke-[2.5]" />
                <span>&lt; 100ms Parsing</span>
              </span>
            </div>
          </div>

          {/* Right Hero Column: Live Neural AST Command Center Console */}
          <div className="lg:col-span-5 text-left">
            <div className="cdx-elevated rounded-2xl p-5 relative overflow-hidden group specular-rim shadow-2xl space-y-4">
              {/* Terminal Window Chrome */}
              <div className="flex items-center justify-between pb-3.5 border-b border-[var(--border-subtle)]">
                <div className="flex items-center space-x-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500/90" />
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-500/90" />
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/90" />
                  <span className="ml-2 text-xs font-mono text-slate-900 dark:text-white font-bold">
                    Codexa Neural AST Inspector
                  </span>
                </div>
                <div className="flex items-center space-x-1.5">
                  <span className="text-[9px] font-mono font-bold bg-blue-500/15 text-blue-700 dark:text-blue-400 px-2 py-0.5 rounded-md border border-blue-500/30">
                    100% AST PARSE
                  </span>
                  <span className="text-[9px] font-mono font-bold bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 px-2 py-0.5 rounded-md border border-emerald-500/30">
                    0 BYTECODE
                  </span>
                </div>
              </div>

              {/* Active Telemetry Cards with Realtime Visual Delight */}
              <div className="space-y-3 font-mono text-xs">
                {/* Telemetry Stat 1 - Vulnerability Guard */}
                <div className="p-3.5 rounded-xl cdx-recessed space-y-1.5 border border-[var(--border-subtle)] transition-all hover:border-blue-500/30">
                  <div className="text-[11px] text-blue-700 dark:text-blue-400 font-bold flex items-center justify-between">
                    <span className="flex items-center space-x-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse" />
                      <span>Vulnerability Guard</span>
                    </span>
                    <ShieldCheck className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div className="text-slate-950 dark:text-white font-bold text-sm font-sans flex items-center justify-between">
                    <span>19+ OWASP Rules Active</span>
                    <span className="text-[10px] font-mono text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded">Enforced</span>
                  </div>
                  <div className="text-[10px] text-slate-600 dark:text-slate-400 font-medium">SQLi, RCE, SSRF, Deserialization, CSRF</div>
                </div>

                {/* Telemetry Stat 2 - Readiness Score */}
                <div className="p-3.5 rounded-xl cdx-recessed space-y-1.5 border border-[var(--border-subtle)] transition-all hover:border-emerald-500/30">
                  <div className="text-[11px] text-emerald-700 dark:text-emerald-400 font-bold flex items-center justify-between">
                    <span className="flex items-center space-x-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                      <span>Readiness Score</span>
                    </span>
                    <Zap className="w-4 h-4 text-emerald-500" />
                  </div>
                  <div className="text-slate-950 dark:text-white font-bold text-sm font-sans flex items-center justify-between">
                    <span>98.5 / 100 Grade A</span>
                    <span className="text-[10px] font-mono text-blue-700 dark:text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded">Verified</span>
                  </div>
                  <div className="text-[10px] text-slate-600 dark:text-slate-400 font-medium">Security 60% &bull; Quality 25% &bull; Ops 15%</div>
                </div>

                {/* Telemetry Stat 3 - AI Cascader */}
                <div className="p-3.5 rounded-xl cdx-recessed space-y-1.5 border border-[var(--border-subtle)] transition-all hover:border-indigo-500/30">
                  <div className="text-[11px] text-indigo-700 dark:text-indigo-400 font-bold flex items-center justify-between">
                    <span className="flex items-center space-x-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-pulse" />
                      <span>AI Cascader</span>
                    </span>
                    <Cpu className="w-4 h-4 text-indigo-500" />
                  </div>
                  <div className="text-slate-950 dark:text-white font-bold text-sm font-sans flex items-center justify-between">
                    <span>Nvidia Nemotron 550B</span>
                    <span className="text-[10px] font-mono text-indigo-700 dark:text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded">&lt; 85ms</span>
                  </div>
                  <div className="text-[10px] text-slate-600 dark:text-slate-400 font-medium">Contextual patches &amp; zero hallucination</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Floating Telemetry Metric Dock (4 Cards) */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-5xl mx-auto mt-12 text-left">
          <div className="p-4 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group transform hover:-translate-y-1 shadow-md">
            <div className="flex items-center space-x-2 text-blue-600 dark:text-blue-400 mb-1.5">
              <div className="p-1.5 rounded-lg bg-blue-500/10 border border-blue-500/20">
                <Shield className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-600 dark:text-slate-400 font-mono font-medium">AST Engine</span>
            </div>
            <div className="text-sm font-bold text-slate-950 dark:text-white font-display">19+ Security Rules</div>
          </div>

          <div className="p-4 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group transform hover:-translate-y-1 shadow-md">
            <div className="flex items-center space-x-2 text-indigo-500 mb-1.5">
              <div className="p-1.5 rounded-lg bg-indigo-500/10 border border-indigo-500/20">
                <Cpu className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-600 dark:text-slate-400 font-mono font-medium">AI Cascade</span>
            </div>
            <div className="text-sm font-bold text-slate-950 dark:text-white font-display">Nemotron 550B</div>
          </div>

          <div className="p-4 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group transform hover:-translate-y-1 shadow-md">
            <div className="flex items-center space-x-2 text-emerald-500 mb-1.5">
              <div className="p-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
                <Zap className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-600 dark:text-slate-400 font-mono font-medium">Benchmark</span>
            </div>
            <div className="text-sm font-bold text-slate-950 dark:text-white font-display">&lt; 100ms Parsing</div>
          </div>

          <div className="p-4 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group transform hover:-translate-y-1 shadow-md">
            <div className="flex items-center space-x-2 text-sky-500 mb-1.5">
              <div className="p-1.5 rounded-lg bg-sky-500/10 border border-sky-500/20">
                <Lock className="w-4 h-4 group-hover:scale-110 transition-transform" />
              </div>
              <span className="text-[11px] text-slate-600 dark:text-slate-400 font-mono font-medium">Sandboxing</span>
            </div>
            <div className="text-sm font-bold text-slate-950 dark:text-white font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-5xl mx-auto px-4 my-14 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-[var(--border-medium)] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 2. REMEDIATION TERMINAL SECTION                                           */}
      {/* ========================================================================= */}
      <section className="max-w-4xl mx-auto py-8 sm:py-14 px-4 relative z-10 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 px-1">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-lg bg-blue-500/10 border border-blue-500/20">
              <Terminal className="w-4 h-4 text-blue-600 dark:text-blue-400" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-900 dark:text-white">
              Interactive Live Remediation Preview
            </h2>
          </div>
          <span className="w-fit text-[11px] font-mono text-blue-700 dark:text-blue-400 bg-blue-500/15 px-3 py-1 rounded-full border border-blue-500/30 shadow-sm font-bold">
            Realtime AST Diff
          </span>
        </div>

        {/* Realistic Terminal Shell with Frosted Glass */}
        <div className="rounded-2xl cdx-elevated p-1 overflow-hidden specular-rim shadow-2xl">
          {/* Window Header */}
          <div className="flex items-center justify-between px-5 py-3.5 bg-[var(--bg-recessed)] border-b border-[var(--border-subtle)]">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-rose-500/90 border border-rose-400/40 shadow-sm" />
              <span className="w-3 h-3 rounded-full bg-amber-500/90 border border-amber-400/40 shadow-sm" />
              <span className="w-3 h-3 rounded-full bg-emerald-500/90 border border-emerald-400/40 shadow-sm" />
              <span className="ml-3 text-xs font-mono text-slate-700 dark:text-slate-300 font-semibold hidden sm:inline">
                {codeDemos[activeCodeTab].title}
              </span>
            </div>

            {/* Segmented Switcher Capsule */}
            <div className="flex items-center p-0.5 bg-[var(--bg-card)] border border-[var(--border-subtle)] rounded-lg">
              {Object.keys(codeDemos).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveCodeTab(tab)}
                  className={`px-3 py-1 rounded-md text-xs font-mono uppercase font-bold transition-all duration-150 ${
                    activeCodeTab === tab
                      ? "bg-blue-500/20 text-blue-700 dark:text-blue-400 border border-blue-500/30 shadow-sm"
                      : "text-slate-600 dark:text-slate-400 hover:text-slate-950 dark:hover:text-white"
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
            <div className="space-y-3 rounded-xl bg-rose-500/10 dark:bg-rose-950/20 border border-rose-500/25 p-4">
              <div className="flex items-center justify-between text-rose-700 dark:text-rose-400 font-bold pb-2 border-b border-rose-500/20">
                <span className="flex items-center space-x-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-500" />
                  <span>Vulnerable (Before)</span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-md bg-rose-500/15 border border-rose-500/25 font-bold">
                  {codeDemos[activeCodeTab].severity}
                </span>
              </div>
              <pre className="text-rose-950 dark:text-rose-200 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1 font-semibold">
                {codeDemos[activeCodeTab].before}
              </pre>
            </div>

            {/* After (Remediated) */}
            <div className="space-y-3 rounded-xl bg-emerald-500/10 dark:bg-emerald-950/20 border border-emerald-500/25 p-4">
              <div className="flex items-center justify-between text-emerald-700 dark:text-emerald-400 font-bold pb-2 border-b border-emerald-500/20">
                <span className="flex items-center space-x-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                  <span>Secure (After)</span>
                </span>
                <button
                  onClick={() => handleCopy(codeDemos[activeCodeTab].after)}
                  className="flex items-center space-x-1 text-[10px] text-emerald-800 dark:text-emerald-300 hover:opacity-80 bg-emerald-500/15 px-2.5 py-0.5 rounded-md border border-emerald-500/25 transition-colors cursor-pointer font-semibold"
                >
                  {copied ? <Check className="w-3 h-3 text-emerald-600 dark:text-emerald-400" /> : <Copy className="w-3 h-3" />}
                  <span>{copied ? "Copied" : "Copy Fix"}</span>
                </button>
              </div>
              <pre className="text-emerald-950 dark:text-emerald-200 whitespace-pre-wrap leading-relaxed overflow-x-auto text-[11px] pt-1 font-semibold">
                {codeDemos[activeCodeTab].after}
              </pre>
            </div>
          </div>

          {/* Explanation Footer */}
          <div className="px-5 py-3.5 bg-[var(--bg-recessed)] border-t border-[var(--border-subtle)] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs text-slate-700 dark:text-slate-300">
            <span className="flex items-center space-x-2 font-medium">
              <Sparkles className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400 flex-shrink-0" />
              <span>{codeDemos[activeCodeTab].explanation}</span>
            </span>
            <span className="text-blue-700 dark:text-blue-400 font-mono font-bold text-[11px]">0 Hallucination</span>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-5xl mx-auto px-4 my-14 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-[var(--border-medium)] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 3. 5-LAYER SECURITY BENTO GRID SECTION (ASYMMETRICAL LAYOUT)               */}
      {/* ========================================================================= */}
      <section className="max-w-6xl mx-auto py-8 sm:py-14 px-4 relative z-10 space-y-8">
        <div className="flex items-center justify-between px-1">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-lg bg-blue-500/10 border border-blue-500/20">
              <Sparkles className="w-4 h-4 text-blue-600 dark:text-blue-400" />
            </div>
            <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-slate-900 dark:text-white">
              Engine Architecture &amp; Capabilities
            </h2>
          </div>
          <span className="text-xs font-mono text-slate-600 dark:text-slate-400 font-medium">5-Layer Security Shield</span>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-5 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 7) - Flagship Feature */}
          <li className="md:col-span-7 list-none">
            <div className="relative h-full rounded-2xl cdx-card p-2 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-[var(--bg-card)] border border-[var(--border-subtle)]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-blue-500/20 bg-blue-500/10 p-2.5 text-blue-600 dark:text-blue-400">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-slate-950 dark:text-white">OWASP Top 10 SAST</h3>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 leading-relaxed font-normal">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and deserialization flaws.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 pt-4 border-t border-[var(--border-subtle)] text-[11px] font-mono">
                  <span className="px-2.5 py-1 rounded-md bg-blue-500/10 text-blue-700 dark:text-blue-400 border border-blue-500/20 font-medium">SQLi Shield</span>
                  <span className="px-2.5 py-1 rounded-md bg-rose-500/10 text-rose-700 dark:text-rose-400 border border-rose-500/20 font-medium">RCE Blocked</span>
                  <span className="px-2.5 py-1 rounded-md bg-indigo-500/10 text-indigo-700 dark:text-indigo-400 border border-indigo-500/20 font-medium">XSS Sanitizer</span>
                  <span className="px-2.5 py-1 rounded-md bg-[var(--bg-recessed)] text-slate-700 dark:text-slate-300 border border-[var(--border-subtle)] font-medium">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 5, Row Span 2) */}
          <li className="md:col-span-5 md:row-span-2 list-none">
            <div className="relative h-full rounded-2xl cdx-card p-2 group transition-all duration-300">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-[var(--bg-card)] border border-[var(--border-subtle)]">
                <div className="space-y-3.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-xl border border-emerald-500/20 bg-emerald-500/10 p-2.5 text-emerald-500">
                      <FileCode className="h-5 w-5" />
                    </div>
                    <span className="text-[10px] font-mono font-bold text-emerald-700 dark:text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded-md border border-emerald-500/20">
                      LIVE PATCHING
                    </span>
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-slate-950 dark:text-white">Before &amp; After Fix Diffs</h3>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 leading-relaxed font-normal">
                      Side-by-side IDE terminal diff comparison with automatic secret masking and copyable secure fixes.
                    </p>
                  </div>
                </div>

                {/* Mini IDE Terminal Window */}
                <div className="my-auto rounded-xl cdx-recessed p-3.5 font-mono text-[11px] space-y-2.5 shadow-inner">
                  <div className="flex items-center justify-between pb-2 border-b border-[var(--border-subtle)] text-[10px] text-slate-600 dark:text-slate-400">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-rose-500/90" />
                      <span className="w-2 h-2 rounded-full bg-amber-500/90" />
                      <span className="w-2 h-2 rounded-full bg-emerald-500/90" />
                      <span className="ml-1 text-slate-700 dark:text-slate-300 font-sans font-medium">auth_handler.ts</span>
                    </div>
                    <span className="text-emerald-700 dark:text-emerald-400 font-bold">Auto-Fixed</span>
                  </div>
                  <div className="space-y-1.5 text-xs font-semibold">
                    <div className="text-rose-800 dark:text-rose-400 bg-rose-500/15 px-2.5 py-1.5 rounded-lg border-l-2 border-rose-500 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-emerald-800 dark:text-emerald-400 bg-emerald-500/15 px-2.5 py-1.5 rounded-lg border-l-2 border-emerald-500 overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-3.5 border-t border-[var(--border-subtle)] flex items-center justify-between text-[11px] text-slate-700 dark:text-slate-300 font-mono">
                  <span className="flex items-center space-x-1.5 text-slate-900 dark:text-white font-medium">
                    <CheckCircle2 className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-blue-700 dark:text-blue-400 font-bold">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 7) */}
          <li className="md:col-span-7 list-none">
            <div className="relative h-full rounded-2xl cdx-card p-2 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-[var(--bg-card)] border border-[var(--border-subtle)]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-indigo-500/20 bg-indigo-500/10 p-2.5 text-indigo-600 dark:text-indigo-400">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-slate-950 dark:text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 leading-relaxed font-normal">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 pt-4 border-t border-[var(--border-subtle)] text-[11px] font-mono">
                  <span className="px-2.5 py-1 rounded-md bg-indigo-500/10 text-indigo-700 dark:text-indigo-400 border border-indigo-500/20 font-medium">Nemotron 550B</span>
                  <span className="px-2.5 py-1 rounded-md bg-blue-500/10 text-blue-700 dark:text-blue-400 border border-blue-500/20 font-medium">Inkling Small</span>
                  <span className="px-2.5 py-1 rounded-md bg-[var(--bg-recessed)] text-slate-700 dark:text-slate-300 border border-[var(--border-subtle)] font-medium">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 6) */}
          <li className="md:col-span-6 list-none">
            <div className="relative h-full rounded-2xl cdx-card p-2 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-[var(--bg-card)] border border-[var(--border-subtle)]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-blue-500/20 bg-blue-500/10 p-2.5 text-blue-600 dark:text-blue-400">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-slate-950 dark:text-white">Readiness Score (0–100)</h3>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 leading-relaxed font-normal">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                <div className="space-y-2.5 pt-4 border-t border-[var(--border-subtle)]">
                  <div className="flex items-center justify-between text-[10px] font-mono text-slate-600 dark:text-slate-400 font-medium">
                    <span>Readiness Weighting</span>
                    <span className="text-blue-700 dark:text-blue-400 font-bold">100% Deterministic</span>
                  </div>
                  <div className="h-2.5 w-full bg-[var(--bg-recessed)] rounded-full overflow-hidden flex border border-[var(--border-subtle)] p-0.5">
                    <div className="bg-blue-600 h-full rounded-full w-[60%]" title="Security 60%" />
                    <div className="bg-indigo-500 h-full rounded-full w-[25%]" title="Quality 25%" />
                    <div className="bg-slate-400 h-full rounded-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 6) */}
          <li className="md:col-span-6 list-none">
            <div className="relative h-full rounded-2xl cdx-card p-2 group transition-all duration-300">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-5 rounded-xl p-6 bg-[var(--bg-card)] border border-[var(--border-subtle)]">
                <div className="space-y-3.5">
                  <div className="w-fit rounded-xl border border-emerald-500/20 bg-emerald-500/10 p-2.5 text-emerald-500">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-slate-950 dark:text-white">Zero-Execution Sandbox</h3>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 leading-relaxed font-normal">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t border-[var(--border-subtle)] text-[11px] font-mono text-slate-600 dark:text-slate-400">
                  <span className="flex items-center space-x-1.5 text-emerald-700 dark:text-emerald-400 font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1.5 text-emerald-700 dark:text-emerald-400 font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-5xl mx-auto px-4 my-14 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-[var(--border-medium)] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 4. SCAN PIPELINE ARCHITECTURE                                             */}
      {/* ========================================================================= */}
      <section className="max-w-5xl mx-auto py-8 sm:py-14 px-4 relative z-10 space-y-10">
        <div className="text-center space-y-3">
          <div className="inline-flex items-center space-x-2 text-xs font-mono text-blue-700 dark:text-blue-400 font-bold uppercase tracking-wider cdx-pill px-3.5 py-1 rounded-full">
            <Layers className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
            <span>Automated Analysis Pipeline</span>
          </div>
          <h2 className="text-2xl sm:text-4xl font-extrabold font-display text-slate-950 dark:text-white tracking-tight">
            How Codexa Audits Your Code
          </h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 pt-2">
          {/* Stage 1 */}
          <div className="p-5 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-700 dark:text-blue-400 font-bold font-mono text-xs mb-3.5">
              01
            </div>
            <h4 className="text-sm font-bold text-slate-950 dark:text-white font-display mb-1">Sandboxed Ingestion</h4>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed font-normal">
              Safe extraction of ZIP archives or GitHub repos with Zip Slip, Bomb &amp; Depth verification.
            </p>
          </div>

          {/* Stage 2 */}
          <div className="p-5 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-700 dark:text-indigo-400 font-bold font-mono text-xs mb-3.5">
              02
            </div>
            <h4 className="text-sm font-bold text-slate-950 dark:text-white font-display mb-1">Deterministic AST Scan</h4>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed font-normal">
              JavaParser &amp; Multi-Lang regex pattern analyzers detect 19+ critical vulnerability rules.
            </p>
          </div>

          {/* Stage 3 */}
          <div className="p-5 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-700 dark:text-sky-400 font-bold font-mono text-xs mb-3.5">
              03
            </div>
            <h4 className="text-sm font-bold text-slate-950 dark:text-white font-display mb-1">Neural Cascade</h4>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed font-normal">
              Nvidia Nemotron 550B generates explainable security insights and context-aware code patches.
            </p>
          </div>

          {/* Stage 4 */}
          <div className="p-5 rounded-2xl cdx-card hover:border-blue-500/40 transition-all duration-300 group">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-700 dark:text-emerald-400 font-bold font-mono text-xs mb-3.5">
              04
            </div>
            <h4 className="text-sm font-bold text-slate-950 dark:text-white font-display mb-1">Readiness Index</h4>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed font-normal">
              Mathematical scoring formula produces clear 0–100 production-readiness rating &amp; report.
            </p>
          </div>
        </div>
      </section>

      {/* Subtle Specular Divider */}
      <div className="max-w-5xl mx-auto px-4 my-14 sm:my-20">
        <div className="w-full h-px bg-gradient-to-r from-transparent via-[var(--border-medium)] to-transparent" />
      </div>

      {/* ========================================================================= */}
      {/* 5. BOTTOM CTA LAUNCH BANNER                                               */}
      {/* ========================================================================= */}
      <section className="max-w-4xl mx-auto py-8 sm:py-14 px-4 relative z-10">
        <div className="relative rounded-3xl cdx-elevated p-8 sm:p-12 text-center space-y-6 overflow-hidden specular-rim shadow-2xl">
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-[400px] h-48 bg-blue-600/15 rounded-full blur-3xl pointer-events-none" />
          
          <div className="relative space-y-2.5">
            <h3 className="text-2xl sm:text-4xl font-extrabold font-display text-slate-950 dark:text-white tracking-tight">
              Ready to Audit Your Codebase?
            </h3>
            <p className="text-sm sm:text-base text-slate-700 dark:text-slate-300 max-w-xl mx-auto font-sans leading-relaxed font-normal">
              Scan your repository in seconds. Zero bytecode execution, 100% explainable security and production readiness score.
            </p>
          </div>

          <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3.5">
            <button
              onClick={onStartAnalysis}
              className="cdx-btn-primary w-full sm:w-auto px-8 py-3.5 rounded-xl font-display font-bold text-sm flex items-center justify-center space-x-2 cursor-pointer shadow-lg shadow-blue-500/25"
            >
              <span>Launch New Audit</span>
              <ArrowRight className="w-4 h-4 stroke-[2.5]" />
            </button>
            <a
              href="https://github.com/Codewithjainam7/Codexa"
              target="_blank"
              rel="noreferrer"
              className="cdx-btn-secondary w-full sm:w-auto px-7 py-3.5 rounded-xl text-sm font-semibold flex items-center justify-center space-x-2 font-display text-slate-900 dark:text-white"
            >
              <GitBranch className="w-4 h-4 text-blue-600 dark:text-blue-400" />
              <span>Star on GitHub</span>
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}
