"use client";
import React from 'react';
import { 
  Shield, Sun, Moon, Activity, Cpu, 
  BookOpen, Github, ExternalLink, Info, CheckCircle2 
} from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function MobileSettingsView({ isConnected, limits }) {
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="max-w-xl mx-auto space-y-5 pb-8 pt-2 text-left">
      {/* Header Title */}
      <div className="flex items-center space-x-3 pb-2 border-b border-[var(--border-subtle)]">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-sm">
          <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center p-1">
            <img src="/logo.png" alt="CODEXA" className="w-full h-full object-contain" />
          </div>
        </div>
        <div>
          <h2 className="text-lg font-bold font-display text-[var(--text-primary)]">App Settings</h2>
          <p className="text-xs text-slate-500 font-mono">System configuration & engine health</p>
        </div>
      </div>

      {/* 1. Interface Theme Card */}
      <div className="cdx-glass-card rounded-2xl p-4 border border-[var(--border-subtle)] space-y-3 shadow-sm">
        <div className="flex items-center justify-between">
          <span className="text-xs font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
            Appearance
          </span>
          <span className="text-[11px] font-mono text-blue-500 font-bold uppercase">
            {theme} Mode
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2.5">
          <button
            onClick={() => { if (theme !== 'light') toggleTheme(); }}
            className={`p-3 rounded-xl border flex items-center justify-center space-x-2 transition-all cursor-pointer ${
              theme === 'light'
                ? 'bg-blue-500/15 border-blue-500 text-blue-600 font-bold shadow-sm'
                : 'bg-[var(--bg-recessed)] border-[var(--border-subtle)] text-slate-500 hover:text-slate-900'
            }`}
          >
            <Sun className="w-4 h-4 text-amber-500" />
            <span className="text-xs font-display">Light Mode</span>
          </button>

          <button
            onClick={() => { if (theme !== 'dark') toggleTheme(); }}
            className={`p-3 rounded-xl border flex items-center justify-center space-x-2 transition-all cursor-pointer ${
              theme === 'dark'
                ? 'bg-blue-500/20 border-blue-500 text-blue-400 font-bold shadow-sm'
                : 'bg-[var(--bg-recessed)] border-[var(--border-subtle)] text-slate-400 hover:text-white'
            }`}
          >
            <Moon className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-display">Dark Mode</span>
          </button>
        </div>
      </div>

      {/* 2. Backend Health & AI Engine Card */}
      <div className="cdx-glass-card rounded-2xl p-4 border border-[var(--border-subtle)] space-y-3 shadow-sm">
        <span className="text-xs font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
          Engine Connectivity
        </span>

        <div className="space-y-2.5">
          <div className="flex items-center justify-between p-2.5 rounded-xl bg-[var(--bg-recessed)]">
            <div className="flex items-center space-x-2.5">
              <Activity className="w-4 h-4 text-blue-500" />
              <span className="text-xs font-medium text-[var(--text-primary)]">Backend Security API</span>
            </div>
            <span className="flex items-center space-x-1.5 text-[11px] font-mono font-bold">
              <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]' : 'bg-rose-500'}`} />
              <span className={isConnected ? 'text-emerald-500' : 'text-rose-500'}>
                {isConnected ? 'ONLINE' : 'DISCONNECTED'}
              </span>
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-[var(--bg-recessed)]">
            <div className="flex items-center space-x-2.5">
              <Cpu className="w-4 h-4 text-indigo-500" />
              <span className="text-xs font-medium text-[var(--text-primary)]">Neural LLM Engine</span>
            </div>
            <span className="text-[11px] font-mono text-blue-500 font-semibold">
              Nvidia Nemotron 550B
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-[var(--bg-recessed)]">
            <div className="flex items-center space-x-2.5">
              <Shield className="w-4 h-4 text-emerald-500" />
              <span className="text-xs font-medium text-[var(--text-primary)]">Static AST Analyzer</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-500 font-bold flex items-center space-x-1">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>ACTIVE</span>
            </span>
          </div>
        </div>
      </div>

      {/* 3. Developer Resources Card */}
      <div className="cdx-glass-card rounded-2xl p-4 border border-[var(--border-subtle)] space-y-2 shadow-sm">
        <span className="text-xs font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
          Developer Resources
        </span>

        <a
          href="http://localhost:8080/swagger-ui.html"
          target="_blank"
          rel="noreferrer"
          className="flex items-center justify-between p-3 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] text-xs font-semibold text-[var(--text-primary)] transition-all"
        >
          <div className="flex items-center space-x-2.5">
            <BookOpen className="w-4 h-4 text-blue-500" />
            <span>OpenAPI Swagger Specification</span>
          </div>
          <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
        </a>

        <a
          href="https://github.com/Codewithjainam7/Codexa"
          target="_blank"
          rel="noreferrer"
          className="flex items-center justify-between p-3 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] text-xs font-semibold text-[var(--text-primary)] transition-all"
        >
          <div className="flex items-center space-x-2.5">
            <Github className="w-4 h-4 text-slate-400" />
            <span>GitHub Open Source Repository</span>
          </div>
          <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
        </a>
      </div>

      {/* 4. App Info Card */}
      <div className="p-3.5 rounded-2xl bg-[var(--bg-recessed)]/60 border border-[var(--border-subtle)] flex items-center justify-between text-xs font-mono text-slate-400">
        <div className="flex items-center space-x-2">
          <Info className="w-3.5 h-3.5 text-blue-500" />
          <span>CODEXA Mobile Edition</span>
        </div>
        <span>v2.4.0 (Build 2026.09)</span>
      </div>
    </div>
  );
}
