"use client";
import React, { useState, useEffect } from 'react';
import { Sun, Moon, Plus } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import { getActiveAvatar } from '../lib/avatars';

export default function MobileTopBar({ currentView, setCurrentView, isConnected }) {
  const { theme, toggleTheme } = useTheme();
  const [avatar, setAvatar] = useState(getActiveAvatar());

  useEffect(() => {
    const handleAvatarChange = () => setAvatar(getActiveAvatar());
    window.addEventListener('codexa_avatar_updated', handleAvatarChange);
    return () => window.removeEventListener('codexa_avatar_updated', handleAvatarChange);
  }, []);

  const AvatarIcon = avatar.icon;

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
    <header className="fixed top-0 inset-x-0 z-40 md:hidden bg-[var(--bg-card)]/95 backdrop-blur-xl border-b border-[var(--border-subtle)] px-3.5 h-14 flex items-center justify-between shadow-sm select-none">
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
          <span className="text-[10px] font-mono text-slate-600 dark:text-slate-400 font-bold">
            / {getScreenTitle()}
          </span>
        </div>
      </div>

      {/* Right Actions: Quick Audit + Theme Toggle + User Avatar + Connection Beacon */}
      <div className="flex items-center space-x-2">
        {/* Quick New Audit Button if not on upload view */}
        {currentView !== 'upload' && (
          <button
            onClick={() => setCurrentView('upload')}
            className="cdx-btn-primary h-7 px-2.5 rounded-lg text-[11px] font-bold font-display flex items-center space-x-1 shadow-sm shadow-blue-500/20 active:scale-95 transition-transform cursor-pointer"
          >
            <Plus className="w-3 h-3 stroke-[3]" />
            <span>Audit</span>
          </button>
        )}

        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          aria-label="Toggle theme"
          className="w-7 h-7 flex items-center justify-center rounded-lg bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-[var(--text-secondary)] active:scale-90 transition-transform cursor-pointer"
        >
          {theme === 'dark' ? (
            <Sun className="w-3.5 h-3.5 text-amber-400" />
          ) : (
            <Moon className="w-3.5 h-3.5 text-slate-800" />
          )}
        </button>

        {/* Active Tech Avatar (Direct link to Settings) */}
        <button
          onClick={() => setCurrentView('settings')}
          aria-label="User Settings"
          className={`w-7 h-7 rounded-lg p-[1px] bg-gradient-to-br ${avatar.color} shadow-sm active:scale-90 transition-transform cursor-pointer relative group`}
        >
          <div className="w-full h-full bg-[var(--bg-card)] rounded-[7px] flex items-center justify-center">
            <AvatarIcon className={`w-3.5 h-3.5 ${avatar.text}`} />
          </div>
          {/* Subtle online pulse ring */}
          <span className="absolute -bottom-0.5 -right-0.5 w-2 h-2 rounded-full bg-emerald-500 border border-[var(--bg-card)]" />
        </button>
      </div>
    </header>
  );
}
