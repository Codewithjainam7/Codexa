"use client";
import React from 'react';
import { Home, PlusCircle, BarChart3, Settings } from 'lucide-react';

export default function BottomNav({ currentView, setCurrentView, activeJobId }) {
  const tabs = [
    { id: 'landing', label: 'Home', icon: Home },
    { id: 'upload', label: 'Audit', icon: PlusCircle },
    { id: 'analysis', label: 'Results', icon: BarChart3, badge: activeJobId ? '●' : null },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <nav className="fixed bottom-0 inset-x-0 z-50 md:hidden pointer-events-auto select-none">
      {/* Top subtle glow border */}
      <div className="w-full h-[1px] bg-gradient-to-r from-transparent via-blue-500/30 to-transparent" />

      {/* Bar container with ultra-slick glassmorphism */}
      <div className="bg-[var(--bg-card)]/95 backdrop-blur-2xl border-t border-[var(--border-subtle)] px-2 py-1.5 pb-safe flex items-center justify-around shadow-[0_-8px_30px_rgb(0,0,0,0.15)]">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = currentView === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => setCurrentView(tab.id)}
              className={`flex-1 py-1.5 px-1 flex flex-col items-center justify-center space-y-1 relative rounded-xl transition-all duration-200 cursor-pointer ${
                isActive
                  ? 'text-blue-600 dark:text-blue-400'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              {/* Active Tab Ambient Pill Glow */}
              {isActive && (
                <div className="absolute inset-x-3 top-0.5 h-7 bg-blue-500/15 dark:bg-blue-500/20 rounded-xl -z-10 animate-fade-in" />
              )}

              <div className="relative">
                <Icon className={`w-5 h-5 transition-transform duration-200 ${isActive ? 'scale-110 stroke-[2.5]' : 'scale-100 stroke-[2]'}`} />
                {tab.badge && !isActive && (
                  <span className="absolute -top-1 -right-1.5 text-blue-500 text-[8px] animate-pulse">
                    {tab.badge}
                  </span>
                )}
              </div>

              <span className={`text-[10px] font-display font-semibold tracking-tight ${isActive ? 'font-bold' : 'font-medium'}`}>
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
