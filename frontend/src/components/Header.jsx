"use client";
import React, { useState, useEffect, useRef } from 'react';
import { Shield, BookOpen, Github, Plus, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function Header({ currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();
  const [isScrolled, setIsScrolled] = useState(false);
  const isScrolledRef = useRef(false);

  useEffect(() => {
    const handleScroll = () => {
      const scrolled = window.scrollY > 25;
      if (scrolled !== isScrolledRef.current) {
        isScrolledRef.current = scrolled;
        setIsScrolled(scrolled);
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <header className="fixed top-0 inset-x-0 z-50 pointer-events-none flex justify-center px-3 sm:px-6 pt-3 sm:pt-4 transition-all duration-300">
      <div 
        className={`pointer-events-auto flex items-center justify-between gap-3 sm:gap-6 transition-[max-width,padding,background-color,border-color,box-shadow,transform,border-radius] duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] ${
          isScrolled
            ? "max-w-5xl w-full h-14 px-4 sm:px-6 rounded-full bg-[var(--bg-card)]/90 backdrop-blur-3xl border border-blue-500/25 shadow-[0_12px_36px_-6px_rgba(0,0,0,0.45),0_0_20px_-2px_rgba(59,130,246,0.18)]"
            : "max-w-7xl w-full h-16 px-4 sm:px-6 rounded-2xl sm:rounded-3xl bg-[var(--bg-card)]/80 backdrop-blur-2xl border border-[var(--border-subtle)] shadow-[0_8px_30px_rgb(0,0,0,0.12)]"
        }`}
      >
        {/* Left: Brand Logo & Squircle Icon */}
        <div 
          className="flex items-center space-x-3 cursor-pointer group select-none shrink-0" 
          onClick={() => setCurrentView('landing')}
        >
          <div className="relative shrink-0">
            <div className="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-md shadow-blue-500/20 transition-transform duration-300 group-hover:scale-105">
              <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center">
                <Shield className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500 dark:text-blue-400 transition-transform duration-300" />
              </div>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-blue-500 rounded-full border-2 border-[var(--bg-card)] flex items-center justify-center">
              <span className="w-1 h-1 bg-white rounded-full animate-ping" />
            </div>
          </div>

          <div className="shrink-0 flex items-center space-x-2">
            <span className="text-sm sm:text-base font-extrabold font-display tracking-tight text-[var(--text-primary)] group-hover:text-blue-500 transition-colors whitespace-nowrap">
              CODEXA
            </span>
            <span className="px-1.5 py-0.5 text-[9px] font-mono font-bold tracking-wider uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/25 rounded-md whitespace-nowrap">
              v2.4
            </span>
          </div>
        </div>

        {/* Right Navigation & Actions */}
        <nav className="flex items-center space-x-2 sm:space-x-3 shrink-0">
          {/* Segmented View Switcher Capsule */}
          <div className="flex items-center p-0.5 sm:p-1 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl shrink-0">
            <button
              onClick={() => setCurrentView('landing')}
              className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 whitespace-nowrap shrink-0 cursor-pointer ${
                currentView === 'landing'
                  ? 'bg-[var(--bg-card)] text-[var(--text-primary)] shadow-sm border border-[var(--border-subtle)]'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-3 sm:px-3.5 py-1.5 rounded-lg text-xs font-semibold font-display transition-all duration-150 flex items-center space-x-1.5 whitespace-nowrap shrink-0 cursor-pointer ${
                currentView === 'upload'
                  ? 'bg-blue-500/20 text-blue-600 dark:text-blue-400 border border-blue-500/30 shadow-sm'
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
            className="hidden md:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-xs font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all whitespace-nowrap shrink-0"
          >
            <BookOpen className="w-3.5 h-3.5 text-blue-500 dark:text-blue-400 shrink-0" />
            <span>API</span>
          </a>

          {/* GitHub Source Link */}
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
            className="w-8 h-8 sm:w-9 sm:h-9 flex items-center justify-center rounded-xl bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border border-[var(--border-subtle)] text-[var(--text-primary)] hover:text-blue-500 transition-all cursor-pointer shrink-0"
          >
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-blue-400" />
            ) : (
              <Moon className="w-4 h-4 text-slate-700" />
            )}
          </button>

          {/* Primary Action Button (New Audit) - Fixed No Wrap & Proper Proportions */}
          <button
            onClick={() => setCurrentView('upload')}
            className="cdx-btn-primary h-8 sm:h-9 px-3.5 sm:px-4 rounded-xl font-display font-bold text-xs flex items-center space-x-1.5 cursor-pointer shrink-0 whitespace-nowrap shadow-md shadow-blue-500/25"
          >
            <Plus className="w-3.5 h-3.5 stroke-[3] shrink-0" />
            <span className="tracking-tight whitespace-nowrap">New Audit</span>
          </button>

          {/* Realtime Backend Status Beacon */}
          <div className="pl-1 sm:pl-2 flex items-center space-x-2 text-xs border-l border-[var(--border-subtle)] shrink-0">
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
            <span className="text-[10px] font-mono text-[var(--text-muted)] hidden sm:inline whitespace-nowrap shrink-0">
              {isConnected ? 'API ONLINE' : 'OFFLINE'}
            </span>
          </div>
        </nav>
      </div>
    </header>
  );
}
