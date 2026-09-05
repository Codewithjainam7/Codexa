"use client";
import React from 'react';
import { Shield, Github, FileCode, Terminal, Lock, ExternalLink, AlertTriangle, Cpu, CheckCircle2, GitBranch } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="relative z-30 border-t border-[var(--border-subtle)] bg-[#F8FAFC] dark:bg-[#0C0D10] pt-16 pb-6 overflow-hidden mt-20 font-sans transition-colors duration-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        {/* Main Grid Columns */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-10 pb-12">
          {/* Brand Column (Span 4) */}
          <div className="md:col-span-4 space-y-4">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-500 via-indigo-600 to-blue-700 p-[1px] flex items-center justify-center shadow-md">
                <div className="w-full h-full bg-[var(--bg-card)] rounded-[10px] flex items-center justify-center overflow-hidden p-0.5">
                  <img src="/logo.png" alt="Codexa Logo" className="w-full h-full object-contain" />
                </div>
              </div>
              <span className="text-xl font-bold font-display tracking-tight text-[var(--text-primary)]">
                Codexa
              </span>
              <span className="text-[10px] font-mono font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20 px-2 py-0.5 rounded-full">
                v2.4
              </span>
            </div>

            <p className="text-xs text-[var(--text-secondary)] leading-relaxed max-w-sm font-normal">
              Automated static analysis, OWASP Top 10 auditing, and explainable AI-assisted code remediation for production-grade software.
            </p>

            <div className="text-xs text-[var(--text-muted)] pt-1 font-mono">
              © 2026 Codexa Security Platform. Open source MIT.
            </div>
          </div>

          {/* Nav Columns (Span 8) */}
          <div className="md:col-span-8 grid grid-cols-2 sm:grid-cols-4 gap-8 text-xs">
            {/* Column 1: Platform */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-[var(--text-primary)] tracking-wider text-[13px]">
                Platform
              </h4>
              <ul className="space-y-2.5 text-[var(--text-secondary)]">
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">AST Static Engine</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Multi-Language SAST</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Readiness Index (0–100)</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Live Fix Terminal</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Zero Bytecode Exec</span></li>
              </ul>
            </div>

            {/* Column 2: AI Cascade */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-[var(--text-primary)] tracking-wider text-[13px]">
                AI Models
              </h4>
              <ul className="space-y-2.5 text-[var(--text-secondary)]">
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Nemotron 3 Ultra 550B</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Nemotron 3.5 Lightning</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Inkling Small</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Secret Masking Guard</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Deterministic Fallback</span></li>
              </ul>
            </div>

            {/* Column 3: Security */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-[var(--text-primary)] tracking-wider text-[13px]">
                Security
              </h4>
              <ul className="space-y-2.5 text-[var(--text-secondary)]">
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">OWASP Top 10</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Zip Slip Protection</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">Path Traversal Block</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">SSRF Prevention</span></li>
                <li><span className="hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors">ReDoS &amp; PRNG Check</span></li>
              </ul>
            </div>

            {/* Column 4: Links */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-[var(--text-primary)] tracking-wider text-[13px]">
                Links
              </h4>
              <ul className="space-y-2.5 text-[var(--text-secondary)]">
                <li>
                  <a
                    href="https://github.com/Codewithjainam7/Codexa"
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 dark:hover:text-blue-400 flex items-center space-x-1.5 transition-colors"
                  >
                    <Github className="w-3.5 h-3.5" />
                    <span>GitHub Repo</span>
                  </a>
                </li>
                <li>
                  <a
                    href="http://localhost:8080/swagger-ui.html"
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 dark:hover:text-blue-400 flex items-center space-x-1.5 transition-colors"
                  >
                    <Terminal className="w-3.5 h-3.5" />
                    <span>Swagger API Docs</span>
                  </a>
                </li>
                <li>
                  <a
                    href="https://openrouter.ai"
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 dark:hover:text-blue-400 flex items-center space-x-1.5 transition-colors"
                  >
                    <Cpu className="w-3.5 h-3.5" />
                    <span>OpenRouter AI</span>
                  </a>
                </li>
                <li>
                  <a
                    href="https://owasp.org/www-project-top-ten/"
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 dark:hover:text-blue-400 flex items-center space-x-1.5 transition-colors"
                  >
                    <Lock className="w-3.5 h-3.5" />
                    <span>OWASP Standards</span>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Advisory Disclaimer Notice */}
        <div className="border-t border-[var(--border-subtle)] pt-6 pb-2 text-[11px] text-[var(--text-muted)] leading-relaxed flex items-start space-x-2.5">
          <AlertTriangle className="w-4 h-4 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
          <p>
            <span className="text-[var(--text-primary)] font-semibold">Advisory Disclaimer:</span> Codexa is an automated static code review and production-readiness assessment engine. It does not execute untrusted bytecode. Suggested remediations should always be verified and tested prior to production deployment.
          </p>
        </div>
      </div>

      {/* Giant Stylized Codexa Watermark Typography at the end of the footer (Preserved concept & reskinned) */}
      <div className="w-full select-none pointer-events-none overflow-hidden flex justify-center items-end -mb-4 sm:-mb-8 lg:-mb-12 opacity-40 dark:opacity-25">
        <span className="font-display font-black tracking-tighter text-6xl sm:text-9xl md:text-[12rem] lg:text-[16rem] text-transparent bg-clip-text bg-gradient-to-b from-[var(--text-muted)] via-[var(--border-medium)] to-transparent leading-none text-center transform translate-y-4">
          Codexa
        </span>
      </div>
    </footer>
  );
}
