"use client";
import React from 'react';
import { Sun, Moon, Plus } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function MobileTopBar({ currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();

  const getScreenTitle = () => {
    switch (currentView) {
      case 'upload':
        return 'New Audit';
      case 'analysis':
        return 'Audit Results';
      case 'settings':
        return 'Settings';
      default:
        return 'Overview';
    }
  };

  return (
    <header className="fixed top-0 inset-x-0 z-40 md:hidden bg-[var(--bg-card)]/95 backdrop-blur-xl border-b border-[var(--border-subtle)] px-4 h-14 flex items-center justify-between shadow-sm select-none">
      {/* Left: Brand Logo & Title */}
      <div
        className="flex items-center space-x-2.5 cursor-pointer active:scale-95 transition-transform"
        onClick={() => setCurrentView('landing')}
      >
        <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[1.5px] shadow-sm shadow-blue-500/20 shrink-0">
          <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center p-0.5 overflow-hidden">
            <img src="/logo.png" alt="CODEXA" className="w-full h-full object-contain" />
          </div>
        </div>

        <div className="flex items-center space-x-1.5">
          <span className="text-sm font-black font-display tracking-tight text-[var(--text-primary)]">
            CODEXA
          </span>
          <span className="text-[10px] font-mono text-slate-400 font-semibold">
            / {getScreenTitle()}
          </span>
        </div>
      </div>

      {/* Right Actions: Theme Toggle + Connection Beacon */}
      <div className="flex items-center space-x-2">
        {/* Quick New Audit Button if not on upload view */}
        {currentView !== 'upload' && (
          <button
            onClick={() => setCurrentView('upload')}
            className="cdx-btn-primary h-7 px-2.5 rounded-lg text-[11px] font-bold font-display flex items-center space-x-1 shadow-sm shadow-blue-500/20 active:scale-95 transition-transform"
          >
            <Plus className="w-3 h-3 stroke-[3]" />
            <span>Audit</span>
          </button>
        )}

        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          aria-label="Toggle theme"
          className="w-7 h-7 flex items-center justify-center rounded-lg bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-[var(--text-secondary)] active:scale-90 transition-transform"
        >
          {theme === 'dark' ? (
            <Sun className="w-3.5 h-3.5 text-blue-400" />
          ) : (
            <Moon className="w-3.5 h-3.5 text-slate-700" />
          )}
        </button>

        {/* Connection status beacon */}
        <div className="flex items-center pl-1">
          <span className="relative flex h-2 w-2">
            {isConnected && (
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            )}
            <span
              className={`relative inline-flex rounded-full h-2 w-2 ${
                isConnected
                  ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]'
                  : 'bg-rose-500 shadow-[0_0_6px_#ef4444]'
              }`}
            />
          </span>
        </div>
      </div>
    </header>
  );
}
