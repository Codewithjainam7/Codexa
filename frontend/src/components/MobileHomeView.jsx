"use client";
import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, Plus, ArrowRight, Zap, 
  Lock, Cpu, FileCode2, Activity, CheckCircle2, ChevronRight,
  UploadCloud, Terminal, ShieldAlert, GitBranch
} from 'lucide-react';
import { getActiveAvatar } from '../lib/avatars';

export default function MobileHomeView({ onStartAnalysis, onViewResults, activeJobId, isConnected }) {
  const [avatar, setAvatar] = useState(getActiveAvatar());

  useEffect(() => {
    const handleAvatarChange = () => setAvatar(getActiveAvatar());
    window.addEventListener('codexa_avatar_updated', handleAvatarChange);
    return () => window.removeEventListener('codexa_avatar_updated', handleAvatarChange);
  }, []);

  const AvatarIcon = avatar.icon;

  const quickSamples = [
    { name: 'Juice Shop', tag: 'OWASP / Node', url: 'https://github.com/juice-shop/juice-shop' },
    { name: 'Spring PetClinic', tag: 'Enterprise Java', url: 'https://github.com/spring-projects/spring-petclinic' },
    { name: 'FastAPI App', tag: 'Python API', url: 'https://github.com/nsidnev/fastapi-realworld-example-app' }
  ];

  return (
    <div className="space-y-4 pb-6 pt-1 text-left select-none font-sans">
      {/* 1. Developer Profile & Engine Status Header */}
      <div className="p-3.5 rounded-2xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div className={`w-9 h-9 rounded-xl bg-gradient-to-br ${avatar.color} p-[1.5px] shadow-sm`}>
            <div className="w-full h-full bg-slate-100 dark:bg-[#070A12] rounded-[10px] flex items-center justify-center p-0.5 overflow-hidden">
              <AvatarIcon className={`w-4 h-4 ${avatar.text}`} />
            </div>
          </div>
          <div>
            <div className="flex items-center space-x-1.5">
              <h2 className="text-xs font-bold font-display text-slate-900 dark:text-white tracking-tight">
                {avatar.name}
              </h2>
              <span className="px-1.5 py-0.2 text-[8px] font-mono font-bold uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/30 rounded">
                {avatar.role}
              </span>
            </div>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 font-mono">
              CODEXA Mobile Engine &bull; v2.4
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-full bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-[10px] font-mono font-bold">
          <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]' : 'bg-rose-500'}`} />
          <span className={isConnected ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'}>
            {isConnected ? 'LIVE' : 'OFFLINE'}
          </span>
        </div>
      </div>

      {/* 1-Tap Quick Sample Repositories */}
      <div className="space-y-1.5">
        <div className="flex items-center justify-between px-1">
          <span className="text-[10px] font-mono font-bold uppercase tracking-wider text-slate-600 dark:text-slate-400">
            1-Tap Quick Audits
          </span>
          <span className="text-[9px] font-mono text-blue-600 dark:text-blue-400 font-semibold">
            Tap to Load
          </span>
        </div>

        <div className="grid grid-cols-3 gap-1.5">
          {quickSamples.map((sample) => (
            <button
              key={sample.name}
              type="button"
              onClick={() => onStartAnalysis(sample.url)}
              className="p-2 rounded-xl bg-white dark:bg-[#0D121F] hover:bg-slate-50 dark:hover:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-blue-500/40 text-left transition-all active:scale-95 cursor-pointer flex flex-col justify-between shadow-sm"
            >
              <div className="flex items-center space-x-1 text-blue-600 dark:text-blue-400 mb-1">
                <GitBranch className="w-3 h-3 shrink-0" />
                <span className="text-[10.5px] font-bold font-display text-slate-900 dark:text-white truncate">
                  {sample.name}
                </span>
              </div>
              <span className="text-[8.5px] font-mono text-slate-500 dark:text-slate-400 truncate">
                {sample.tag}
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* 2. Primary Action Card */}
      <div className="rounded-2xl p-4 sm:p-5 bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 transition-all shadow-md relative overflow-hidden space-y-3.5">
        {/* Top subtle blue accent line */}
        <div className="absolute top-0 inset-x-0 h-0.5 bg-gradient-to-r from-blue-500 via-indigo-500 to-transparent" />

        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Terminal className="w-4 h-4 text-blue-600 dark:text-blue-400" />
            <span className="text-[11px] font-mono font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider">
              Code Security Audit
            </span>
          </div>
          <span className="text-[10px] font-mono text-slate-500 dark:text-slate-400">
            OWASP Top 10 &bull; AST
          </span>
        </div>

        <div>
          <h3 className="text-base font-bold font-display text-slate-900 dark:text-white tracking-tight">
            Run Security Inspection
          </h3>
          <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 leading-relaxed font-sans">
            Audit your codebase for SQLi, RCE, IDOR, and hardcoded secrets with AI remediation diffs.
          </p>
        </div>

        <div className="pt-1 flex items-center gap-2">
          <button
            onClick={onStartAnalysis}
            className="cdx-btn-primary flex-1 py-2.5 px-4 rounded-xl font-display font-bold text-xs flex items-center justify-center space-x-2 shadow-md shadow-blue-500/25 active:scale-95 transition-transform cursor-pointer"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>Start Code Audit</span>
          </button>

          {activeJobId && (
            <button
              onClick={onViewResults}
              className="py-2.5 px-3.5 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-300 hover:text-slate-950 dark:hover:text-white font-display font-semibold text-xs flex items-center space-x-1.5 active:scale-95 transition-transform cursor-pointer"
            >
              <span>Results</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>

      {/* 3. Realtime Engine Telemetry Bar */}
      <div className="grid grid-cols-4 gap-1.5 sm:gap-2 text-center">
        <div className="p-2 sm:p-2.5 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="text-[8px] sm:text-[9px] font-mono text-slate-500 dark:text-slate-400 uppercase">AST Engine</div>
          <div className="text-[11px] sm:text-xs font-bold font-mono text-emerald-600 dark:text-emerald-400 mt-0.5">READY</div>
        </div>
        <div className="p-2 sm:p-2.5 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="text-[8px] sm:text-[9px] font-mono text-slate-500 dark:text-slate-400 uppercase">Maintainability</div>
          <div className="text-[11px] sm:text-xs font-bold font-mono text-blue-600 dark:text-blue-400 mt-0.5 truncate">Tracked</div>
        </div>
        <div className="p-2 sm:p-2.5 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="text-[8px] sm:text-[9px] font-mono text-slate-500 dark:text-slate-400 uppercase">AI Reviewer</div>
          <div className="text-[11px] sm:text-xs font-bold font-mono text-indigo-600 dark:text-indigo-400 mt-0.5 truncate">Nemotron</div>
        </div>
        <div className="p-2 sm:p-2.5 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="text-[8px] sm:text-[9px] font-mono text-slate-500 dark:text-slate-400 uppercase">Ruleset</div>
          <div className="text-[11px] sm:text-xs font-bold font-mono text-amber-600 dark:text-amber-400 mt-0.5">19+ AST</div>
        </div>
      </div>

      {/* 4. Active Audit Job Card */}
      {activeJobId && (
        <div 
          onClick={onViewResults}
          className="p-3.5 rounded-xl bg-blue-500/10 border border-blue-500/30 flex items-center justify-between cursor-pointer active:scale-95 transition-transform"
        >
          <div className="flex items-center space-x-2.5">
            <Activity className="w-4 h-4 text-blue-600 dark:text-blue-400" />
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white font-display">Active Audit Report</div>
              <div className="text-[10px] font-mono text-blue-600 dark:text-blue-400">ID: {activeJobId.substring(0, 12)}...</div>
            </div>
          </div>
          <span className="text-xs font-mono font-bold text-blue-600 dark:text-blue-400 flex items-center">
            View <ChevronRight className="w-3.5 h-3.5 ml-0.5" />
          </span>
        </div>
      )}

      {/* 5. Core Vulnerability Scopes */}
      <div className="space-y-2">
        <span className="text-xs font-mono font-bold uppercase tracking-wider text-slate-600 dark:text-slate-400 px-1">
          Inspection Rules
        </span>

        <div className="grid grid-cols-2 gap-2">
          <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-1 shadow-sm">
            <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-800 dark:text-slate-200 font-display">
              <Lock className="w-3.5 h-3.5 text-rose-500 dark:text-rose-400" />
              <span>Injection</span>
            </div>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-snug">
              SQLi &bull; NoSQLi &bull; Command Exec
            </p>
          </div>

          <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-1 shadow-sm">
            <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-800 dark:text-slate-200 font-display">
              <ShieldCheck className="w-3.5 h-3.5 text-amber-500 dark:text-amber-400" />
              <span>Auth &amp; Secrets</span>
            </div>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-snug">
              API Keys &bull; JWT &bull; Broken ACL
            </p>
          </div>

          <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-1 shadow-sm">
            <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-800 dark:text-slate-200 font-display">
              <FileCode2 className="w-3.5 h-3.5 text-blue-500 dark:text-blue-400" />
              <span>Code Quality</span>
            </div>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-snug">
              Swallowed Exceptions &bull; AST Smells
            </p>
          </div>

          <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-1 shadow-sm">
            <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-800 dark:text-slate-200 font-display">
              <Zap className="w-3.5 h-3.5 text-indigo-500 dark:text-indigo-400" />
              <span>Hardening</span>
            </div>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-snug">
              CORS &bull; Rate Limits &bull; Headers
            </p>
          </div>
        </div>
      </div>

      {/* 6. Supported Stacks Strip */}
      <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm flex items-center justify-between text-xs">
        <span className="text-slate-500 dark:text-slate-400 font-mono text-[10px]">SUPPORTED</span>
        <div className="flex items-center space-x-2 text-[10px] font-mono text-slate-700 dark:text-slate-300 font-medium">
          <span>Java</span>
          <span>&bull;</span>
          <span>Spring</span>
          <span>&bull;</span>
          <span>TypeScript</span>
          <span>&bull;</span>
          <span>Python</span>
          <span>&bull;</span>
          <span>Go</span>
        </div>
      </div>
    </div>
  );
}
