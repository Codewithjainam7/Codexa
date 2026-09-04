import React from 'react';
import { ShieldCheck, Terminal, BookOpen, Layers } from 'lucide-react';

export default function Header({ currentView, setCurrentView, isConnected }) {
  return (
    <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setCurrentView('landing')}>
          <div className="p-2 bg-gradient-to-tr from-emerald-600 to-teal-500 rounded-xl shadow-lg shadow-emerald-500/20">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-black tracking-tight text-white">CODEXA</span>
              <span className="px-2 py-0.5 text-xs font-semibold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full">MVP</span>
            </div>
            <p className="text-xs text-slate-400 hidden sm:block">Production Readiness &amp; Security Auditor</p>
          </div>
        </div>

        <nav className="flex items-center space-x-1 sm:space-x-3">
          <button
            onClick={() => setCurrentView('landing')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              currentView === 'landing' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setCurrentView('upload')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              currentView === 'upload' ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30' : 'bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/30'
            }`}
          >
            New Analysis
          </button>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-400 hover:text-slate-200 flex items-center space-x-1"
          >
            <BookOpen className="w-4 h-4" />
            <span className="hidden sm:inline">API Docs</span>
          </a>
          <div className="pl-2 flex items-center space-x-2 text-xs">
            <span className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_8px_#10b981]' : 'bg-amber-500 animate-pulse'}`} />
            <span className="text-slate-400 hidden md:inline">{isConnected ? 'Backend Active' : 'Connecting...'}</span>
          </div>
        </nav>
      </div>
    </header>
  );
}
