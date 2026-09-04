import React from 'react';
import { Shield, Github, FileCode, Terminal, Lock, ExternalLink, AlertTriangle, Sparkles, CheckCircle2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="relative border-t border-neutral-800 bg-black pt-14 pb-4 overflow-hidden mt-16 font-sans">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        {/* Main Grid Columns */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-10 pb-12">
          {/* Brand Column (Span 4) */}
          <div className="md:col-span-4 space-y-4">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-neutral-400 to-white p-0.5 flex items-center justify-center shadow-lg shadow-white/20">
                <div className="w-full h-full bg-black rounded-[10px] flex items-center justify-center">
                  <Shield className="w-4 h-4 text-white" />
                </div>
              </div>
              <span className="text-xl font-bold font-display tracking-tight text-white">
                Codexa
              </span>
              <span className="text-[10px] font-mono font-bold bg-white/10 text-white border border-white/20 px-2 py-0.5 rounded-full">
                MVP
              </span>
            </div>

            <p className="text-xs text-neutral-400 leading-relaxed max-w-sm">
              Automated static analysis, OWASP Top 10 auditing, and explainable AI-assisted code remediation for production-grade software.
            </p>

            <div className="text-xs text-neutral-500 pt-1 font-mono">
              © 2026 Codexa Security Platform. All rights reserved.
            </div>
          </div>

          {/* Nav Columns (Span 8) */}
          <div className="md:col-span-8 grid grid-cols-2 sm:grid-cols-4 gap-8 text-xs">
            {/* Column 1: Platform */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-neutral-200 tracking-wider text-[13px]">
                Platform
              </h4>
              <ul className="space-y-2.5 text-neutral-400">
                <li><span className="hover:text-white cursor-pointer transition-colors">AST Static Engine</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Multi-Language SAST</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Readiness Index (0–100)</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Live Fix Terminal</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Zero Bytecode Exec</span></li>
              </ul>
            </div>

            {/* Column 2: AI Cascade */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-neutral-200 tracking-wider text-[13px]">
                AI Models
              </h4>
              <ul className="space-y-2.5 text-neutral-400">
                <li><span className="hover:text-white cursor-pointer transition-colors">Nemotron 3 Ultra 550B</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Nemotron 3.5 Lightning</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Inkling Small</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Secret Masking Guard</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Deterministic Fallback</span></li>
              </ul>
            </div>

            {/* Column 3: Security */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-neutral-200 tracking-wider text-[13px]">
                Security
              </h4>
              <ul className="space-y-2.5 text-neutral-400">
                <li><span className="hover:text-white cursor-pointer transition-colors">OWASP Top 10</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Zip Slip Protection</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">Path Traversal Block</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">SSRF Prevention</span></li>
                <li><span className="hover:text-white cursor-pointer transition-colors">ReDoS &amp; PRNG Check</span></li>
              </ul>
            </div>

            {/* Column 4: Links */}
            <div className="space-y-3">
              <h4 className="font-semibold font-display text-neutral-200 tracking-wider text-[13px]">
                Links
              </h4>
              <ul className="space-y-2.5 text-neutral-400">
                <li>
                  <a
                    href="https://github.com/Codewithjainam7/Codexa"
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-white flex items-center space-x-1.5 transition-colors"
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
                    className="hover:text-white flex items-center space-x-1.5 transition-colors"
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
                    className="hover:text-white flex items-center space-x-1.5 transition-colors"
                  >
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>OpenRouter AI</span>
                  </a>
                </li>
                <li><span className="hover:text-white cursor-pointer transition-colors">MIT Open License</span></li>
              </ul>
            </div>
          </div>
        </div>

        {/* Advisory Disclaimer Notice */}
        <div className="border-t border-neutral-800 pt-6 pb-2 text-[11px] text-neutral-500 leading-relaxed flex items-start space-x-2.5">
          <AlertTriangle className="w-4 h-4 text-neutral-400 flex-shrink-0 mt-0.5" />
          <p>
            <span className="text-neutral-400 font-semibold">Advisory Disclaimer:</span> Codexa is an automated static code review and production-readiness assessment engine. It does not execute untrusted bytecode. Suggested remediations should always be verified and tested prior to production deployment.
          </p>
        </div>
      </div>

      {/* Giant Stylized Codexa Watermark Typography at the end of the footer */}
      <div className="w-full select-none pointer-events-none overflow-hidden flex justify-center items-end -mb-4 sm:-mb-8 lg:-mb-12">
        <span className="font-display font-black tracking-tighter text-6xl sm:text-9xl md:text-[12rem] lg:text-[16rem] text-transparent bg-clip-text bg-gradient-to-b from-neutral-700/25 via-neutral-700/10 to-transparent leading-none text-center transform translate-y-4">
          Codexa
        </span>
      </div>
    </footer>
  );
}
