import React, { useState } from 'react';
import CodeDiffViewer from './CodeDiffViewer';
import { 
  ChevronDown, ShieldAlert, ExternalLink, 
  FileCode, ShieldCheck, Flame, Info, X, Maximize2, Minimize2
} from 'lucide-react';

export default function CapsuleFindingCard({ finding, defaultExpanded = false, getSeverityBadge }) {
  const [isOpen, setIsOpen] = useState(defaultExpanded);
  const [isClosing, setIsClosing] = useState(false);

  const f = finding;

  const handleToggle = () => {
    if (isOpen) {
      setIsClosing(true);
      setTimeout(() => {
        setIsOpen(false);
        setIsClosing(false);
      }, 250);
    } else {
      setIsOpen(true);
    }
  };

  const getSeverityBorder = (sev) => {
    switch (sev) {
      case 'CRITICAL':
        return 'border-rose-500/40 hover:border-rose-500/80 shadow-rose-500/10';
      case 'HIGH':
        return 'border-orange-500/40 hover:border-orange-500/80 shadow-orange-500/10';
      case 'MEDIUM':
        return 'border-amber-500/40 hover:border-amber-500/80 shadow-amber-500/10';
      default:
        return 'border-blue-500/40 hover:border-blue-500/80 shadow-blue-500/10';
    }
  };

  const getSeverityDot = (sev) => {
    switch (sev) {
      case 'CRITICAL':
        return 'bg-rose-500 animate-pulse';
      case 'HIGH':
        return 'bg-orange-500';
      case 'MEDIUM':
        return 'bg-amber-400';
      default:
        return 'bg-blue-400';
    }
  };

  return (
    <div className="relative">
      {/* 1. Closed Pill Capsule State */}
      {!isOpen && (
        <div 
          onClick={handleToggle}
          className={`group flex items-center justify-between px-5 py-3 rounded-full bg-slate-900/90 hover:bg-slate-900 border ${getSeverityBorder(f.severity)} shadow-lg cursor-pointer transition-all duration-300 transform hover:scale-[1.01] active:scale-[0.99] select-none`}
        >
          <div className="flex items-center space-x-3 min-w-0 pr-3">
            <span className={`w-2.5 h-2.5 rounded-full shrink-0 ${getSeverityDot(f.severity)}`} />
            <span className="text-[11px] font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20 shrink-0">
              {f.ruleId}
            </span>
            <h4 className="text-xs sm:text-sm font-bold text-white truncate group-hover:text-emerald-300 transition-colors">
              {f.title}
            </h4>
          </div>

          <div className="flex items-center space-x-3 shrink-0">
            <span className="hidden sm:inline-block font-mono text-[11px] text-slate-400 truncate max-w-[180px]">
              📄 {f.filePath?.split('/').pop()}:{f.startLine}
            </span>
            {getSeverityBadge(f.severity)}
            <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 group-hover:text-white group-hover:bg-slate-700 transition-all">
              <ChevronDown className="w-3.5 h-3.5 transition-transform duration-200" />
            </div>
          </div>
        </div>
      )}

      {/* 2. Expanded Capsule Window with Morph Open/Close Animations */}
      {isOpen && (
        <div 
          className={`bg-slate-900/95 border ${getSeverityBorder(f.severity)} shadow-2xl backdrop-blur-xl rounded-3xl p-6 sm:p-7 space-y-5 transition-all duration-300 overflow-hidden ${
            isClosing ? 'animate-capsule-close' : 'animate-capsule-open'
          }`}
        >
          {/* Header Pill Bar with Close Button */}
          <div className="flex items-center justify-between gap-3 pb-3 border-b border-slate-800">
            <div className="flex items-center space-x-3 min-w-0 pr-2">
              <span className={`w-3 h-3 rounded-full shrink-0 ${getSeverityDot(f.severity)}`} />
              <span className="text-xs font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20 shrink-0">
                {f.ruleId}
              </span>
              <h3 className="text-sm sm:text-base font-bold text-white truncate tracking-tight">
                {f.title}
              </h3>
            </div>

            <div className="flex items-center space-x-2 shrink-0">
              {getSeverityBadge(f.severity)}
              <button
                onClick={handleToggle}
                className="w-8 h-8 rounded-full bg-slate-800 hover:bg-rose-500/20 hover:text-rose-300 border border-slate-700 hover:border-rose-500/40 flex items-center justify-center text-slate-400 transition-all shadow-sm active:scale-95"
                title="Close Window"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* File Meta Tags & OWASP Mapping */}
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <span className="font-mono text-slate-200 bg-slate-950 px-3 py-1 rounded-full border border-slate-800 flex items-center space-x-1.5 shadow-inner">
              <FileCode className="w-3.5 h-3.5 text-slate-400" />
              <span>{f.filePath}:{f.startLine}</span>
            </span>
            {f.owaspMapping && (
              <span className="text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-3 py-1 rounded-full font-medium flex items-center space-x-1.5 shadow-sm">
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>{f.owaspMapping}</span>
              </span>
            )}
            {f.requiresManualReview && (
              <span className="px-3 py-1 text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 rounded-full">
                Review Required
              </span>
            )}
            <span className="text-xs font-mono text-slate-500 ml-auto">
              Risk Priority: <strong className="text-slate-300">{f.priorityScore}</strong>
            </span>
          </div>

          {/* Vulnerability Analysis Card */}
          <div className="bg-slate-950/70 p-4 rounded-2xl border border-slate-800/80 space-y-1">
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center space-x-1.5">
              <Info className="w-3.5 h-3.5 text-emerald-400" />
              <span>Vulnerability Analysis</span>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed pt-1">
              {f.description}
            </p>
          </div>

          {/* Potential Impact Card */}
          {f.impact && (
            <div className="p-4 bg-rose-950/20 border border-rose-500/30 rounded-2xl text-xs text-slate-300 space-y-1 shadow-sm">
              <strong className="text-rose-400 font-bold block text-xs flex items-center space-x-1.5">
                <Flame className="w-3.5 h-3.5" />
                <span>Potential Security &amp; Operational Risk:</span>
              </strong>
              <p className="leading-relaxed text-slate-300/90">{f.impact}</p>
            </div>
          )}

          {/* IDE Terminal Code Diff Comparison */}
          <CodeDiffViewer
            originalCode={f.evidenceMasked}
            suggestedFix={f.suggestedFix}
            ruleId={f.ruleId}
          />

          {/* References & Links */}
          {f.references && f.references.length > 0 && (
            <div className="pt-3 border-t border-slate-800 flex flex-wrap items-center gap-2 text-[11px]">
              <span className="text-slate-500 font-medium">Security References:</span>
              {f.references.map((ref, idx) => (
                <a
                  key={idx}
                  href={ref}
                  target="_blank"
                  rel="noreferrer"
                  className="px-2.5 py-1 bg-slate-950 rounded-full border border-slate-800 text-emerald-400 hover:text-emerald-300 hover:border-emerald-500/40 transition-all flex items-center space-x-1"
                >
                  <span>OWASP Advisory</span>
                  <ExternalLink className="w-3 h-3 inline" />
                </a>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
