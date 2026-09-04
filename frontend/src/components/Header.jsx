import React from 'react';
import { Shield, Sparkles, BookOpen, Github, Plus, Layers } from 'lucide-react';

export default function Header({ currentView, setCurrentView, isConnected }) {
  return (
    <header className="sticky top-3 z-50 w-full px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto transition-all">
      <div className="ios-glass rounded-[26px] h-16 px-4 sm:px-6 flex items-center justify-between gap-4 shadow-[0_15px_40px_rgba(0,0,0,0.5)]">
        {/* Brand Logo & Squircle Icon */}
        <div 
          className="flex items-center space-x-3 cursor-pointer group select-none" 
          onClick={() => setCurrentView('landing')}
        >
          <div className="relative">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-cyan-400/80 via-blue-600/70 to-violet-600/80 p-[1.5px] shadow-[0_0_20px_rgba(6,182,212,0.35)] group-hover:shadow-[0_0_30px_rgba(6,182,212,0.6)] transition-all duration-300">
              <div className="w-full h-full bg-slate-950/80 backdrop-blur-xl rounded-[14px] flex items-center justify-center">
                <Shield className="w-5 h-5 text-cyan-300 group-hover:scale-110 transition-transform duration-300" />
              </div>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-cyan-400 rounded-full border-2 border-slate-950 flex items-center justify-center shadow-[0_0_8px_#22d3ee]">
              <span className="w-1 h-1 bg-white rounded-full animate-ping" />
            </div>
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <span className="text-base sm:text-lg font-bold font-display tracking-tight text-white group-hover:text-cyan-300 transition-colors">
                CODEXA
              </span>
              <span className="px-2 py-0.5 text-[9px] font-mono font-bold tracking-wider uppercase bg-white/10 text-cyan-300 border border-white/15 rounded-full backdrop-blur-md shadow-sm">
                iOS 26
              </span>
            </div>
            <p className="text-[10px] text-slate-400 font-sans hidden md:block tracking-wide">
              Neural Code Review &amp; Security Shield
            </p>
          </div>
        </div>

        {/* Center & Right Navigation Actions */}
        <nav className="flex items-center space-x-2 sm:space-x-3">
          {/* iOS Segmented View Switcher Capsule */}
          <div className="flex items-center p-1 bg-black/40 border border-white/10 rounded-2xl backdrop-blur-2xl shadow-inner">
            <button
              onClick={() => setCurrentView('landing')}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold font-display transition-all duration-200 ${
                currentView === 'landing'
                  ? 'bg-white/15 text-white shadow-[0_2px_10px_rgba(0,0,0,0.3)] border border-white/20'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold font-display transition-all duration-200 flex items-center space-x-1.5 ${
                currentView === 'upload'
                  ? 'bg-cyan-500/25 text-cyan-300 border border-cyan-400/40 shadow-[0_0_15px_rgba(6,182,212,0.3)]'
                  : 'text-slate-400 hover:text-slate-200'
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
            className="hidden sm:flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-medium text-slate-300 hover:text-white transition-all backdrop-blur-xl"
          >
            <BookOpen className="w-3.5 h-3.5 text-cyan-400" />
            <span>API</span>
          </a>

          {/* GitHub Source Link */}
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-medium text-slate-300 hover:text-white transition-all backdrop-blur-xl"
          >
            <Github className="w-3.5 h-3.5 text-slate-300" />
            <span>GitHub</span>
          </a>

          {/* Primary Action Button (New Audit) */}
          <button
            onClick={() => setCurrentView('upload')}
            className="relative group overflow-hidden px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-500/90 via-blue-600/90 to-violet-600/90 text-white font-display font-bold text-xs shadow-[0_0_20px_rgba(6,182,212,0.35)] hover:shadow-[0_0_30px_rgba(6,182,212,0.6)] border border-white/25 transition-all transform hover:scale-105 active:scale-95 cursor-pointer flex items-center space-x-1.5 backdrop-blur-xl"
          >
            <Plus className="w-3.5 h-3.5 text-white stroke-[3]" />
            <span className="tracking-tight">New Audit</span>
          </button>

          {/* Realtime Backend Status Beacon */}
          <div className="pl-1 sm:pl-2 flex items-center space-x-2 text-xs border-l border-white/10">
            <span className="relative flex h-2.5 w-2.5">
              {isConnected && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75" />
              )}
              <span
                className={`relative inline-flex rounded-full h-2.5 w-2.5 ${
                  isConnected
                    ? 'bg-cyan-400 shadow-[0_0_12px_#22d3ee]'
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
