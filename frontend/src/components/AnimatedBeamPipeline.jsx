"use client";

import React, { forwardRef, useRef } from "react";
import { cn } from "@/lib/utils";
import { AnimatedBeam } from "@/components/magicui/AnimatedBeam";
import { 
  GitBranch, FolderArchive, Shield, Sparkles, 
  Code2, ShieldAlert, Cpu, Zap, CheckCircle2
} from "lucide-react";
import { useTheme } from "@/context/ThemeContext";

const NodeCircle = forwardRef(({ className, children, label, active, completed }, ref) => {
  return (
    <div className="flex flex-col items-center space-y-1.5 z-10">
      <div
        ref={ref}
        className={cn(
          "relative flex size-12 sm:size-14 items-center justify-center rounded-2xl border-2 transition-all duration-500",
          active 
            ? "border-blue-500 bg-blue-500/15 text-blue-600 dark:text-blue-400 shadow-[0_0_25px_rgba(59,130,246,0.4)] scale-110"
            : completed
            ? "border-emerald-500/60 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 shadow-[0_0_15px_rgba(16,185,129,0.15)]"
            : "border-[var(--border-subtle)] bg-[var(--bg-recessed)] text-slate-500 hover:border-blue-500/30",
          className
        )}
      >
        {children}
        {completed && (
          <div className="absolute -top-1 -right-1 w-4 h-4 bg-emerald-500 rounded-full flex items-center justify-center text-white">
            <CheckCircle2 className="w-3 h-3 stroke-[2.5]" />
          </div>
        )}
      </div>
      {label && (
        <span className={cn(
          "text-[10px] font-mono tracking-tight text-center max-w-[90px] truncate leading-tight font-medium transition-colors",
          active ? "text-blue-700 dark:text-blue-400 font-bold" : completed ? "text-slate-700 dark:text-slate-300" : "text-slate-500"
        )}>
          {label}
        </span>
      )}
    </div>
  );
});

NodeCircle.displayName = "NodeCircle";

export function AnimatedBeamPipeline({ currentStage = "INGESTION", sourceIdentifier = "Repository" }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const containerRef = useRef(null);
  
  // Left Input Nodes
  const input1Ref = useRef(null);
  const input2Ref = useRef(null);
  const input3Ref = useRef(null);

  // Center Hub Node
  const centerHubRef = useRef(null);

  // Right Output Engine Nodes
  const out1Ref = useRef(null);
  const out2Ref = useRef(null);
  const out3Ref = useRef(null);
  const out4Ref = useRef(null);

  const stageOrder = [
    'INGESTION',
    'JAVA_AST_PARSING',
    'SECURITY_AND_QUALITY_RULES',
    'AI_EXPLANATION_AND_REMEDIATION',
    'PRIORITIZATION_AND_SCORING'
  ];
  
  const currentIdx = stageOrder.indexOf(currentStage);

  return (
    <div
      ref={containerRef}
      className="relative flex w-full max-w-3xl mx-auto items-center justify-between p-6 sm:p-10 rounded-3xl cdx-elevated border border-[var(--border-subtle)] shadow-2xl overflow-hidden backdrop-blur-2xl"
    >
      {/* Background Decorative Ambient Radial Gradients */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -top-24 -left-24 w-72 h-72 bg-blue-500/5 rounded-full blur-3xl pointer-events-none" />

      {/* 1. Left Column: Ingestion & Input Nodes */}
      <div className="flex flex-col justify-between h-full space-y-6 sm:space-y-8 z-10">
        <NodeCircle 
          ref={input1Ref} 
          label="GitHub Source" 
          active={currentStage === 'INGESTION'} 
          completed={currentIdx > 0}
        >
          <GitBranch className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>

        <NodeCircle 
          ref={input2Ref} 
          label="ZIP Ingest" 
          active={currentStage === 'INGESTION'} 
          completed={currentIdx > 0}
        >
          <FolderArchive className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>

        <NodeCircle 
          ref={input3Ref} 
          label="OWASP Scope" 
          active={currentStage === 'INGESTION'} 
          completed={currentIdx > 0}
        >
          <Shield className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>
      </div>

      {/* 2. Center Column: Codexa Core Orchestrator Hub */}
      <div className="flex flex-col items-center justify-center z-10 px-2 sm:px-4">
        <div className="relative">
          {/* Pulsing Outer Rings */}
          <div className="absolute -inset-3 rounded-full bg-amber-500/20 blur-md animate-pulse" />
          <div className="absolute -inset-6 rounded-full border border-amber-500/30 animate-spin" style={{ animationDuration: '15s' }} />

          <div
            ref={centerHubRef}
            className="relative flex size-20 sm:size-24 items-center justify-center rounded-3xl border-2 border-amber-500 bg-gradient-to-tr from-amber-500/20 via-black/80 to-amber-600/20 p-4 shadow-[0_0_35px_rgba(245,158,11,0.35)] text-amber-500 transform transition-transform hover:scale-105"
          >
            <Sparkles className="w-10 h-10 sm:w-12 sm:h-12 text-amber-500 animate-pulse" />
            <div className="absolute bottom-1 text-[9px] font-mono font-black text-amber-500 tracking-wider">
              CODEXA
            </div>
          </div>
        </div>
        <span className="text-xs font-mono font-bold text-blue-600 dark:text-blue-400 mt-2.5 tracking-tight text-center">
          Neural Core
        </span>
      </div>

      {/* 3. Right Column: Output & Analysis Nodes */}
      <div className="flex flex-col justify-between h-full space-y-4 sm:space-y-5 z-10">
        <NodeCircle 
          ref={out1Ref} 
          label="AST Parser" 
          active={currentStage === 'JAVA_AST_PARSING'} 
          completed={currentIdx > 1}
        >
          <Code2 className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>

        <NodeCircle 
          ref={out2Ref} 
          label="Rule Engine" 
          active={currentStage === 'SECURITY_AND_QUALITY_RULES'} 
          completed={currentIdx > 2}
        >
          <ShieldAlert className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>

        <NodeCircle 
          ref={out3Ref} 
          label="Nemotron AI" 
          active={currentStage === 'AI_EXPLANATION_AND_REMEDIATION'} 
          completed={currentIdx > 3}
        >
          <Cpu className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>

        <NodeCircle 
          ref={out4Ref} 
          label="Readiness Index" 
          active={currentStage === 'PRIORITIZATION_AND_SCORING'} 
          completed={currentIdx >= 4}
        >
          <Zap className="w-5 h-5 sm:w-6 sm:h-6" />
        </NodeCircle>
      </div>

      {/* Animated Beams: Inputs -> Center Hub */}
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={input1Ref}
        toRef={centerHubRef}
        curvature={-20}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={input2Ref}
        toRef={centerHubRef}
        curvature={0}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={input3Ref}
        toRef={centerHubRef}
        curvature={20}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />

      {/* Animated Beams: Center Hub -> Output Nodes */}
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={centerHubRef}
        toRef={out1Ref}
        curvature={-30}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={centerHubRef}
        toRef={out2Ref}
        curvature={-10}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={centerHubRef}
        toRef={out3Ref}
        curvature={10}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
      <AnimatedBeam
        containerRef={containerRef}
        fromRef={centerHubRef}
        toRef={out4Ref}
        curvature={30}
        duration={2.5}
        gradientStartColor="#3B82F6"
        gradientStopColor="#2563EB"
        pathColor={isDark ? "rgba(59, 130, 246, 0.15)" : "rgba(37, 99, 235, 0.15)"}
      />
    </div>
  );
}

export default AnimatedBeamPipeline;
