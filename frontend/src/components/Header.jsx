"use client";
import React from 'react';
import { Shield, BookOpen, Github, Plus, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function Header({ currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-3 z-50 w-full px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto transition-all duration-300">
      <div className="cdx-elevated rounded-2xl h-16 px-4 sm:px-6 flex items-center justify-between gap-4 transition-all duration-200">
        {/* Brand Logo & Squircle Icon */}
        <div 
          className="flex items-center space-x-3 cursor-pointer group select-none" 
          onClick={() => setCurrentView('landing')}
        >
          <div className="relative">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 via-amber-500 to-amber-700 p-[1.5px] shadow-md transition-all duration-300 group-hover:scale-105">
              <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center">
                <Shield className="w-5 h-5 text-amber-500 dark:text-amber-400 transition-transform duration-300" />
              </div>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-amber-500 rounded-full border-2 border-[var(--bg-card)] flex items-center justify-center">
              <span className="w-1 h-1 bg-white rounded-full animate-ping" />
            </div>
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <span className="text-base sm:text-lg font-extrabold font-display tracking-tight text-[var(--text-primary)] group-hover:text-amber-500 transition-colors">
                CODEXA
              </span>
              <span className="px-2 py-0.5 text-[9px] font-mono font-bold tracking-wider uppercase bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20 rounded-md">
                v2.4
              </span>
            </div>
            <p className="text-[10px] text-[var(--text-muted)] font-sans hidden md:block tracking-wide">
              Deterministic AST &amp; Neural Security Auditor
            </p>
          </div>
        </div>

        {/* Center & Right Navigation Actions */}
        <nav className="flex items-center space-x-2 sm:space-x-3">
          {/* Segmented View Switcher Capsule */}
          <div className="flex items-center p-1 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl">
            <button
              onClick={() => setCurrentView('landing')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 ${
                currentView === 'landing'
                  ? 'bg-[var(--bg-card)] text-[var(--text-primary)] shadow-sm border border-[var(--border-subtle)]'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 flex items-center space-x-1.5 ${
                currentView === 'upload'
                  ? 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30 shadow-sm'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
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
            className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-xs font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all"
          >
            <BookOpen className="w-3.5 h-3.5 text-amber-500" />
            <span>API</span>
          </a>

          {/* GitHub Source Link */}
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-xs font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all"
          >
            <Github className="w-3.5 h-3.5 text-[var(--text-secondary)]" />
            <span>GitHub</span>
          </a>

          {/* Light / Dark Mode Toggle Switch */}
          <button
            onClick={toggleTheme}
            aria-label="Toggle light and dark theme"
            className="p-2 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-[var(--text-primary)] hover:text-amber-500 transition-all cursor-pointer"
          >
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-amber-400" />
            ) : (
              <Moon className="w-4 h-4 text-slate-700" />
            )}
          </button>

          {/* Primary Action Button (New Audit) */}
          <button
            onClick={() => setCurrentView('upload')}
            className="cdx-btn-primary px-3.5 sm:px-4 py-2 rounded-xl font-display font-bold text-xs flex items-center space-x-1.5 cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5 stroke-[3]" />
            <span className="tracking-tight">New Audit</span>
          </button>

          {/* Realtime Backend Status Beacon */}
          <div className="pl-1 sm:pl-2 flex items-center space-x-2 text-xs border-l border-[var(--border-subtle)]">
            <span className="relative flex h-2.5 w-2.5">
              {isConnected && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
              )}
              <span
                className={`relative inline-flex rounded-full h-2.5 w-2.5 ${
                  isConnected
                    ? 'bg-emerald-500 shadow-[0_0_8px_#10b981]'
                    : 'bg-rose-500 shadow-[0_0_8px_#ef4444]'
                }`}
              />
            </span>
            <span className="text-[10px] font-mono text-[var(--text-muted)] hidden xl:inline">
              {isConnected ? 'API ONLINE' : 'DISCONNECTED'}
            </span>
          </div>
        </nav>
      </div>
    </header>
  );
}
