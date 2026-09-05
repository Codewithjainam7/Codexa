"use client";
import React from 'react';
import { Home, PlusCircle, BarChart3, Settings } from 'lucide-react';

export default function BottomNav({ currentView, setCurrentView, activeJobId }) {
  const tabs = [
    { id: 'landing', label: 'Home', icon: Home },
    { id: 'upload', label: 'Audit', icon: PlusCircle },
    { id: 'analysis', label: 'Results', icon: BarChart3, hasBadge: !!activeJobId },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <nav className="fixed bottom-0 inset-x-0 z-50 md:hidden pointer-events-auto select-none bg-[var(--bg-card)]/98 backdrop-blur-2xl border-t border-[var(--border-subtle)] shadow-[0_-6px_25px_rgba(0,0,0,0.2)]">
      <div className="max-w-md mx-auto grid grid-cols-4 h-16 px-1">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = currentView === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => setCurrentView(tab.id)}
              className={`flex flex-col items-center justify-center space-y-1 relative h-full transition-all duration-150 cursor-pointer active:scale-90 ${
                isActive
                  ? 'text-blue-600 dark:text-blue-400'
                  : 'text-slate-400 dark:text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
              }`}
            >
              {/* Active Tab Top Indicator Line */}
              {isActive && (
                <div className="absolute top-0 inset-x-4 h-0.5 bg-blue-600 dark:bg-blue-400 rounded-full shadow-[0_0_8px_#3b82f6]" />
              )}

              {/* Icon Container with Badge */}
              <div className="relative">
                <Icon className={`w-5 h-5 transition-transform duration-150 ${isActive ? 'scale-110 stroke-[2.5]' : 'scale-100 stroke-[2]'}`} />
                {tab.hasBadge && !isActive && (
                  <span className="absolute -top-1 -right-1.5 w-2 h-2 rounded-full bg-blue-500 animate-pulse shadow-[0_0_6px_#3b82f6]" />
                )}
              </div>

              {/* Label */}
              <span className={`text-[10px] font-display tracking-tight ${isActive ? 'font-bold' : 'font-medium'}`}>
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
