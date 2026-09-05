"use client";
import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Shield, 
  Home, 
  PlusCircle, 
  BarChart3, 
  BookOpen, 
  Github, 
  Sun, 
  Moon, 
  X, 
  ExternalLink,
  Activity,
  Cpu,
  ChevronRight
} from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function MobileSidebar({ isOpen, onClose, currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();

  // Prevent background scroll when sidebar drawer is active
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  const handleNav = (viewName) => {
    setCurrentView(viewName);
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 md:hidden flex justify-end">
          {/* Translucent Dark Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.25 }}
            onClick={onClose}
            className="fixed inset-0 bg-slate-950/70 backdrop-blur-md cursor-pointer"
          />

          {/* Slide-over Sidebar Content Container */}
          <motion.aside
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 260 }}
            className="relative w-4/5 max-w-sm h-full bg-[var(--bg-card)] backdrop-blur-3xl border-l border-[var(--border-subtle)] shadow-2xl flex flex-col z-10 overflow-y-auto"
          >
            {/* Top Bar Header */}
            <div className="p-4 sm:p-5 flex items-center justify-between border-b border-[var(--border-subtle)]">
              <div 
                className="flex items-center space-x-2.5 cursor-pointer group"
                onClick={() => handleNav('landing')}
              >
                <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-md shadow-blue-500/20">
                  <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center overflow-hidden p-0.5">
                    <img src="/logo.png" alt="CODEXA Logo" className="w-full h-full object-contain" />
                  </div>
                </div>
                <div className="flex flex-col">
                  <div className="flex items-center space-x-1.5">
                    <span className="text-sm font-extrabold font-display tracking-tight text-[var(--text-primary)]">
                      CODEXA
                    </span>
                    <span className="px-1.5 py-0.5 text-[9px] font-mono font-bold tracking-wider uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/25 rounded-md">
                      v2.4
                    </span>
                  </div>
                  <span className="text-[10px] text-[var(--text-muted)] font-mono">Mobile Security Console</span>
                </div>
              </div>

              <button
                onClick={onClose}
                className="w-9 h-9 flex items-center justify-center rounded-xl bg-[var(--bg-recessed)] hover:bg-blue-500/10 text-[var(--text-secondary)] hover:text-blue-500 border border-[var(--border-subtle)] transition-all cursor-pointer"
                aria-label="Close sidebar"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Navigation Menu Links */}
            <div className="p-4 flex-1 space-y-6">
              <div className="space-y-1.5">
                <span className="px-2 text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
                  Navigation
                </span>

                {/* Overview Dashboard Link */}
                <button
                  onClick={() => handleNav('landing')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl font-display text-xs font-semibold transition-all cursor-pointer ${
                    currentView === 'landing'
                      ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/30 shadow-sm'
                      : 'text-[var(--text-secondary)] hover:bg-[var(--bg-recessed)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <Home className={`w-4 h-4 ${currentView === 'landing' ? 'text-blue-500' : 'text-[var(--text-muted)]'}`} />
                    <span>Overview Dashboard</span>
                  </div>
                  {currentView === 'landing' && (
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-500 shadow-[0_0_8px_#3b82f6]" />
                  )}
                </button>

                {/* Codebase Audit Link */}
                <button
                  onClick={() => handleNav('upload')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl font-display text-xs font-semibold transition-all cursor-pointer ${
                    currentView === 'upload'
                      ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/30 shadow-sm'
                      : 'text-[var(--text-secondary)] hover:bg-[var(--bg-recessed)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <PlusCircle className={`w-4 h-4 ${currentView === 'upload' ? 'text-blue-500' : 'text-[var(--text-muted)]'}`} />
                    <span>Codebase Audit</span>
                  </div>
                  {currentView === 'upload' && (
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-500 shadow-[0_0_8px_#3b82f6]" />
                  )}
                </button>

                {/* Active Audit Report Link */}
                <button
                  onClick={() => handleNav('analysis')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl font-display text-xs font-semibold transition-all cursor-pointer ${
                    currentView === 'analysis'
                      ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/30 shadow-sm'
                      : 'text-[var(--text-secondary)] hover:bg-[var(--bg-recessed)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <BarChart3 className={`w-4 h-4 ${currentView === 'analysis' ? 'text-blue-500' : 'text-[var(--text-muted)]'}`} />
                    <span>Audit Report & Diffs</span>
                  </div>
                  {currentView === 'analysis' && (
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-500 shadow-[0_0_8px_#3b82f6]" />
                  )}
                </button>
              </div>

              {/* Developer Links */}
              <div className="space-y-1.5">
                <span className="px-2 text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
                  Developer Resources
                </span>

                <a
                  href="http://localhost:8080/swagger-ui.html"
                  target="_blank"
                  rel="noreferrer"
                  onClick={onClose}
                  className="flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-recessed)] transition-all"
                >
                  <div className="flex items-center space-x-3">
                    <BookOpen className="w-4 h-4 text-blue-500" />
                    <span>Swagger API Specs</span>
                  </div>
                  <ExternalLink className="w-3.5 h-3.5 text-[var(--text-muted)]" />
                </a>

                <a
                  href="https://github.com/Codewithjainam7/Codexa"
                  target="_blank"
                  rel="noreferrer"
                  onClick={onClose}
                  className="flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-recessed)] transition-all"
                >
                  <div className="flex items-center space-x-3">
                    <Github className="w-4 h-4 text-[var(--text-secondary)]" />
                    <span>GitHub Repository</span>
                  </div>
                  <ExternalLink className="w-3.5 h-3.5 text-[var(--text-muted)]" />
                </a>
              </div>

              {/* System Engine Status Card */}
              <div className="p-3.5 rounded-2xl bg-[var(--bg-recessed)]/70 border border-[var(--border-subtle)] space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center space-x-2">
                    <Activity className="w-3.5 h-3.5 text-blue-500" />
                    <span className="font-mono text-[10px] font-bold text-[var(--text-muted)]">BACKEND ENGINE</span>
                  </div>
                  <span className="flex items-center space-x-1.5 text-[10px] font-mono font-bold">
                    <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981] animate-pulse' : 'bg-rose-500'}`} />
                    <span className={isConnected ? 'text-emerald-500 dark:text-emerald-400' : 'text-rose-500'}>
                      {isConnected ? 'API ONLINE' : 'OFFLINE'}
                    </span>
                  </span>
                </div>

                <div className="flex items-center justify-between text-[11px] pt-2 border-t border-[var(--border-subtle)] text-[var(--text-secondary)]">
                  <div className="flex items-center space-x-1.5">
                    <Cpu className="w-3.5 h-3.5 text-indigo-500" />
                    <span>DeepSeek / OpenAI AI</span>
                  </div>
                  <span className="px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-600 dark:text-blue-400 text-[9px] font-mono font-bold">
                    READY
                  </span>
                </div>
              </div>

              {/* Theme Selector Segmented Control */}
              <div className="space-y-1.5">
                <span className="px-2 text-[10px] font-mono font-bold uppercase tracking-wider text-[var(--text-muted)]">
                  Interface Theme
                </span>
                <div className="p-1 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl flex items-center">
                  <button
                    onClick={() => { if (theme !== 'light') toggleTheme(); }}
                    className={`flex-1 py-2 rounded-lg text-xs font-semibold flex items-center justify-center space-x-2 transition-all cursor-pointer ${
                      theme === 'light'
                        ? 'bg-[var(--bg-card)] text-blue-600 shadow-sm border border-[var(--border-subtle)]'
                        : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    <Sun className="w-3.5 h-3.5 text-amber-500" />
                    <span>Light</span>
                  </button>
                  <button
                    onClick={() => { if (theme !== 'dark') toggleTheme(); }}
                    className={`flex-1 py-2 rounded-lg text-xs font-semibold flex items-center justify-center space-x-2 transition-all cursor-pointer ${
                      theme === 'dark'
                        ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30 shadow-sm'
                        : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    <Moon className="w-3.5 h-3.5 text-blue-400" />
                    <span>Dark</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Bottom Quick Action CTA Button */}
            <div className="p-4 border-t border-[var(--border-subtle)] bg-[var(--bg-card)]/80">
              <button
                onClick={() => handleNav('upload')}
                className="cdx-btn-primary w-full py-3 px-4 rounded-xl font-display font-bold text-xs flex items-center justify-center space-x-2 cursor-pointer shadow-lg shadow-blue-500/25"
              >
                <span>Launch Code Audit</span>
                <ChevronRight className="w-4 h-4 stroke-[2.5]" />
              </button>
            </div>
          </motion.aside>
        </div>
      )}
    </AnimatePresence>
  );
}
