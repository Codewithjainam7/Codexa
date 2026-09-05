"use client";
import React from 'react';
import { motion } from 'framer-motion';
import { Home, PlusCircle, BarChart3, Settings } from 'lucide-react';

export default function BottomNav({ currentView, setCurrentView, activeJobId }) {
  const tabs = [
    { 
      id: 'landing', 
      label: 'Home', 
      icon: Home 
    },
    { 
      id: 'upload', 
      label: 'Audit', 
      icon: PlusCircle,
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
    <nav className="fixed bottom-3 inset-x-3 z-50 md:hidden pointer-events-auto select-none">
      <div className="max-w-md mx-auto relative p-1.5 rounded-2xl bg-white/92 dark:bg-[#070A14]/94 backdrop-blur-2xl border border-slate-200/80 dark:border-white/10 shadow-[0_12px_36px_rgba(0,0,0,0.16),0_0_24px_rgba(37,99,235,0.08)] dark:shadow-[0_16px_40px_rgba(0,0,0,0.65),0_0_25px_rgba(37,99,235,0.14)]">
        {/* Ambient Top Glow Line */}
        <div className="absolute top-0 inset-x-6 h-[1px] bg-gradient-to-r from-transparent via-blue-500/40 to-transparent pointer-events-none" />

        <div className="grid grid-cols-4 gap-1 relative z-10">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = currentView === tab.id;

            return (
              <motion.button
                key={tab.id}
                type="button"
                onClick={() => setCurrentView(tab.id)}
                whileTap={{ scale: 0.88 }}
                className={`relative flex flex-col items-center justify-center py-2 px-1 rounded-xl transition-colors duration-150 cursor-pointer ${
                  isActive
                    ? 'text-blue-600 dark:text-white'
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'
                }`}
              >
                {/* Framer Motion Liquid Spring Sliding Pill Background */}
                {isActive && (
                  <motion.div
                    layoutId="activeBottomNavPill"
                    className="absolute inset-0 rounded-xl bg-blue-500/10 dark:bg-blue-600/20 border border-blue-500/25 dark:border-blue-400/30 shadow-[0_0_14px_rgba(59,130,246,0.25)]"
                    transition={{ type: "spring", stiffness: 450, damping: 35 }}
                  />
                )}

                {/* Icon Container with Badge */}
                <div className="relative z-10 flex flex-col items-center">
                  <div className="relative">
                    <Icon 
                      className={`w-5 h-5 transition-all duration-200 ${
                        isActive 
                          ? 'scale-110 stroke-[2.4] text-blue-600 dark:text-blue-400 drop-shadow-[0_0_8px_rgba(59,130,246,0.4)]' 
                          : tab.isPrimary 
                            ? 'text-blue-500 dark:text-blue-400 stroke-[2.2]'
                            : 'scale-100 stroke-[1.8]'
                      }`} 
                    />

                    {/* Results Active Beacon Indicator */}
                    {tab.hasBadge && (
                      <span className="absolute -top-1 -right-1.5 flex h-2.5 w-2.5">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
                        <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-blue-500 shadow-[0_0_8px_#3b82f6]" />
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

                  {/* Micro Indicator Dot for Active Tab */}
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
