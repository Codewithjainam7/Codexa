import React, { useEffect, useState } from 'react';
import { 
  Sparkles, ShieldCheck, Cpu, Code2, Binary, 
  Search, FileCheck2, Zap, Flame, Shield, Activity
} from 'lucide-react';
import AnimatedBeamPipeline from './AnimatedBeamPipeline';
import DottedGlowBackground from './ui/DottedGlowBackground';
import AnimatedCircularProgressBar from './magicui/AnimatedCircularProgressBar';

export default function LiveReviewPulseLoader({ job }) {
  const [activeStepIndex, setActiveStepIndex] = useState(0);
  const [pulseLine, setPulseLine] = useState(0);

  const stages = [
    { id: 'INGESTION', label: 'Sandboxed Ingestion', icon: Binary, desc: 'Extracting archive, checking Zip Slip & safety quotas' },
    { id: 'JAVA_AST_PARSING', label: 'AST Syntax & Graph Parsing', icon: Code2, desc: 'Constructing Abstract Syntax Tree and symbol graph' },
    { id: 'SECURITY_AND_QUALITY_RULES', label: 'Static Security & Quality Rules', icon: ShieldCheck, desc: 'Evaluating OWASP Top 10 rules across all files' },
    { id: 'AI_EXPLANATION_AND_REMEDIATION', label: 'Neural AI Code Review & Remediation', icon: Sparkles, desc: 'Deep LLM audit generating fix diffs & explanations' },
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
    <div className="relative overflow-hidden bg-[#020B08]/90 border border-emerald-500/20 rounded-3xl p-6 sm:p-8 max-w-4xl mx-auto space-y-8 shadow-2xl backdrop-blur-xl">
      {/* Dotted Glow Background Matrix (Emerald Theme) */}
      <DottedGlowBackground
        className="opacity-75"
        gap={16}
        radius={1.6}
        colorDarkVar="#064e3b"
        glowColorDarkVar="#10b981"
        speedScale={1.2}
      />

      {/* Header with Glowing Orbit & Circular Progress */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-6 pb-6 border-b border-emerald-500/20">
        <div className="flex items-center space-x-4">
          <div className="relative">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-500 via-teal-500 to-emerald-700 flex items-center justify-center shadow-lg shadow-emerald-500/25 animate-pulse">
              <Sparkles className="w-7 h-7 text-white animate-spin" style={{ animationDuration: '6s' }} />
            </div>
            <div className="absolute -inset-1 rounded-2xl bg-emerald-400/20 blur-md -z-10 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
              <h2 className="text-lg font-bold text-white font-display">Live Codebase Audit in Progress</h2>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Target: <span className="font-mono text-emerald-400">{job?.sourceIdentifier}</span>
            </p>
          </div>
        </div>

        {/* Animated Circular Progress Bar */}
        <div className="flex items-center space-x-3 bg-[#020B08]/95 px-4 py-2 rounded-2xl border border-emerald-500/20 shadow-inner">
          <div className="text-right">
            <div className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Scanning</div>
            <div className="text-xs text-emerald-400 font-mono font-bold">{currentStageName}</div>
          </div>
          <AnimatedCircularProgressBar
            value={job?.progressPercent || 15}
            gaugePrimaryColor="#10b981"
            gaugeSecondaryColor="rgba(255, 255, 255, 0.08)"
            className="size-14 sm:size-16"
          />
        </div>
      </div>

      {/* Animated Beam Architecture Pipeline */}
      <div>
        <div className="flex items-center justify-between mb-3 px-1">
          <span className="text-xs font-mono font-bold text-slate-400 uppercase tracking-wider flex items-center space-x-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>Neural Review Data-Flow Architecture</span>
          </span>
          <span className="text-[11px] font-mono text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20">
            Nvidia Nemotron 550B Engine
          </span>
        </div>
        <AnimatedBeamPipeline currentStage={currentStageName} sourceIdentifier={job?.sourceIdentifier} />
      </div>

      {/* Modern Stage Stepper */}
      <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
        {stages.map((st, i) => {
          const Icon = st.icon;
          const isDone = i < activeStepIndex;
          const isCurrent = i === activeStepIndex;
          
          return (
            <div 
              key={st.id} 
              className={`p-3.5 rounded-2xl border transition-all duration-300 flex flex-col justify-between space-y-3 ${
                isCurrent 
                  ? 'bg-emerald-950/30 border-emerald-500/50 shadow-lg shadow-emerald-500/15' 
                  : isDone 
                  ? 'bg-[#020B08]/80 border-emerald-500/20 opacity-80' 
                  : 'bg-[#020B08]/40 border-slate-900 opacity-40'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className={`w-7 h-7 rounded-xl flex items-center justify-center text-xs font-bold ${
                  isCurrent 
                    ? 'bg-gradient-to-tr from-emerald-500 to-teal-600 text-white shadow-md shadow-emerald-500/30' 
                    : isDone 
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' 
                    : 'bg-slate-800 text-slate-500'
                }`}>
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-[10px] font-mono font-bold text-slate-500">0{i + 1}</span>
              </div>

              <div>
                <div className={`text-xs font-bold leading-tight ${isCurrent ? 'text-emerald-300' : isDone ? 'text-slate-200' : 'text-slate-500'}`}>
                  {st.label}
                </div>
                {isCurrent && (
                  <p className="text-[10px] text-emerald-400/80 mt-1 leading-normal font-medium">
                    {st.desc}
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Simulated Live Scan Activity Terminal */}
      <div className="bg-[#020B08] border border-emerald-500/20 rounded-2xl p-4 font-mono text-xs text-slate-400 space-y-2">
        <div className="flex items-center justify-between pb-2 border-b border-emerald-500/20 text-[11px] text-slate-500">
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500/60 inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500/60 inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/60 inline-block" />
            <span className="ml-2 font-medium text-slate-400">Codexa Real-Time Pipeline Stream</span>
          </div>
          <span className="text-[10px] text-emerald-400 font-bold tracking-wider uppercase">Active Engine</span>
        </div>

        <div className="space-y-1 text-slate-300 py-1">
          <div className="flex items-center space-x-2 text-emerald-400">
            <span className="animate-pulse">❯</span>
            <span>{terminalLines[pulseLine]}</span>
          </div>
          <div className="text-[11px] text-slate-500 pl-4">
            Stage: <span className="text-slate-400">{currentStageName}</span> &bull; Active Workers: <span className="text-slate-400">4 Core AST Threads</span>
          </div>
        </div>
      </div>
    </div>
  );
}
