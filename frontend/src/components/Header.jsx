import React from 'react';
import { ShieldCheck, Terminal, BookOpen, Layers } from 'lucide-react';

export default function Header({ currentView, setCurrentView, isConnected }) {
  return (
    <header className="border-b border-neutral-800/80 bg-black/75 backdrop-blur-xl sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setCurrentView('landing')}>
          <div className="p-2 bg-neutral-900 border border-neutral-700/80 rounded-xl shadow-lg shadow-black">
            <ShieldCheck className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-bold font-display tracking-tight text-white">CODEXA</span>
              <span className="px-2 py-0.5 text-[10px] font-mono font-bold uppercase tracking-wider bg-white/10 text-neutral-200 border border-white/20 rounded-full">MVP</span>
            </div>
            <p className="text-xs text-neutral-400 hidden sm:block">Production Readiness &amp; Security Auditor</p>
          </div>
        </div>

        <nav className="flex items-center space-x-1 sm:space-x-3">
          <button
            onClick={() => setCurrentView('landing')}
            className={`px-3.5 py-1.5 rounded-xl text-sm font-medium transition-all ${
              currentView === 'landing' ? 'bg-neutral-800 text-white border border-neutral-700' : 'text-neutral-400 hover:text-white'
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setCurrentView('upload')}
            className={`px-4 py-1.5 rounded-xl text-sm font-bold transition-all ${
              currentView === 'upload' ? 'bg-white text-black shadow-lg shadow-white/10' : 'bg-white text-black hover:bg-neutral-200 shadow-md'
            }`}
          >
            New Analysis
          </button>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="px-3 py-1.5 rounded-xl text-sm font-medium text-neutral-400 hover:text-white flex items-center space-x-1 transition-colors"
          >
            <BookOpen className="w-4 h-4" />
            <span className="hidden sm:inline">API Docs</span>
          </a>
          <div className="pl-2 flex items-center space-x-2 text-xs">
            <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-white shadow-[0_0_8px_rgba(255,255,255,0.9)]' : 'bg-amber-400 animate-pulse'}`} />
            <span className="text-neutral-400 hidden md:inline">{isConnected ? 'Backend Active' : 'Connecting...'}</span>
          </div>
        </nav>
      </div>
    </header>
  );
}
