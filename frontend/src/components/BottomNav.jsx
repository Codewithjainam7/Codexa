"use client";
import React from 'react';
import { motion } from 'framer-motion';
import { LayoutGrid, Plus, BarChart3, Settings } from 'lucide-react';

export default function BottomNav({ currentView, setCurrentView, activeJobId }) {
  const triggerHaptic = () => {
    try {
      if (typeof window !== 'undefined' && window.navigator?.vibrate) {
        window.navigator.vibrate(12);
      }
    } catch (_) {}
  };

  const tabs = [
    { 
      id: 'landing', 
      label: 'Home', 
      icon: LayoutGrid 
    },
    { 
      id: 'upload', 
      label: 'Audit', 
      icon: Plus,
      isPrimary: true
    },
    { 
      id: 'analysis', 
      label: 'Results', 
      icon: BarChart3, 
      hasBadge: !!activeJobId 
    },
    { 
      id: 'settings', 
      label: 'Settings', 
      icon: Settings 
    },
  ];

  return (
    <nav className="fixed bottom-[max(0.75rem,env(safe-area-inset-bottom))] inset-x-4 z-50 md:hidden pointer-events-auto select-none">
      <div className="max-w-md mx-auto relative px-2 py-1.5 rounded-2xl bg-white/95 dark:bg-[#070A14]/95 backdrop-blur-3xl border border-slate-200/80 dark:border-white/10 shadow-[0_16px_40px_rgba(0,0,0,0.18),0_0_20px_rgba(37,99,235,0.08)] dark:shadow-[0_20px_50px_rgba(0,0,0,0.7),0_0_25px_rgba(37,99,235,0.12)]">
        {/* Subtle Ambient Top Accent Line */}
        <div className="absolute top-0 inset-x-8 h-[1px] bg-gradient-to-r from-transparent via-blue-500/40 dark:via-blue-400/30 to-transparent pointer-events-none" />

        <div className="grid grid-cols-4 gap-1 relative z-10 items-center">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = currentView === tab.id;

            return (
              <motion.button
                key={tab.id}
                type="button"
                onClick={() => {
                  triggerHaptic();
                  setCurrentView(tab.id);
                }}
                whileTap={{ scale: 0.88 }}
                className={`relative flex flex-col items-center justify-center py-2 px-1 rounded-xl transition-colors duration-150 cursor-pointer ${
                  isActive
                    ? 'text-blue-600 dark:text-white'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                }`}
              >
                {/* Framer Motion Liquid Spring Sliding Pill */}
                {isActive && (
                  <motion.div
                    layoutId="activeBottomNavPill"
                    className="absolute inset-0 rounded-xl bg-blue-500/12 dark:bg-blue-600/20 border border-blue-500/30 dark:border-blue-400/30 shadow-[0_0_16px_rgba(59,130,246,0.25)]"
                    transition={{ type: "spring", stiffness: 450, damping: 35 }}
                  />
                )}

                {/* Icon Container with Badge */}
                <div className="relative z-10 flex flex-col items-center">
                  <div className="relative">
                    <Icon 
                      className={`w-5 h-5 transition-all duration-200 ${
                        isActive 
                          ? 'scale-110 stroke-[2.4] text-blue-600 dark:text-blue-400 drop-shadow-[0_0_10px_rgba(59,130,246,0.5)]' 
                          : tab.isPrimary
                            ? 'text-blue-500 dark:text-blue-400 stroke-[2.2]'
                            : 'scale-100 stroke-[1.8]'
                      }`} 
                    />

                    {/* Results Active Beacon Indicator */}
                    {tab.hasBadge && (
                      <span className="absolute -top-1 -right-1.5 flex h-2 w-2">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500 shadow-[0_0_8px_#3b82f6]" />
                      </span>
                    )}
                  </div>

                  {/* Tab Label */}
                  <span 
                    className={`text-[10px] mt-1 font-display tracking-tight leading-none transition-all duration-150 ${
                      isActive 
                        ? 'font-bold text-blue-600 dark:text-white' 
                        : 'font-medium'
                    }`}
                  >
                    {tab.label}
                  </span>

                  {/* Micro Beacon Dot for Active Tab */}
                  {isActive && (
                    <motion.span
                      layoutId="activeNavDot"
                      className="w-1 h-1 rounded-full bg-blue-600 dark:bg-blue-400 shadow-[0_0_6px_#3b82f6] mt-1"
                      transition={{ type: "spring", stiffness: 500, damping: 35 }}
                    />
                  )}
                </div>
              </motion.button>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
