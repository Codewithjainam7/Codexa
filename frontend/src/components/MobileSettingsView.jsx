"use client";
import React, { useState, useEffect } from 'react';
import { 
  Shield, Sun, Moon, Activity, Cpu, 
  BookOpen, Github, ExternalLink, Info, CheckCircle2,
  Bot, Atom, Terminal, Brain, ShieldAlert, Sparkles, Binary, Rocket,
  Check, User
} from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

const TECH_AVATARS = [
  { id: 'sentinel', name: 'Cyber Sentinel', role: 'Security Bot', icon: Bot, color: 'from-blue-500 to-cyan-500', text: 'text-cyan-400', bg: 'bg-cyan-500/15' },
  { id: 'quantum', name: 'Quantum Core', role: 'Kernel Dev', icon: Atom, color: 'from-indigo-500 to-violet-500', text: 'text-violet-400', bg: 'bg-violet-500/15' },
  { id: 'hacker', name: 'Terminal Hacker', role: 'Pen Tester', icon: Terminal, color: 'from-emerald-500 to-teal-500', text: 'text-emerald-400', bg: 'bg-emerald-500/15' },
  { id: 'neural', name: 'Neural Synapse', role: 'AI Architect', icon: Brain, color: 'from-pink-500 to-rose-500', text: 'text-pink-400', bg: 'bg-pink-500/15' },
  { id: 'shield', name: 'Zero Trust', role: 'SOC Defender', icon: Shield, color: 'from-amber-500 to-orange-500', text: 'text-amber-400', bg: 'bg-amber-500/15' },
  { id: 'daemon', name: 'Glitch Daemon', role: 'Bug Hunter', icon: Sparkles, color: 'from-purple-500 to-indigo-600', text: 'text-purple-400', bg: 'bg-purple-500/15' },
  { id: 'silicon', name: 'Bio Silicon', role: 'AST Parser', icon: Binary, color: 'from-teal-500 to-emerald-600', text: 'text-teal-400', bg: 'bg-teal-500/15' },
  { id: 'warp', name: 'Hyper Warp', role: 'Cloud Lead', icon: Rocket, color: 'from-sky-500 to-blue-600', text: 'text-sky-400', bg: 'bg-sky-500/15' },
];

export default function MobileSettingsView({ isConnected, limits }) {
  const { theme, toggleTheme } = useTheme();
  const [selectedAvatarId, setSelectedAvatarId] = useState('sentinel');

  useEffect(() => {
    const saved = localStorage.getItem('codexa_user_avatar');
    if (saved) {
      setSelectedAvatarId(saved);
    }
  }, []);

  const handleSelectAvatar = (id) => {
    setSelectedAvatarId(id);
    localStorage.setItem('codexa_user_avatar', id);
  };

  const activeAvatar = TECH_AVATARS.find(a => a.id === selectedAvatarId) || TECH_AVATARS[0];
  const ActiveIcon = activeAvatar.icon;

  return (
    <div className="max-w-xl mx-auto space-y-4 pb-8 pt-1 text-left select-none">
      {/* 1. Interactive Tech Avatar Profile Card */}
      <div className="p-4 rounded-2xl bg-[#0D121F] border border-slate-800 shadow-sm space-y-3.5">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-400">
            Developer Identity
          </span>
          <span className="px-2 py-0.5 rounded-full text-[9px] font-mono font-bold uppercase bg-blue-500/15 text-blue-400 border border-blue-500/25">
            Active Profile
          </span>
        </div>

        {/* Current Active Avatar Display */}
        <div className="flex items-center space-x-3.5 p-3 rounded-xl bg-slate-900/80 border border-slate-800">
          <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${activeAvatar.color} p-[1.5px] shadow-md`}>
            <div className="w-full h-full bg-[#070A12] rounded-[14px] flex items-center justify-center">
              <ActiveIcon className={`w-6 h-6 ${activeAvatar.text}`} />
            </div>
          </div>
          <div>
            <div className="text-sm font-bold text-white font-display">
              {activeAvatar.name}
            </div>
            <div className="text-[11px] font-mono text-slate-400">
              Role: <span className={activeAvatar.text}>{activeAvatar.role}</span>
            </div>
          </div>
        </div>

        {/* Avatar Grid Selector */}
        <div className="space-y-1.5 pt-1">
          <div className="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider">
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
                      ? 'bg-blue-500/20 border-blue-500 shadow-md shadow-blue-500/20 scale-105'
                      : 'bg-slate-900/60 border-slate-800 hover:border-slate-700 hover:bg-slate-900 text-slate-400'
                  }`}
                >
                  <div className={`w-8 h-8 rounded-lg ${avatar.bg} flex items-center justify-center`}>
                    <Icon className={`w-4 h-4 ${avatar.text}`} />
                  </div>
                  <span className="text-[9px] font-mono font-semibold text-slate-300 truncate w-full text-center">
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
      <div className="p-4 rounded-2xl bg-[#0D121F] border border-slate-800 space-y-3 shadow-sm">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-400">
            Appearance
          </span>
          <span className="text-[11px] font-mono text-blue-400 font-bold uppercase">
            {theme} Mode
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2.5">
          <button
            onClick={() => { if (theme !== 'light') toggleTheme(); }}
            className={`p-2.5 rounded-xl border flex items-center justify-center space-x-2 transition-all cursor-pointer ${
              theme === 'light'
                ? 'bg-blue-500/15 border-blue-500 text-blue-600 font-bold shadow-sm'
                : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'
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
                : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'
            }`}
          >
            <Moon className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-display">Dark Mode</span>
          </button>
        </div>
      </div>

      {/* 3. Backend Health & AI Engine Card */}
      <div className="p-4 rounded-2xl bg-[#0D121F] border border-slate-800 space-y-3 shadow-sm">
        <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-400">
          Engine Connectivity
        </span>

        <div className="space-y-2">
          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/90 border border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Activity className="w-4 h-4 text-blue-400" />
              <span className="text-xs font-medium text-slate-200">Backend Security API</span>
            </div>
            <span className="flex items-center space-x-1.5 text-[11px] font-mono font-bold">
              <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_6px_#10b981]' : 'bg-rose-500'}`} />
              <span className={isConnected ? 'text-emerald-400' : 'text-rose-400'}>
                {isConnected ? 'ONLINE' : 'DISCONNECTED'}
              </span>
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/90 border border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Cpu className="w-4 h-4 text-indigo-400" />
              <span className="text-xs font-medium text-slate-200">Neural LLM Engine</span>
            </div>
            <span className="text-[11px] font-mono text-blue-400 font-semibold">
              Nvidia Nemotron 550B
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/90 border border-slate-800/80">
            <div className="flex items-center space-x-2.5">
              <Shield className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-medium text-slate-200">Deterministic AST</span>
            </div>
            <span className="text-[10px] font-mono text-emerald-400 font-bold flex items-center space-x-1">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>ACTIVE</span>
            </span>
          </div>
        </div>
      </div>

      {/* 4. Developer Resources Card */}
      <div className="p-4 rounded-2xl bg-[#0D121F] border border-slate-800 space-y-2 shadow-sm">
        <span className="text-[11px] font-mono font-bold uppercase tracking-wider text-slate-400">
          Developer Resources
        </span>

        <a
          href="http://localhost:8080/swagger-ui.html"
          target="_blank"
          rel="noreferrer"
          className="flex items-center justify-between p-3 rounded-xl bg-slate-900/90 hover:bg-slate-800 text-xs font-semibold text-slate-200 transition-all border border-slate-800/80"
        >
          <div className="flex items-center space-x-2.5">
            <BookOpen className="w-4 h-4 text-blue-400" />
            <span>OpenAPI Swagger Docs</span>
          </div>
          <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
        </a>

        <a
          href="https://github.com/Codewithjainam7/Codexa"
          target="_blank"
          rel="noreferrer"
          className="flex items-center justify-between p-3 rounded-xl bg-slate-900/90 hover:bg-slate-800 text-xs font-semibold text-slate-200 transition-all border border-slate-800/80"
        >
          <div className="flex items-center space-x-2.5">
            <Github className="w-4 h-4 text-slate-300" />
            <span>GitHub Repository</span>
          </div>
          <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
        </a>
      </div>

      {/* 5. App Info Card */}
      <div className="p-3 rounded-xl bg-[#0D121F] border border-slate-800 flex items-center justify-between text-xs font-mono text-slate-400">
        <div className="flex items-center space-x-2">
          <Info className="w-3.5 h-3.5 text-blue-400" />
          <span>CODEXA Mobile</span>
        </div>
        <span>v2.4.0</span>
      </div>
    </div>
  );
}
