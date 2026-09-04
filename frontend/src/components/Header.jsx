import React from 'react';
import { Shield, Sparkles, BookOpen, Github, Cpu, Plus } from 'lucide-react';

export default function Header({ currentView, setCurrentView, isConnected }) {
  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-2xl transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <div 
          className="flex items-center space-x-3 cursor-pointer group select-none" 
          onClick={() => setCurrentView('landing')}
        >
          <div className="relative">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 via-blue-600 to-violet-600 p-[1px] shadow-lg shadow-cyan-500/20 group-hover:shadow-cyan-500/40 transition-all duration-300">
              <div className="w-full h-full bg-slate-950 rounded-[11px] flex items-center justify-center">
                <Shield className="w-5 h-5 text-cyan-400 group-hover:scale-110 transition-transform duration-300" />
              </div>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-cyan-500 rounded-full border-2 border-slate-950 flex items-center justify-center">
              <span className="w-1 h-1 bg-white rounded-full animate-ping" />
            </div>
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <span className="text-lg sm:text-xl font-black font-display tracking-tight text-white group-hover:text-cyan-300 transition-colors">
                CODEXA
              </span>
              <span className="px-2 py-0.5 text-[10px] font-mono font-bold tracking-wider uppercase bg-cyan-500/10 text-cyan-300 border border-cyan-500/30 rounded-full shadow-sm">
                v2.4 AST
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-sans hidden md:block">
              AI Code Review &amp; Security Auditor
            </p>
          </div>
        </div>

        {/* Center & Right Navigation Actions */}
        <nav className="flex items-center space-x-2 sm:space-x-3">
          {/* View Switcher Capsule */}
          <div className="flex items-center p-1 bg-slate-900/90 border border-slate-800 rounded-xl">
            <button
              onClick={() => setCurrentView('landing')}
              className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all ${
                currentView === 'landing'
                  ? 'bg-slate-800 text-white shadow-sm border border-slate-700/60'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all flex items-center space-x-1.5 ${
                currentView === 'upload'
                  ? 'bg-cyan-500/15 text-cyan-300 border border-cyan-500/30 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <span>Audit Code</span>
            </button>
          </div>

          {/* Quick API Docs Link */}
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-slate-900/60 hover:bg-slate-800 border border-slate-800 text-xs font-medium text-slate-300 hover:text-white transition-all"
          >
            <BookOpen className="w-3.5 h-3.5 text-cyan-400" />
            <span>API Docs</span>
          </a>

          {/* GitHub Source Link */}
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-slate-900/60 hover:bg-slate-800 border border-slate-800 text-xs font-medium text-slate-300 hover:text-white transition-all"
          >
            <Github className="w-3.5 h-3.5 text-slate-300" />
            <span>GitHub</span>
          </a>

          {/* Primary Action Button (New Audit) */}
          <button
            onClick={() => setCurrentView('upload')}
            className="relative group overflow-hidden px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-500 via-blue-600 to-violet-600 text-white font-display font-bold text-xs shadow-lg shadow-cyan-500/25 hover:shadow-cyan-500/40 transition-all transform hover:-translate-y-0.5 active:scale-95 cursor-pointer flex items-center space-x-1.5"
          >
            <Plus className="w-3.5 h-3.5 text-white stroke-[3]" />
            <span className="tracking-tight">New Audit</span>
          </button>

          {/* Realtime Backend Status Dot */}
          <div className="pl-1 sm:pl-2 flex items-center space-x-2 text-xs border-l border-slate-800/80">
            <span className="relative flex h-2.5 w-2.5">
              {isConnected && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75" />
              )}
              <span
                className={`relative inline-flex rounded-full h-2.5 w-2.5 ${
                  isConnected
                    ? 'bg-cyan-400 shadow-[0_0_10px_rgba(34,211,238,0.9)]'
                    : 'bg-amber-400 animate-pulse'
                }`}
              />
            </span>
            <span className="text-[11px] font-mono text-slate-400 hidden xl:inline">
              {isConnected ? 'Engine Active' : 'Connecting...'}
            </span>
          </div>
        </nav>
      </div>
    </header>
  );
}
