"use client";

import React, { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { useOutsideClick } from "@/hooks/use-outside-click";
import CodeDiffViewer from "./CodeDiffViewer";
import {
  FileCode, ShieldCheck, Flame, Info, X, ExternalLink,
  ChevronRight, AlertTriangle, ShieldAlert, Sparkles
} from "lucide-react";

export function ExpandableFindingCards({ findings = [], getSeverityBadge }) {
  const [active, setActive] = useState(null);
  const modalRef = useRef(null);

  useEffect(() => {
    function onKeyDown(event) {
      if (event.key === "Escape") {
        setActive(null);
      }
    }

    if (active) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }

    window.addEventListener("keydown", onKeyDown);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "auto";
    };
  }, [active]);

  useOutsideClick(modalRef, () => {
    if (active) setActive(null);
  });

  const getSeverityBorder = (sev) => {
    switch (sev) {
      case "CRITICAL":
        return "border-rose-500/40 hover:border-rose-500/90 shadow-rose-500/10 hover:shadow-rose-500/20";
      case "HIGH":
        return "border-orange-500/40 hover:border-orange-500/90 shadow-orange-500/10 hover:shadow-orange-500/20";
      case "MEDIUM":
        return "border-amber-500/40 hover:border-amber-500/90 shadow-amber-500/10 hover:shadow-amber-500/20";
      default:
        return "border-blue-500/40 hover:border-blue-500/90 shadow-blue-500/10 hover:shadow-blue-500/20";
    }
  };

  const getSeverityDot = (sev) => {
    switch (sev) {
      case "CRITICAL":
        return "bg-rose-500 animate-pulse ring-4 ring-rose-500/20";
      case "HIGH":
        return "bg-orange-500 ring-2 ring-orange-500/20";
      case "MEDIUM":
        return "bg-amber-400";
      default:
        return "bg-blue-400";
    }
  };

  return (
    <>
      {/* 1. Backdrop Overlay */}
      <AnimatePresence>
        {active && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 bg-slate-950/80 backdrop-blur-md h-full w-full z-50 pointer-events-auto"
          />
        )}
      </AnimatePresence>

      {/* 2. Expanded Modal Window with Layout Morphing */}
      <AnimatePresence>
        {active && (
          <div className="fixed inset-0 grid place-items-center z-50 p-3 sm:p-6 overflow-y-auto pointer-events-none">
            <motion.div
              layoutId={"card-" + active.id}
              ref={modalRef}
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              transition={{ type: "spring", stiffness: 350, damping: 30 }}
              className="w-full max-w-4xl max-h-[90vh] flex flex-col bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-5 shadow-2xl backdrop-blur-2xl pointer-events-auto overflow-y-auto code-terminal-scroll my-auto"
            >
              {/* Header Pill Bar */}
              <div className="flex items-center justify-between gap-3 pb-4 border-b border-slate-800/80">
                <div className="flex items-center space-x-3 min-w-0 pr-2">
                  <span className={"w-3.5 h-3.5 rounded-full shrink-0 " + getSeverityDot(active.severity)} />
                  <motion.span
                    layoutId={"rule-" + active.id}
                    className="text-xs font-mono font-bold text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20 shrink-0"
                  >
                    {active.ruleId}
                  </motion.span>
                  <motion.h3
                    layoutId={"title-" + active.id}
                    className="text-base sm:text-lg font-bold text-white truncate tracking-tight"
                  >
                    {active.title}
                  </motion.h3>
                </div>

                <div className="flex items-center space-x-3 shrink-0">
                  {getSeverityBadge(active.severity)}
                  <motion.button
                    initial={{ opacity: 0, rotate: -45 }}
                    animate={{ opacity: 1, rotate: 0 }}
                    exit={{ opacity: 0, rotate: 45 }}
                    onClick={() => setActive(null)}
                    className="w-8 h-8 rounded-full bg-slate-800 hover:bg-rose-500/20 hover:text-rose-300 border border-slate-700 hover:border-rose-500/40 flex items-center justify-center text-slate-400 transition-all shadow-sm active:scale-95"
                    title="Close Window (Esc)"
                  >
                    <X className="w-4 h-4" />
                  </motion.button>
                </div>
              </div>

              {/* Meta Tags */}
              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="font-mono text-slate-200 bg-slate-950 px-3 py-1.5 rounded-full border border-slate-800 flex items-center space-x-1.5 shadow-inner">
                  <FileCode className="w-3.5 h-3.5 text-slate-400" />
                  <span>{active.filePath}:{active.startLine}</span>
                </span>
                {active.owaspMapping && (
                  <span className="text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-3 py-1.5 rounded-full font-medium flex items-center space-x-1.5 shadow-sm">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    <span>{active.owaspMapping}</span>
                  </span>
                )}
                {active.requiresManualReview && (
                  <span className="px-3 py-1.5 text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 rounded-full">
                    Review Required
                  </span>
                )}
                <span className="text-xs font-mono text-slate-500 ml-auto">
                  Priority Weight: <strong className="text-slate-200">{active.priorityScore}</strong>
                </span>
              </div>

              {/* Vulnerability Analysis */}
              <div className="bg-slate-950/80 p-4 sm:p-5 rounded-2xl border border-slate-800/80 space-y-1.5">
                <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center space-x-1.5">
                  <Info className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Vulnerability Analysis</span>
                </div>
                <p className="text-xs sm:text-sm text-slate-300 leading-relaxed pt-1">
                  {active.description}
                </p>
              </div>

              {/* Impact Card */}
              {active.impact && (
                <div className="p-4 sm:p-5 bg-rose-950/20 border border-rose-500/30 rounded-2xl text-xs sm:text-sm text-slate-300 space-y-1.5 shadow-sm">
                  <strong className="text-rose-400 font-bold block flex items-center space-x-1.5">
                    <Flame className="w-4 h-4" />
                    <span>Potential Security & Architectural Impact:</span>
                  </strong>
                  <p className="leading-relaxed text-slate-300/90">{active.impact}</p>
                </div>
              )}

              {/* IDE Code Diff Comparison */}
              <CodeDiffViewer
                originalCode={active.evidenceMasked}
                suggestedFix={active.suggestedFix}
                ruleId={active.ruleId}
              />

              {/* Security References */}
              {active.references && active.references.length > 0 && (
                <div className="pt-3 border-t border-slate-800 flex flex-wrap items-center gap-2 text-[11px]">
                  <span className="text-slate-500 font-medium">Security Advisories:</span>
                  {active.references.map((ref, idx) => (
                    <a
                      key={idx}
                      href={ref}
                      target="_blank"
                      rel="noreferrer"
                      className="px-3 py-1 bg-slate-950 rounded-full border border-slate-800 text-emerald-400 hover:text-emerald-300 hover:border-emerald-500/40 transition-all flex items-center space-x-1"
                    >
                      <span>OWASP Reference</span>
                      <ExternalLink className="w-3 h-3 inline" />
                    </a>
                  ))}
                </div>
              )}

              {/* Bottom Close Button */}
              <div className="pt-2 flex justify-end">
                <button
                  onClick={() => setActive(null)}
                  className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold rounded-full border border-slate-700 transition-all shadow-md active:scale-95"
                >
                  Close Finding Window
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* 3. List of Collapsed Pill Capsules with layoutId morphing */}
      <div className="space-y-3">
        {findings.map((f) => (
          <motion.div
            layoutId={"card-" + f.id}
            key={f.id}
            onClick={() => setActive(f)}
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
            className={"group flex items-center justify-between px-5 py-3.5 rounded-full bg-slate-900/90 hover:bg-slate-900 border " + getSeverityBorder(f.severity) + " shadow-lg cursor-pointer transition-all duration-300 select-none backdrop-blur-md"}
          >
            <div className="flex items-center space-x-3 min-w-0 pr-3">
              <span className={"w-2.5 h-2.5 rounded-full shrink-0 " + getSeverityDot(f.severity)} />
              <motion.span
                layoutId={"rule-" + f.id}
                className="text-[11px] font-mono font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20 shrink-0"
              >
                {f.ruleId}
              </motion.span>
              <motion.h4
                layoutId={"title-" + f.id}
                className="text-xs sm:text-sm font-bold text-white truncate group-hover:text-emerald-300 transition-colors"
              >
                {f.title}
              </motion.h4>
            </div>

            <div className="flex items-center space-x-3 shrink-0">
              <span className="hidden sm:inline-block font-mono text-[11px] text-slate-400 truncate max-w-[180px]">
                📄 {f.filePath?.split("/").pop()}:{f.startLine}
              </span>
              {getSeverityBadge(f.severity)}
              <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 group-hover:text-white group-hover:bg-emerald-500 group-hover:text-slate-950 transition-all">
                <ChevronRight className="w-3.5 h-3.5" />
              </div>
            </div>
          </motion.div>
        ))}
      </div>
    </>
  );
}

export default ExpandableFindingCards;