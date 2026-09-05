"use client";
import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';

export default function SplashScreen({ onFinish }) {
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setTimeout(() => {
            onFinish();
          }, 350);
          return 100;
        }
        return prev + 5;
      });
    }, 45);

    return () => clearInterval(interval);
  }, [onFinish]);

  return (
    <motion.div
      initial={{ opacity: 1 }}
      exit={{ opacity: 0, scale: 1.04 }}
      transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
      className="fixed inset-0 z-[100] bg-[#07090E] flex flex-col items-center justify-between p-8 select-none"
    >
      {/* Ambient background glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-blue-600/20 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full" />

      {/* Centered Logo & Branding */}
      <div className="flex flex-col items-center space-y-6 relative z-10">
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
          className="relative"
        >
          {/* Logo container with pulsating cobalt glow */}
          <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-3xl bg-gradient-to-br from-blue-500 via-indigo-600 to-blue-700 p-[2px] shadow-2xl shadow-blue-500/40">
            <div className="w-full h-full bg-[#0D1321] rounded-[22px] flex items-center justify-center p-3 overflow-hidden">
              <img
                src="/logo.png"
                alt="CODEXA"
                className="w-full h-full object-contain"
              />
            </div>
          </div>
          {/* Animated ping beacon */}
          <span className="absolute -top-1 -right-1 flex h-4 w-4">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-4 w-4 bg-blue-500" />
          </span>
        </motion.div>

        <motion.div
          initial={{ y: 12, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="text-center space-y-1.5"
        >
          <div className="flex items-center justify-center space-x-2">
            <h1 className="text-2xl sm:text-3xl font-black font-display tracking-tight text-white">
              CODEXA
            </h1>
            <span className="px-2 py-0.5 text-[10px] font-mono font-bold tracking-wider uppercase bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-md">
              v2.4
            </span>
          </div>
          <p className="text-xs text-slate-400 font-mono tracking-wide">
            AI Code Security & Production Engine
          </p>
        </motion.div>
      </div>

      {/* Booting Progress Bar & Status */}
      <div className="w-full max-w-xs space-y-3 relative z-10 mb-4">
        <div className="flex items-center justify-between text-[11px] font-mono text-slate-400">
          <span className="flex items-center space-x-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse" />
            <span>Initializing Security Engine...</span>
          </span>
          <span className="text-blue-400 font-bold">{progress}%</span>
        </div>

        <div className="w-full h-1.5 bg-slate-900 rounded-full overflow-hidden border border-slate-800">
          <motion.div
            className="h-full bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full shadow-[0_0_12px_#3b82f6]"
            style={{ width: `${progress}%` }}
            transition={{ ease: 'linear' }}
          />
        </div>

        <p className="text-[10px] text-slate-500 text-center font-mono">
          AST Parser &bull; Nemotron 550B &bull; OWASP Rules
        </p>
      </div>
    </motion.div>
  );
}
