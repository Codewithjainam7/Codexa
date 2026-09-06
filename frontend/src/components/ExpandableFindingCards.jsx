"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import AnimatedList from "@/components/magicui/AnimatedList";
import CodeDiffViewer from "./CodeDiffViewer";
import {
  FileCode, ShieldCheck, Flame, Info, ExternalLink,
  ChevronDown, ChevronUp, AlertCircle
} from "lucide-react";

export function ExpandableFindingCards({ findings = [], getSeverityBadge }) {
  const [expandedIds, setExpandedIds] = useState(new Set());

  const toggleExpand = (id) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const toggleAll = () => {
    if (expandedIds.size === findings.length) {
      setExpandedIds(new Set());
    } else {
      setExpandedIds(new Set(findings.map((f) => f.id)));
    }
  };

  const getSeverityBorder = (sev) => {
    switch (sev) {
      case "CRITICAL":
        return "border-rose-500/40 hover:border-rose-500/80 shadow-rose-500/5";
      case "HIGH":
        return "border-orange-500/40 hover:border-orange-500/80 shadow-orange-500/5";
      case "MEDIUM":
        return "border-amber-500/40 hover:border-amber-500/80 shadow-amber-500/5";
      default:
        return "border-blue-500/40 hover:border-blue-500/80 shadow-blue-500/5";
    }
  };

  const getSeverityDot = (sev) => {
    switch (sev) {
      case "CRITICAL":
        return "bg-rose-500 animate-pulse";
      case "HIGH":
        return "bg-orange-500";
      case "MEDIUM":
        return "bg-amber-400";
      default:
        return "bg-blue-400";
    }
  };

  return (
    <div className="space-y-4">
      {findings.length > 1 && (
        <div className="flex justify-end pb-1">
          <button
            onClick={toggleAll}
            className="text-xs font-semibold text-slate-700 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-300 flex items-center space-x-1.5 px-3 py-1 rounded-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm transition-colors cursor-pointer"
          >
            {expandedIds.size === findings.length ? (
              <>
                <ChevronUp className="w-3.5 h-3.5" />
                <span>Collapse All</span>
              </>
            ) : (
              <>
                <ChevronDown className="w-3.5 h-3.5" />
                <span>Expand All</span>
              </>
            )}
          </button>
        </div>
      )}

      <AnimatedList delay={100}>
        {findings.map((f) => {
          const isExpanded = expandedIds.has(f.id);

          return (
            <div
              key={f.id}
              className={"rounded-2xl cdx-glass-card border " + getSeverityBorder(f.severity) + " transition-all duration-200 overflow-hidden shadow-lg"}
            >
              {/* Header Pill Row (Clickable & Touch-Friendly) */}
              <div
                onClick={() => toggleExpand(f.id)}
                className="flex items-center justify-between p-3 sm:p-4 cursor-pointer select-none hover:bg-blue-500/5 transition-colors gap-2"
              >
                <div className="flex items-center space-x-2 sm:space-x-3 min-w-0 pr-1 sm:pr-3">
                  <span className={"w-2 sm:w-2.5 h-2 sm:h-2.5 rounded-full shrink-0 " + getSeverityDot(f.severity)} />
                  <span className="text-[10px] sm:text-[11px] font-mono font-bold text-blue-600 dark:text-blue-400 bg-blue-500/10 px-2 sm:px-2.5 py-0.5 rounded-full border border-blue-500/20 shrink-0">
                    {f.ruleId}
                  </span>
                  <h4 className="text-xs sm:text-sm font-bold text-[var(--text-primary)] truncate hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    {f.title}
                  </h4>
                </div>

                <div className="flex items-center space-x-2 sm:space-x-3 shrink-0">
                  <span className="hidden md:inline-block font-mono text-[11px] text-[var(--text-muted)] truncate max-w-[180px]">
                    📄 {f.filePath?.split("/").pop()}:{f.startLine}
                  </span>
                  <div className="shrink-0 scale-90 sm:scale-100">
                    {getSeverityBadge(f.severity)}
                  </div>
                  <div className={"w-5 h-5 sm:w-6 sm:h-6 rounded-full cdx-recessed flex items-center justify-center text-[var(--text-muted)] transition-transform duration-200 shrink-0 " + (isExpanded ? "rotate-180 text-blue-600 dark:text-blue-400 bg-blue-500/10" : "")}>
                    <ChevronDown className="w-3.5 h-3.5 sm:w-3.5 sm:h-3.5" />
                  </div>
                </div>
              </div>

              {/* In-Place Expanded Content (No Full-Screen Modal) */}
              <AnimatePresence initial={false}>
                {isExpanded && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: "auto", opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                    transition={{ duration: 0.25, ease: "easeInOut" }}
                    className="overflow-hidden border-t border-[var(--border-subtle)]"
                  >
                    <div className="p-5 sm:p-6 space-y-4 bg-[var(--bg-recessed)]/50 backdrop-blur-md">
                      {/* Meta Tags */}
                      <div className="flex flex-wrap items-center gap-2 text-xs">
                        <span className="font-mono text-[var(--text-secondary)] cdx-pill px-3 py-1 rounded-full border border-[var(--border-subtle)] flex items-center space-x-1.5">
                          <FileCode className="w-3.5 h-3.5 text-[var(--text-muted)]" />
                          <span>{f.filePath}:{f.startLine}</span>
                        </span>
                        {f.owaspMapping && (
                          <span className="text-blue-700 dark:text-blue-400 bg-blue-500/10 border border-blue-500/20 px-3 py-1 rounded-full font-medium flex items-center space-x-1.5">
                            <ShieldCheck className="w-3.5 h-3.5" />
                            <span>{f.owaspMapping}</span>
                          </span>
                        )}
                        {f.requiresManualReview && (
                          <span className="px-3 py-1 text-[10px] font-semibold bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-500/20 rounded-full">
                            Review Required
                          </span>
                        )}
                        <span className="text-xs font-mono text-[var(--text-muted)] ml-auto">
                          Priority Weight: <strong className="text-[var(--text-primary)]">{f.priorityScore}</strong>
                        </span>
                      </div>

                      {/* Vulnerability Analysis */}
                      <div className="bg-white dark:bg-slate-950/80 p-4 rounded-xl border border-slate-200 dark:border-slate-800 space-y-1 shadow-sm">
                        <div className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider flex items-center space-x-1.5">
                          <Info className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                          <span>Vulnerability Analysis</span>
                        </div>
                        <p className="text-xs sm:text-sm text-slate-800 dark:text-slate-300 leading-relaxed pt-1">
                          {f.description}
                        </p>
                      </div>

                      {/* Potential Impact */}
                      {f.impact && (
                        <div className="p-4 bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-500/30 rounded-xl text-xs sm:text-sm text-slate-800 dark:text-slate-300 space-y-1">
                          <strong className="text-rose-700 dark:text-rose-400 font-bold block flex items-center space-x-1.5">
                            <Flame className="w-4 h-4" />
                            <span>Potential Security & Architectural Risk:</span>
                          </strong>
                          <p className="leading-relaxed text-slate-700 dark:text-slate-300/90">{f.impact}</p>
                        </div>
                      )}

                      {/* Side-by-Side Code Diff Viewer */}
                      <CodeDiffViewer
                        originalCode={f.evidenceMasked}
                        suggestedFix={f.suggestedFix}
                        ruleId={f.ruleId}
                      />

                      {/* Security References */}
                      {f.references && f.references.length > 0 && (
                        <div className="pt-2 border-t border-slate-200 dark:border-slate-800/80 flex flex-wrap items-center gap-2 text-[11px]">
                          <span className="text-slate-500 dark:text-slate-400 font-medium">References:</span>
                          {f.references.map((ref, idx) => (
                            <a
                              key={idx}
                              href={ref}
                              target="_blank"
                              rel="noreferrer"
                              className="px-2.5 py-0.5 bg-white dark:bg-slate-950 rounded-full border border-slate-200 dark:border-slate-800 text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 hover:border-blue-400/40 transition-all flex items-center space-x-1 shadow-sm"
                            >
                              <span>OWASP Advisory</span>
                              <ExternalLink className="w-3 h-3 inline" />
                            </a>
                          ))}
                        </div>
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          );
        })}
      </AnimatedList>
    </div>
  );
}

export default ExpandableFindingCards;