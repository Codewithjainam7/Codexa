"use client";
import React, { useState, useEffect } from 'react';
import { 
  Shield, Sun, Moon, Activity, Cpu, 
  BookOpen, Github, ExternalLink, Info, CheckCircle2,
  Check, User
} from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import { TECH_AVATARS, getActiveAvatar, setActiveAvatar } from '../lib/avatars';

export default function MobileSettingsView({ isConnected, limits }) {
  const { theme, toggleTheme } = useTheme();
  const [selectedAvatarId, setSelectedAvatarId] = useState(getActiveAvatar().id);

  const handleSelectAvatar = (id) => {
    setSelectedAvatarId(id);
    setActiveAvatar(id);
  };

  const activeAvatar = TECH_AVATARS.find(a => a.id === selectedAvatarId) || TECH_AVATARS[0];
  const ActiveIcon = activeAvatar.icon;

  return (
    <div className="max-w-xl mx-auto space-y-4 pb-8 pt-1 text-left select-none font-sans">
      {/* 1. Interactive Tech Avatar Profile Card */}
      <div className="p-4 rounded-2xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 shadow-sm space-y-3.5">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
            Developer Identity
          </span>
          <span className="px-2 py-0.5 rounded-full text-[9px] font-mono font-bold uppercase bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/25">
            Active Profile
          </span>
        </div>

        {/* Current Active Avatar Display */}
        <div className="flex items-center space-x-3.5 p-3 rounded-xl bg-slate-50 dark:bg-slate-900/80 border border-slate-200 dark:border-slate-800">
          <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${activeAvatar.color} p-[1.5px] shadow-md`}>
            <div className="w-full h-full bg-white dark:bg-[#070A12] rounded-[14px] flex items-center justify-center">
              <ActiveIcon className={`w-6 h-6 ${activeAvatar.text}`} />
            </div>
          </div>
          <div>
            <div className="text-sm font-bold text-slate-900 dark:text-white font-display">
              {activeAvatar.name}
            </div>
            <div className="text-[11px] font-mono text-slate-500 dark:text-slate-400">
              Role: <span className={activeAvatar.text}>{activeAvatar.role}</span>
            </div>
          </div>
        </div>

        {/* Avatar Grid Selector */}
        <div className="space-y-1.5 pt-1">
          <div className="text-[10px] font-mono font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
            Select Tech Avatar:
          </div>
          <div className="grid grid-cols-4 gap-2">
            {TECH_AVATARS.map((avatar) => {
              const Icon = avatar.icon;
              const isSelected = avatar.id === selectedAvatarId;

              return (
                <button
                  key={avatar.id}
                  onClick={() => handleSelectAvatar(avatar.id)}
                  className={`p-2.5 rounded-xl border flex flex-col items-center justify-center space-y-1 transition-all cursor-pointer relative ${
                    isSelected
                      ? 'bg-blue-500/15 dark:bg-blue-500/20 border-blue-500 shadow-md shadow-blue-500/20 scale-105'
                      : 'bg-slate-50 dark:bg-slate-900/60 border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-900 text-slate-600 dark:text-slate-400'
                  }`}
                >
                  <div className={`w-8 h-8 rounded-lg ${avatar.bg} flex items-center justify-center`}>
                    <Icon className={`w-4 h-4 ${avatar.text}`} />
                  </div>
                  <span className="text-[9px] font-mono font-semibold text-slate-700 dark:text-slate-300 truncate w-full text-center">
                    {avatar.name.split(' ')[0]}
                  </span>
                  {isSelected && (
                    <span className="absolute -top-1 -right-1 w-3.5 h-3.5 rounded-full bg-blue-500 text-white flex items-center justify-center text-[8px] font-bold">
                      ✓
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* 2. Interface Theme Card */}
      <div className="p-4 rounded-2xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-3 shadow-sm">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
            Appearance
          </span>
          <span className="text-[11px] font-mono text-blue-600 dark:text-blue-400 font-bold uppercase">
            {theme} Mode
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2.5">
          <button
            onClick={() => { if (theme !== 'light') toggleTheme(); }}
            className={`p-2.5 rounded-xl border flex items-center justify-center space-x-2 transition-all cursor-pointer ${
              theme === 'light'
                ? 'bg-blue-500/15 border-blue-500 text-blue-600 font-bold shadow-sm'
                : 'bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Sun className="w-4 h-4 text-amber-500" />
            <span className="text-xs font-display">Light Mode</span>
          </button>

          <button
            onClick={() => { if (theme !== 'dark') toggleTheme(); }}
            className={`p-2.5 rounded-xl border flex items-center justify-center space-x-2 transition-all cursor-pointer ${
              theme === 'dark'
                ? 'bg-blue-500/20 border-blue-500 text-blue-400 font-bold shadow-sm'
                : 'bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Moon className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-display">Dark Mode</span>
          </button>
        </div>
      </div>

      {/* 3. Backend Health & AI Engine Card */}
      <div className="p-4 rounded-2xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-3 shadow-sm">
        <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
          Engine Connectivity
        </span>

        <div className="space-y-2">
          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Activity className="w-4 h-4 text-blue-600 dark:text-blue-400" />
              <span className="text-xs font-medium text-slate-800 dark:text-slate-200">Backend Security API</span>
            </div>
            <span className="flex items-center space-x-1.5 text-[11px] font-mono font-bold">
              <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]' : 'bg-rose-500'}`} />
              <span className={isConnected ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'}>
                {isConnected ? 'ONLINE' : 'DISCONNECTED'}
              </span>
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Cpu className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
              <span className="text-xs font-medium text-slate-800 dark:text-slate-200">Neural LLM Engine</span>
            </div>
            <span className="text-[11px] font-mono text-blue-600 dark:text-blue-400 font-semibold">
              Nvidia Nemotron 550B
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Shield className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
              <span className="text-xs font-medium text-slate-800 dark:text-slate-200">Deterministic AST</span>
            </div>
            <span className="text-[10px] font-mono text-emerald-600 dark:text-emerald-400 font-bold flex items-center space-x-1">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>ACTIVE</span>
            </span>
          </div>
        </div>
      </div>

      {/* 4. Developer Resources Card */}
      <div className="p-4 rounded-2xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 space-y-2 shadow-sm">
        <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
          Developer Resources
        </span>

        <a
          href="https://github.com/Codewithjainam7/Codexa"
          target="_blank"
          rel="noreferrer"
          className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-900/90 hover:bg-slate-100 dark:hover:bg-slate-800 text-xs font-semibold text-slate-800 dark:text-slate-200 transition-all border border-slate-200 dark:border-slate-800/80"
        >
          <div className="flex items-center space-x-2.5">
            <Github className="w-4 h-4 text-slate-700 dark:text-slate-300" />
            <span>GitHub Repository</span>
          </div>
          <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
        </a>
      </div>

      {/* 5. App Info Card */}
      <div className="p-3 rounded-xl bg-white dark:bg-[#0D121F] border border-slate-200 dark:border-slate-800 flex items-center justify-between text-xs font-mono text-slate-500 dark:text-slate-400 shadow-sm">
        <div className="flex items-center space-x-2">
          <Info className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
          <span>CODEXA Mobile</span>
        </div>
        <span>v2.4.0</span>
      </div>
    </div>
  );
}
