"use client";
import React, { useState } from 'react';
import { Shield, BookOpen, Github, Plus, Sun, Moon, Menu } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import MobileSidebar from './MobileSidebar';

export default function Header({ currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  return (
    <>
      <header className="fixed top-0 inset-x-0 z-40 pointer-events-none flex justify-center px-2 sm:px-6 pt-2 sm:pt-4">
        <div className="pointer-events-auto max-w-7xl w-full rounded-2xl sm:rounded-3xl bg-[var(--bg-card)]/90 backdrop-blur-2xl border border-[var(--border-subtle)] shadow-[0_8px_30px_rgb(0,0,0,0.15)] flex flex-col transition-all">
          <div className="h-14 sm:h-16 px-3 sm:px-6 flex items-center justify-between gap-2 sm:gap-6">
            {/* Left: Brand Logo & Squircle Icon */}
            <div 
              className="flex items-center space-x-2 sm:space-x-3 cursor-pointer group select-none shrink-0" 
              onClick={() => { setCurrentView('landing'); setMobileSidebarOpen(false); }}
            >
              <div className="relative shrink-0">
                <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-md shadow-blue-500/20 transition-transform duration-300 group-hover:scale-105">
                  <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center">
                    <Shield className="w-3.5 h-3.5 sm:w-5 sm:h-5 text-blue-500 dark:text-blue-400 transition-transform duration-300" />
                  </div>
                </div>
                <div className="absolute -bottom-0.5 -right-0.5 w-2 h-2 sm:w-2.5 sm:h-2.5 bg-blue-500 rounded-full border-2 border-[var(--bg-card)] flex items-center justify-center">
                  <span className="w-1 h-1 bg-white rounded-full animate-ping" />
                </div>
              </div>

              <div className="shrink-0 flex items-center space-x-1.5 sm:space-x-2">
                <span className="text-xs sm:text-base font-extrabold font-display tracking-tight text-[var(--text-primary)] group-hover:text-blue-500 transition-colors whitespace-nowrap">
                  CODEXA
                </span>
                <span className="px-1 sm:px-1.5 py-0.5 text-[8px] sm:text-[9px] font-mono font-bold tracking-wider uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/25 rounded-md whitespace-nowrap">
                  v2.4
                </span>
              </div>
            </div>

            {/* Right Navigation & Actions */}
            <nav className="flex items-center space-x-1.5 sm:space-x-3 shrink-0">
              {/* Segmented View Switcher Capsule (Desktop / Tablet) */}
              <div className="hidden md:flex items-center p-0.5 sm:p-1 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl shrink-0">
                <button
                  onClick={() => { setCurrentView('landing'); setMobileSidebarOpen(false); }}
                  className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 whitespace-nowrap shrink-0 cursor-pointer ${
                    currentView === 'landing'
                      ? 'bg-[var(--bg-card)] text-[var(--text-primary)] shadow-sm border border-[var(--border-subtle)]'
                      : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  Overview
                </button>
                <button
                  onClick={() => { setCurrentView('upload'); setMobileSidebarOpen(false); }}
                  className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 flex items-center space-x-1.5 whitespace-nowrap shrink-0 cursor-pointer ${
                    currentView === 'upload'
                      ? 'bg-blue-500/20 text-blue-600 dark:text-blue-400 border border-blue-500/30 shadow-sm'
                      : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  <span>Audit</span>
                </button>
              </div>

              {/* Quick API Docs Link (Desktop) */}
              <a
                href="http://localhost:8080/swagger-ui.html"
                target="_blank"
                rel="noreferrer"
                className="hidden md:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-xs font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all whitespace-nowrap shrink-0"
              >
                <BookOpen className="w-3.5 h-3.5 text-blue-500 dark:text-blue-400 shrink-0" />
                <span>API</span>
              </a>

              {/* GitHub Source Link (Desktop) */}
              <a
                href="https://github.com/Codewithjainam7/Codexa"
                target="_blank"
                rel="noreferrer"
                className="hidden lg:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-xs font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all whitespace-nowrap shrink-0"
              >
                <Github className="w-3.5 h-3.5 text-[var(--text-secondary)] shrink-0" />
                <span>GitHub</span>
              </a>

              {/* Light / Dark Mode Toggle Switch */}
              <button
                onClick={toggleTheme}
                aria-label="Toggle light and dark theme"
                className="w-7 h-7 sm:w-9 sm:h-9 flex items-center justify-center rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-[var(--text-primary)] hover:text-blue-500 transition-all cursor-pointer shrink-0"
              >
                {theme === 'dark' ? (
                  <Sun className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-blue-400" />
                ) : (
                  <Moon className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-slate-700" />
                )}
              </button>

              {/* Primary Action Button (New Audit) */}
              <button
                onClick={() => { setCurrentView('upload'); setMobileSidebarOpen(false); }}
                className="cdx-btn-primary h-7 sm:h-9 px-2.5 sm:px-4 rounded-xl font-display font-bold text-[11px] sm:text-xs flex items-center space-x-1 cursor-pointer shrink-0 whitespace-nowrap shadow-md shadow-blue-500/25"
              >
                <Plus className="w-3 h-3 sm:w-3.5 sm:h-3.5 stroke-[3] shrink-0" />
                <span className="tracking-tight whitespace-nowrap">New Audit</span>
              </button>

              {/* Realtime Backend Status Beacon (Desktop) */}
              <div className="hidden sm:flex pl-2 items-center space-x-2 text-xs border-l border-[var(--border-subtle)] shrink-0">
                <span className="relative flex h-2.5 w-2.5 shrink-0">
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
                <span className="text-[10px] font-mono text-[var(--text-muted)] whitespace-nowrap shrink-0">
                  {isConnected ? 'API ONLINE' : 'OFFLINE'}
                </span>
              </div>

              {/* Mobile Menu Sidebar Trigger Button */}
              <button
                onClick={() => setMobileSidebarOpen(true)}
                className="md:hidden w-7 h-7 flex items-center justify-center rounded-xl bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:border-blue-500/40 transition-all cursor-pointer shrink-0"
                aria-label="Open mobile sidebar menu"
              >
                <Menu className="w-4 h-4" />
              </button>
            </nav>
          </div>
        </div>
      </header>

      {/* Slide-over Mobile Sidebar Drawer */}
      <MobileSidebar
        isOpen={mobileSidebarOpen}
        onClose={() => setMobileSidebarOpen(false)}
        currentView={currentView}
        setCurrentView={setCurrentView}
        isConnected={isConnected}
      />
    </>
  );
}
