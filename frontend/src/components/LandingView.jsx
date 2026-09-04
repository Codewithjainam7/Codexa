"use client";

import React from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2
} from "lucide-react";
import DottedGlowBackground from "./ui/DottedGlowBackground";
import GlowingEffect from "./ui/GlowingEffect";

const GridItem = ({ area, icon, title, description }) => {
  return (
    <li className={"min-h-[14rem] list-none " + area}>
      <div className="relative h-full rounded-2xl border border-slate-800/90 p-2 md:rounded-3xl md:p-3 bg-slate-950/80 backdrop-blur-xl group transition-all">
        <GlowingEffect
          spread={40}
          glow={true}
          disabled={false}
          proximity={64}
          inactiveZone={0.01}
        />
        <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl p-6 md:p-6 bg-slate-900/60 border border-slate-800/60 shadow-[0_0_27px_0_rgba(16,185,129,0.04)]">
          <div className="relative flex flex-1 flex-col justify-between gap-4">
            <div className="w-fit rounded-xl border border-slate-700/80 bg-slate-950 p-2.5 text-emerald-400 shadow-inner group-hover:border-emerald-500/40 group-hover:text-emerald-300 transition-colors">
              {icon}
            </div>
            <div className="space-y-2">
              <h3 className="font-sans text-lg font-bold tracking-tight text-white md:text-xl">
                {title}
              </h3>
              <p className="font-sans text-xs text-slate-400 md:text-sm leading-relaxed">
                {description}
              </p>
            </div>
          </div>
        </div>
      </div>
    </li>
  );
};

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
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400">
            Ready for Production?
          </span>
        </h1>
        <p className="text-base sm:text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed">
          Codexa audits &quot;vibe-coded&quot; Spring Boot applications, detects OWASP security vulnerabilities &amp; architectural flaws, ranks findings by real risk, and computes an explainable <strong>Production Readiness Score</strong>.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-base flex items-center justify-center space-x-2 shadow-lg shadow-emerald-500/25 transition-all transform hover:-translate-y-0.5 active:scale-95"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5" />
          </button>
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-800 font-semibold text-base transition-colors flex items-center justify-center space-x-2"
          >
            <span>View GitHub Source</span>
          </a>
        </div>
      </section>

      {/* Aceternity Glowing Effect Bento Grid */}
      <section className="space-y-6 relative z-10">
        <div className="flex items-center space-x-2 px-1">
          <Sparkles className="w-4 h-4 text-emerald-400" />
          <h2 className="text-sm font-mono font-bold uppercase tracking-wider text-slate-400">
            Engine Architecture &amp; Capabilities
          </h2>
        </div>

        <ul className="grid grid-cols-1 grid-rows-none gap-4 md:grid-cols-12 md:grid-rows-3 lg:gap-4 xl:max-h-[34rem] xl:grid-rows-2">
          <GridItem
            area="md:[grid-area:1/1/2/7] xl:[grid-area:1/1/2/5]"
            icon={<Lock className="h-5 w-5" />}
            title="OWASP Top 10 Security"
            description="Deterministic JavaParser AST checking for SQL injection, command execution, hardcoded credentials, and broken access control."
          />

          <GridItem
            area="md:[grid-area:1/7/2/13] xl:[grid-area:2/1/3/5]"
            icon={<Zap className="h-5 w-5" />}
            title="Production Readiness Index"
            description="Mathematical 0–100 scoring model weighting Security (60%), Quality (25%), and Operations (15%) with strict failure caps."
          />

          <GridItem
            area="md:[grid-area:2/1/3/7] xl:[grid-area:1/5/3/8]"
            icon={<FileCode className="h-5 w-5" />}
            title="Before & After Fix Diffs"
            description="Side-by-side IDE terminal diff comparison with automatic secret masking and copyable production-ready patch code."
          />

          <GridItem
            area="md:[grid-area:2/7/3/13] xl:[grid-area:1/8/2/13]"
            icon={<Cpu className="h-5 w-5" />}
            title="Nvidia Nemotron 550B AI"
            description="State-of-the-art neural code review cascade generating non-hallucinatory explanations and contextual remediation."
          />

          <GridItem
            area="md:[grid-area:3/1/4/13] xl:[grid-area:2/8/3/13]"
            icon={<ShieldCheck className="h-5 w-5" />}
            title="Zero-Execution Sandboxing"
            description="Secure archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution."
          />
        </ul>
      </section>
    </div>
  );
}
