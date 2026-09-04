import React from 'react';
import { Shield, Sparkles, BookOpen, Github, Plus, Layers } from 'lucide-react';

export default function Header({ currentView, setCurrentView, isConnected }) {
  return (
    <header className="sticky top-3 z-50 w-full px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto transition-all duration-300">
      <div className="theme-glass rounded-[26px] h-16 px-4 sm:px-6 flex items-center justify-between gap-4 shadow-[0_15px_40px_rgba(0,23,31,0.8)]">
        {/* Brand Logo & Squircle Icon */}
        <div 
          className="flex items-center space-x-3 cursor-pointer group select-none" 
          onClick={() => setCurrentView('landing')}
        >
          <div className="relative">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-[#00A8E8] via-[#007EA7] to-[#003459] p-[1.5px] shadow-[0_0_20px_rgba(0,168,232,0.4)] group-hover:shadow-[0_0_30px_rgba(0,168,232,0.7)] transition-all duration-300">
              <div className="w-full h-full bg-[#00171F]/90 backdrop-blur-xl rounded-[14px] flex items-center justify-center">
                <Shield className="w-5 h-5 text-[#00A8E8] group-hover:scale-110 transition-transform duration-300" />
              </div>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-[#00A8E8] rounded-full border-2 border-[#00171F] flex items-center justify-center shadow-[0_0_8px_#00A8E8]">
              <span className="w-1 h-1 bg-white rounded-full animate-ping" />
            </div>
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <span className="text-base sm:text-lg font-bold font-display tracking-tight text-white group-hover:text-[#00A8E8] transition-colors">
                CODEXA
              </span>
              <span className="px-2 py-0.5 text-[9px] font-mono font-bold tracking-wider uppercase bg-[#00A8E8]/15 text-[#00A8E8] border border-[#00A8E8]/30 rounded-full backdrop-blur-md shadow-sm">
                v2.4
              </span>
            </div>
            <p className="text-[10px] text-slate-400 font-sans hidden md:block tracking-wide">
              Deterministic AST &amp; Neural Security Auditor
            </p>
          </div>
        </div>

        {/* Center & Right Navigation Actions */}
        <nav className="flex items-center space-x-2 sm:space-x-3">
          {/* Segmented View Switcher Capsule */}
          <div className="flex items-center p-1 bg-[#00171F]/90 border border-[#007EA7]/30 rounded-2xl backdrop-blur-2xl shadow-inner">
            <button
              onClick={() => setCurrentView('landing')}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold font-display transition-all duration-200 ${
                currentView === 'landing'
                  ? 'bg-white/15 text-white shadow-[0_2px_10px_rgba(0,0,0,0.3)] border border-white/20'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold font-display transition-all duration-200 flex items-center space-x-1.5 ${
                currentView === 'upload'
                  ? 'bg-[#00A8E8]/25 text-[#00A8E8] border border-[#00A8E8]/40 shadow-[0_0_15px_rgba(0,168,232,0.3)]'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <span>Audit</span>
            </button>
          </div>

          {/* Quick API Docs Link */}
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-[#007EA7]/30 text-xs font-medium text-slate-300 hover:text-white transition-all backdrop-blur-xl"
          >
            <BookOpen className="w-3.5 h-3.5 text-[#00A8E8]" />
            <span>API</span>
          </a>

          {/* GitHub Source Link */}
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-[#007EA7]/30 text-xs font-medium text-slate-300 hover:text-white transition-all backdrop-blur-xl"
          >
            <Github className="w-3.5 h-3.5 text-slate-300" />
            <span>GitHub</span>
          </a>

          {/* Primary Action Button (New Audit) */}
          <button
            onClick={() => setCurrentView('upload')}
            className="relative group overflow-hidden px-4 py-2 rounded-xl bg-gradient-to-r from-[#00A8E8] via-[#007EA7] to-[#003459] text-white font-display font-bold text-xs shadow-[0_0_20px_rgba(0,168,232,0.4)] hover:shadow-[0_0_30px_rgba(0,168,232,0.7)] border border-white/20 transition-all duration-300 transform hover:scale-105 active:scale-95 cursor-pointer flex items-center space-x-1.5 backdrop-blur-xl"
          >
            <Plus className="w-3.5 h-3.5 text-white stroke-[3]" />
            <span className="tracking-tight">New Audit</span>
          </button>

          {/* Realtime Backend Status Beacon */}
          <div className="pl-1 sm:pl-2 flex items-center space-x-2 text-xs border-l border-[#007EA7]/30">
            <span className="relative flex h-2.5 w-2.5">
              {isConnected && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#00A8E8] opacity-75" />
              )}
              <span
                className={`relative inline-flex rounded-full h-2.5 w-2.5 ${
                  isConnected
                    ? 'bg-[#00A8E8] shadow-[0_0_12px_#00A8E8]'
                    : 'bg-amber-400 animate-pulse'
                }`}
              />
            </span>
            <span className="text-[10px] font-mono text-slate-400 hidden xl:inline">
              {isConnected ? 'Active' : 'Connecting'}
            </span>
          </div>
        </nav>
      </div>
    </header>
  );
}
