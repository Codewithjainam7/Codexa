import React from 'react';

export default function Kbd({ children, className = '' }) {
  return (
    <kbd className={`inline-flex items-center justify-center px-1.5 py-0.5 text-[10px] font-mono font-semibold text-slate-400 bg-slate-800/80 border border-slate-700/60 rounded shadow-xs select-none ${className}`}>
      {children}
    </kbd>
  );
}
