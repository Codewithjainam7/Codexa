"use client";

import React, { useEffect, useState } from 'react';
import { 
  ShieldCheck, Cpu, Code2, Binary, 
  Search, FileCheck2, Zap, Flame, Shield, Activity, Layers
} from 'lucide-react';
import AnimatedBeamPipeline from './AnimatedBeamPipeline';
import DottedGlowBackground from './ui/DottedGlowBackground';
import AnimatedCircularProgressBar from './magicui/AnimatedCircularProgressBar';
import { useTheme } from '../context/ThemeContext';

export default function LiveReviewPulseLoader({ job }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [activeStepIndex, setActiveStepIndex] = useState(0);
  const [pulseLine, setPulseLine] = useState(0);

  const stages = [
    { id: 'INGESTION', label: 'Sandboxed Ingestion', icon: Binary, desc: 'Extracting archive, checking Zip Slip & safety quotas' },
    { id: 'JAVA_AST_PARSING', label: 'AST Syntax & Graph Parsing', icon: Code2, desc: 'Constructing Abstract Syntax Tree and symbol graph' },
    { id: 'SECURITY_AND_QUALITY_RULES', label: 'Static Security & Quality Rules', icon: ShieldCheck, desc: 'Evaluating OWASP Top 10 rules across all files' },
    { id: 'AI_EXPLANATION_AND_REMEDIATION', label: 'Neural AI Code Review & Remediation', icon: Cpu, desc: 'Deep LLM audit generating fix diffs & explanations' },
    { id: 'PRIORITIZATION_AND_SCORING', label: 'Production Readiness Scoring', icon: Zap, desc: 'Computing mathematical risk score & readiness index' }
  ];

  const currentStageName = job?.progressStage || 'INGESTION';

  // Determine current active step
  useEffect(() => {
    const idx = stages.findIndex(s => s.id === currentStageName);
    if (idx !== -1) {
      setActiveStepIndex(idx);
    }
  }, [currentStageName]);

  // Terminal code scanning simulation
  useEffect(() => {
    const timer = setInterval(() => {
      setPulseLine(p => (p + 1) % 12);
    }, 600);
    return () => clearInterval(timer);
  }, []);

  const terminalLines = [
    'Scanning AST node graph for tainted data-flow...',
    'Inspecting authentication handlers and session configs...',
    'Checking XSS sanitizers and DOM element bindings...',
    'Auditing child_process and command execution patterns...',
    'Analyzing exception handlers and swallowed catch blocks...',
    'Verifying CORS policy headers and allowed origins...',
    'Generating neural AI remediation patch diffs with Nemotron 550B...',
    'Calculating CVSS weights and impact probabilities...',
    'Masking credential tokens and secret payloads...',
    'Validating production readiness against enterprise baseline...',
    'Synthesizing final architectural scorecard...',
    'Almost ready! Finalizing analysis report...'
  ];

  return (
    <div className="relative overflow-hidden cdx-card rounded-3xl p-6 sm:p-8 max-w-4xl mx-auto space-y-8 shadow-2xl backdrop-blur-2xl border border-[var(--border-subtle)]">
      {/* Dotted Glow Background Matrix (Adaptive Theme) */}
      <DottedGlowBackground
        className="opacity-70 dark:opacity-50"
        gap={16}
        radius={1.6}
        colorDarkVar="#292524"
        glowColorDarkVar="#3B82F6"
        colorLightVar="#E2E8F0"
        glowColorLightVar="#2563EB"
        speedScale={1.2}
      />

      {/* Header with Glowing Orbit & Circular Progress */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-6 pb-6 border-b border-[var(--border-subtle)] relative z-10">
        <div className="flex items-center space-x-4">
          <div className="relative">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-blue-500 flex items-center justify-center shadow-lg shadow-blue-500/25 animate-pulse">
              <ShieldCheck className="w-7 h-7 text-white dark:text-slate-950" />
            </div>
            <div className="absolute -inset-1 rounded-2xl bg-blue-400/20 blur-md -z-10 animate-pulse" />
          </div>
          <div className="text-left">
            <div className="flex items-center space-x-2">
              <span className="w-2.5 h-2.5 rounded-full bg-blue-500 animate-ping" />
              <h2 className="text-lg font-bold text-slate-950 dark:text-white font-display">Live Codebase Audit in Progress</h2>
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
              Target: <span className="font-mono text-blue-600 dark:text-blue-400 font-semibold">{job?.sourceIdentifier || 'Codebase Repository'}</span>
            </p>
          </div>
        </div>

        {/* Magic UI Animated Circular Progress Bar */}
        <div className="flex items-center space-x-3 cdx-recessed px-4 py-2 rounded-2xl border border-[var(--border-subtle)] shadow-inner">
          <div className="text-right">
            <div className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Scanning</div>
            <div className="text-xs text-blue-600 dark:text-blue-400 font-mono font-bold">{currentStageName}</div>
          </div>
          <AnimatedCircularProgressBar
            value={job?.progressPercent || 15}
            gaugePrimaryColor={isDark ? "#3B82F6" : "#2563EB"}
            gaugeSecondaryColor={isDark ? "rgba(255, 255, 255, 0.08)" : "rgba(0, 0, 0, 0.06)"}
            className="size-14 sm:size-16"
          />
        </div>
      </div>

      {/* Magic UI Animated Beam Architecture Pipeline */}
      <div className="relative z-10 space-y-3">
        <div className="flex items-center justify-between px-1">
          <span className="text-xs font-mono font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider flex items-center space-x-2">
            <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
            <span>Neural Review Data-Flow Architecture</span>
          </span>
          <span className="text-[11px] font-mono text-blue-700 dark:text-blue-400 bg-blue-500/10 px-2.5 py-0.5 rounded-full border border-blue-500/20 font-semibold">
            Nvidia Nemotron 550B Engine
          </span>
        </div>
        <AnimatedBeamPipeline currentStage={currentStageName} sourceIdentifier={job?.sourceIdentifier} />
      </div>

      {/* Modern Stage Stepper */}
      <div className="grid grid-cols-1 sm:grid-cols-5 gap-3 relative z-10 text-left">
        {stages.map((st, i) => {
          const Icon = st.icon;
          const isDone = i < activeStepIndex;
          const isCurrent = i === activeStepIndex;
          
          return (
            <div 
              key={st.id} 
              className={`p-3.5 rounded-2xl border transition-all duration-300 flex flex-col justify-between space-y-3 ${
                isCurrent 
                  ? 'bg-blue-500/15 border-blue-500/50 shadow-lg shadow-blue-500/15' 
                  : isDone 
                  ? 'bg-emerald-500/10 border-emerald-500/30' 
                  : 'cdx-recessed opacity-60 border-[var(--border-subtle)]'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className={`w-7 h-7 rounded-xl flex items-center justify-center text-xs font-bold ${
                  isCurrent 
                    ? 'bg-gradient-to-tr from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/30' 
                    : isDone 
                    ? 'bg-emerald-500/20 text-emerald-700 dark:text-emerald-400 border border-emerald-500/30' 
                    : 'bg-black/10 dark:bg-white/10 text-slate-400'
                }`}>
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-[10px] font-mono font-bold text-slate-500">0{i + 1}</span>
              </div>

              <div>
                <div className={`text-xs font-bold leading-tight ${
                  isCurrent 
                    ? 'text-blue-700 dark:text-blue-400' 
                    : isDone 
                    ? 'text-emerald-700 dark:text-emerald-400' 
                    : 'text-slate-600 dark:text-slate-400'
                }`}>
                  {st.label}
                </div>
                {isCurrent && (
                  <p className="text-[10px] text-blue-700/90 dark:text-blue-300/90 mt-1 leading-normal font-medium">
                    {st.desc}
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Simulated Live Scan Activity Terminal */}
      <div className="cdx-recessed border border-[var(--border-subtle)] rounded-2xl p-4 font-mono text-xs text-slate-600 dark:text-slate-400 space-y-2 relative z-10 text-left">
        <div className="flex items-center justify-between pb-2 border-b border-[var(--border-subtle)] text-[11px] text-slate-500">
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500/80 inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500/80 inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/80 inline-block" />
            <span className="ml-2 font-semibold text-slate-700 dark:text-slate-300">Codexa Real-Time Pipeline Stream</span>
          </div>
          <span className="text-[10px] text-blue-600 dark:text-blue-400 font-bold tracking-wider uppercase">Active Engine</span>
        </div>

        <div className="space-y-1 text-slate-700 dark:text-slate-300 py-1">
          <div className="flex items-center space-x-2 text-blue-600 dark:text-blue-400 font-semibold">
            <span className="animate-pulse font-bold">❯</span>
            <span>{terminalLines[pulseLine]}</span>
          </div>
          <div className="text-[11px] text-slate-500 pl-4">
            Stage: <span className="text-slate-700 dark:text-slate-300 font-semibold">{currentStageName}</span> &bull; Active Workers: <span className="text-slate-700 dark:text-slate-300">4 Core AST Threads</span>
          </div>
        </div>
      </div>
    </div>
  );
}
