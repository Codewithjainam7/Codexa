"use client";

import React, { useEffect, useState } from 'react';
import { 
  ShieldCheck, Cpu, Code2, Binary, 
  Search, FileCheck2, Zap, Flame, Shield, Activity, Layers,
  ChevronRight
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

  const activeStage = stages[activeStepIndex] || stages[0];
  const ActiveIcon = activeStage.icon;

  return (
    <div className="relative overflow-hidden cdx-glass-card rounded-2xl sm:rounded-3xl p-4 sm:p-8 max-w-4xl mx-auto space-y-4 sm:space-y-6 shadow-xl backdrop-blur-2xl border border-[var(--border-glass)]">
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

      {/* Header with Glowing Orbit & Circular Progress (Mobile Optimized) */}
      <div className="flex items-center justify-between gap-3 pb-3 sm:pb-6 border-b border-[var(--border-subtle)] relative z-10">
        <div className="flex items-center space-x-2.5 sm:space-x-4 min-w-0">
          <div className="relative shrink-0">
            <div className="w-10 h-10 sm:w-14 sm:h-14 rounded-xl sm:rounded-2xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-blue-500 flex items-center justify-center shadow-md sm:shadow-lg shadow-blue-500/25 animate-pulse">
              <ShieldCheck className="w-5 h-5 sm:w-7 sm:h-7 text-white dark:text-slate-950" />
            </div>
            <div className="absolute -inset-1 rounded-xl sm:rounded-2xl bg-blue-400/20 blur-sm -z-10 animate-pulse" />
          </div>
          <div className="text-left min-w-0">
            <div className="flex items-center space-x-1.5 sm:space-x-2">
              <span className="w-2 h-2 rounded-full bg-blue-500 animate-ping shrink-0" />
              <h2 className="text-xs sm:text-lg font-bold text-slate-950 dark:text-white font-display truncate">
                Audit in Progress
              </h2>
            </div>
            <p className="text-[10px] sm:text-xs text-slate-500 dark:text-slate-400 mt-0.5 truncate font-mono">
              <span className="text-blue-600 dark:text-blue-400 font-semibold">{job?.sourceIdentifier || 'Codebase'}</span>
            </p>
          </div>
        </div>

        {/* Animated Circular Progress Bar (Compact on Mobile) */}
        <div className="flex items-center space-x-2 sm:space-x-3 cdx-recessed px-2.5 sm:px-4 py-1.5 sm:py-2 rounded-xl sm:rounded-2xl border border-[var(--border-subtle)] shrink-0">
          <div className="text-right hidden xs:block">
            <div className="text-[9px] sm:text-[10px] text-slate-500 uppercase tracking-wider font-bold">Scanning</div>
            <div className="text-[10px] sm:text-xs text-blue-600 dark:text-blue-400 font-mono font-bold truncate max-w-[90px]">{currentStageName}</div>
          </div>
          <AnimatedCircularProgressBar
            value={job?.progressPercent || 15}
            gaugePrimaryColor={isDark ? "#3B82F6" : "#2563EB"}
            gaugeSecondaryColor={isDark ? "rgba(255, 255, 255, 0.08)" : "rgba(0, 0, 0, 0.06)"}
            className="size-9 sm:size-14"
          />
        </div>
      </div>

      {/* Magic UI Animated Beam Architecture Pipeline (Desktop/Tablet Only) */}
      <div className="hidden sm:block relative z-10 space-y-3">
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

      {/* MOBILE COMPACT ACTIVE STAGE CARD (<640px) */}
      <div className="sm:hidden relative z-10 space-y-2.5">
        {/* Step Indicator Progress Bar */}
        <div className="flex items-center justify-between px-1 text-[10px] font-mono font-bold text-slate-500">
          <span>STAGE {activeStepIndex + 1} OF 5</span>
          <span className="text-blue-500 font-semibold">{job?.progressPercent || 15}% COMPLETE</span>
        </div>
        
        <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-800 rounded-full overflow-hidden">
          <div 
            className="h-full bg-blue-500 transition-all duration-500 rounded-full shadow-[0_0_8px_#3b82f6]"
            style={{ width: `${Math.max(15, job?.progressPercent || ((activeStepIndex + 1) * 20))}%` }}
          />
        </div>

        {/* Current Active Step Highlight Card */}
        <div className="p-3 rounded-xl bg-blue-500/15 border border-blue-500/40 shadow-sm flex items-start space-x-3 text-left">
          <div className="w-8 h-8 rounded-lg bg-blue-500 text-white flex items-center justify-center shrink-0 shadow-sm shadow-blue-500/30 mt-0.5">
            <ActiveIcon className="w-4 h-4" />
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-xs font-bold font-display text-blue-600 dark:text-blue-400 truncate">
              {activeStage.label}
            </div>
            <p className="text-[10px] text-slate-600 dark:text-slate-300 mt-0.5 leading-snug">
              {activeStage.desc}
            </p>
          </div>
        </div>
      </div>

      {/* DESKTOP STAGE STEPPER (>=640px) */}
      <div className="hidden sm:grid grid-cols-5 gap-3 relative z-10 text-left">
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

      {/* Live Scan Activity Terminal Stream (Compact on Mobile) */}
      <div className="cdx-recessed border border-[var(--border-subtle)] rounded-xl sm:rounded-2xl p-3 sm:p-4 font-mono text-xs text-slate-700 dark:text-slate-300 space-y-1.5 sm:space-y-2 relative z-10 text-left">
        <div className="flex items-center justify-between pb-1.5 sm:pb-2 border-b border-[var(--border-subtle)] text-[10px] sm:text-[11px] text-slate-600 dark:text-slate-400">
          <div className="flex items-center space-x-1.5">
            <span className="w-2 h-2 rounded-full bg-rose-500/80 inline-block" />
            <span className="w-2 h-2 rounded-full bg-amber-500/80 inline-block" />
            <span className="w-2 h-2 rounded-full bg-emerald-500/80 inline-block" />
            <span className="ml-1 sm:ml-2 font-semibold text-slate-800 dark:text-slate-200 truncate">Live Scan Stream</span>
          </div>
          <span className="text-[9px] sm:text-[10px] text-blue-700 dark:text-blue-400 font-bold tracking-wider uppercase">Active Engine</span>
        </div>

        <div className="space-y-1 text-slate-700 dark:text-slate-300 py-0.5">
          <div className="flex items-center space-x-1.5 sm:space-x-2 text-blue-700 dark:text-blue-400 font-semibold text-[11px] sm:text-xs">
            <span className="animate-pulse font-bold shrink-0">❯</span>
            <span className="truncate">{terminalLines[pulseLine]}</span>
          </div>
          <div className="text-[10px] sm:text-[11px] text-slate-600 dark:text-slate-400 pl-3.5 sm:pl-4 truncate">
            Stage: <span className="text-slate-900 dark:text-white font-bold">{currentStageName}</span> &bull; 4 Core AST Workers
          </div>
        </div>
      </div>
    </div>
  );
}
