import React from "react";
import {
  Shield, Zap, FileCode, Lock, Cpu, ShieldCheck,
  ArrowRight, Sparkles, Terminal, CheckCircle2, AlertTriangle, Code2, Activity
} from "lucide-react";
import AuroraBackground from "./ui/AuroraBackground";
import GlowingEffect from "./ui/GlowingEffect";
import { CanvasText } from "./ui/canvas-text";

export default function LandingView({ onStartAnalysis }) {
  return (
    <AuroraBackground className="relative space-y-10 py-0 font-sans bg-transparent">
      {/* Hero Section - Pulled up with minimal top gap */}
      <section className="text-center max-w-4xl mx-auto space-y-5 relative z-10 pt-0">
        <div className="inline-flex items-center space-x-2 px-3.5 py-1 rounded-full bg-neutral-900 border border-neutral-700/80 text-neutral-200 text-xs font-semibold shadow-sm backdrop-blur-md">
          <Shield className="w-3.5 h-3.5 text-white" />
          <span>Deterministic AST Rules + Nvidia Nemotron 550B Audit</span>
        </div>
        
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold font-display text-white tracking-tight leading-[1.12]">
          Is Your <br className="sm:hidden" />
          <CanvasText
            text="AI-Generated Code"
            colors={[
              "rgba(255, 255, 255, 1)",
              "rgba(220, 220, 220, 0.9)",
              "rgba(180, 180, 180, 0.8)",
              "rgba(140, 140, 140, 0.7)",
              "rgba(100, 100, 100, 0.6)",
              "rgba(70, 70, 70, 0.5)",
              "rgba(50, 50, 50, 0.4)",
              "rgba(30, 30, 30, 0.3)",
            ]}
            animationSpeed={0.6}
            className="mx-1 my-1"
          />
          <br />
          <span className="text-neutral-100">
            Ready for Production?
          </span>
        </h1>

        <p className="text-base sm:text-lg text-neutral-300 max-w-2xl mx-auto leading-relaxed">
          Codexa audits AI-generated applications, detects OWASP vulnerabilities &amp; architectural flaws, ranks findings by real risk, and computes an explainable <strong>Production Readiness Score</strong>.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 pt-1">
          <button
            onClick={onStartAnalysis}
            className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-white hover:bg-neutral-200 text-black font-extrabold text-base flex items-center justify-center space-x-2 shadow-xl shadow-white/10 transition-all transform hover:-translate-y-0.5 active:scale-95 cursor-pointer font-display tracking-tight"
          >
            <span>Start Codebase Audit</span>
            <ArrowRight className="w-5 h-5 text-black" />
          </button>
          <a
            href="https://github.com/Codewithjainam7/Codexa"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-neutral-900/90 hover:bg-neutral-800 text-white border border-neutral-700/80 font-semibold text-base transition-colors flex items-center justify-center space-x-2 shadow-lg font-display"
          >
            <span>View GitHub Source</span>
          </a>
        </div>

        {/* Quick Capabilities Strip (Monochrome) */}
        <div className="pt-2 grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-3xl mx-auto text-left">
          <div className="p-3 rounded-2xl bg-neutral-950/80 border border-neutral-800 backdrop-blur-sm shadow-inner">
            <div className="text-[11px] text-neutral-400 font-mono">AST Engine</div>
            <div className="text-sm font-bold text-white font-display">19+ Security Rules</div>
          </div>
          <div className="p-3 rounded-2xl bg-neutral-950/80 border border-neutral-800 backdrop-blur-sm shadow-inner">
            <div className="text-[11px] text-neutral-400 font-mono">AI Models</div>
            <div className="text-sm font-bold text-white font-display">Nemotron 550B</div>
          </div>
          <div className="p-3 rounded-2xl bg-neutral-950/80 border border-neutral-800 backdrop-blur-sm shadow-inner">
            <div className="text-[11px] text-neutral-400 font-mono">Benchmark</div>
            <div className="text-sm font-bold text-neutral-200 font-display">&lt; 100ms Parsing</div>
          </div>
          <div className="p-3 rounded-2xl bg-neutral-950/80 border border-neutral-800 backdrop-blur-sm shadow-inner">
            <div className="text-[11px] text-neutral-400 font-mono">Sandboxing</div>
            <div className="text-sm font-bold text-neutral-300 font-display">Zero Bytecode Exec</div>
          </div>
        </div>
      </section>

      {/* Rich Aceternity Bento Grid (Monochrome Black & White) */}
      <section className="space-y-4 relative z-10">
        <div className="flex items-center space-x-2 px-1">
          <Sparkles className="w-4 h-4 text-white" />
          <h2 className="text-xs font-mono font-bold uppercase tracking-wider text-neutral-400">
            Engine Architecture &amp; Capabilities
          </h2>
        </div>

        <ul className="grid grid-cols-1 md:grid-cols-12 gap-4 auto-rows-fr">
          {/* Card 1: OWASP Top 10 Security (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-neutral-800/90 p-2 sm:p-3 bg-neutral-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-neutral-900/70 border border-neutral-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-neutral-700 bg-neutral-950 p-2.5 text-white shadow-inner">
                    <Lock className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">OWASP Top 10 Security</h3>
                    <p className="text-xs text-neutral-400 mt-1 leading-relaxed">
                      Deterministic AST checking for SQL injection, command execution, hardcoded credentials, and cryptographic flaws.
                    </p>
                  </div>
                </div>

                {/* Rich interactive visual preview (Monochrome) */}
                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-neutral-800 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/20">SQLi Shield</span>
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/20">RCE Blocked</span>
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/20">XSS Sanitizer</span>
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/20">SSRF Guard</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 2: Center Tall Code Diff Mockup (Span 4, Row Span 2) */}
          <li className="md:col-span-4 md:row-span-2 list-none">
            <div className="relative h-full rounded-3xl border border-neutral-800/90 p-2 sm:p-3 bg-neutral-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={45} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-neutral-900/70 border border-neutral-800/60">
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between">
                    <div className="w-fit rounded-xl border border-neutral-700 bg-neutral-950 p-2.5 text-white shadow-inner">
                      <FileCode className="h-5 w-5" />
                    </div>
                    <span className="text-[10px] font-mono font-bold text-white bg-white/10 px-2 py-0.5 rounded-full border border-white/20">
                      LIVE PATCHING
                    </span>
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Before &amp; After Fix Diffs</h3>
                    <p className="text-xs text-neutral-400 mt-1 leading-relaxed">
                      Side-by-side IDE terminal diff comparison with automatic secret masking and copyable secure fixes.
                    </p>
                  </div>
                </div>

                {/* Mini IDE Terminal Code Window taking up vertical space */}
                <div className="my-auto rounded-xl bg-black border border-neutral-800 p-3 font-mono text-[11px] space-y-2 shadow-inner">
                  <div className="flex items-center justify-between pb-1.5 border-b border-neutral-800 text-[10px] text-neutral-500">
                    <div className="flex items-center space-x-1.5">
                      <span className="w-2 h-2 rounded-full bg-neutral-600" />
                      <span className="w-2 h-2 rounded-full bg-neutral-500" />
                      <span className="w-2 h-2 rounded-full bg-neutral-400" />
                      <span className="ml-1 text-neutral-300 font-sans">auth_handler.ts</span>
                    </div>
                    <span className="text-white font-bold">Auto-Fixed</span>
                  </div>
                  <div className="space-y-1 text-xs">
                    <div className="text-neutral-400 bg-neutral-900 px-2 py-1 rounded border-l-2 border-neutral-600 overflow-x-auto truncate">
                      {'- const q = "SELECT * FROM u WHERE id=" + id;'}
                    </div>
                    <div className="text-white bg-neutral-800 px-2 py-1 rounded border-l-2 border-white overflow-x-auto truncate">
                      {'+ const u = await db.user.find({ id });'}
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-neutral-800 flex items-center justify-between text-[11px] text-neutral-400 font-mono">
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3.5 h-3.5 text-white" />
                    <span>Secret Masking</span>
                  </span>
                  <span className="text-white font-bold">0 Hallucination</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 3: Nvidia Nemotron 550B AI (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-neutral-800/90 p-2 sm:p-3 bg-neutral-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-neutral-900/70 border border-neutral-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-neutral-700 bg-neutral-950 p-2.5 text-white shadow-inner">
                    <Cpu className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Nvidia Nemotron 550B AI</h3>
                    <p className="text-xs text-neutral-400 mt-1 leading-relaxed">
                      State-of-the-art neural code review cascade generating contextual remediation and deep explanations.
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-neutral-800 text-[10px] font-mono">
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/20">Nemotron 550B</span>
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-neutral-300 border border-white/20">Inkling Small</span>
                  <span className="px-2 py-0.5 rounded-full bg-white/10 text-neutral-400 border border-white/20">AST Graph</span>
                </div>
              </div>
            </div>
          </li>

          {/* Card 4: Production Readiness Index (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-neutral-800/90 p-2 sm:p-3 bg-neutral-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-neutral-900/70 border border-neutral-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-neutral-700 bg-neutral-950 p-2.5 text-white shadow-inner">
                    <Zap className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Readiness Scoring (0–100)</h3>
                    <p className="text-xs text-neutral-400 mt-1 leading-relaxed">
                      Mathematical model weighting Security (60%), Quality (25%), and Operations (15%) with failure caps.
                    </p>
                  </div>
                </div>

                {/* Mini readiness meter (Monochrome) */}
                <div className="space-y-1.5 pt-2 border-t border-neutral-800">
                  <div className="flex items-center justify-between text-[10px] font-mono text-neutral-400">
                    <span>Readiness Weighting</span>
                    <span className="text-white font-bold">100% Deterministic</span>
                  </div>
                  <div className="h-2 w-full bg-black rounded-full overflow-hidden flex border border-neutral-800">
                    <div className="bg-white h-full w-[60%]" title="Security 60%" />
                    <div className="bg-neutral-400 h-full w-[25%]" title="Quality 25%" />
                    <div className="bg-neutral-600 h-full w-[15%]" title="Operations 15%" />
                  </div>
                </div>
              </div>
            </div>
          </li>

          {/* Card 5: Zero-Execution Sandboxing (Span 4) */}
          <li className="md:col-span-4 list-none">
            <div className="relative h-full rounded-3xl border border-neutral-800/90 p-2 sm:p-3 bg-neutral-950/90 backdrop-blur-xl group transition-all">
              <GlowingEffect spread={40} glow={true} disabled={false} proximity={64} inactiveZone={0.01} />
              <div className="relative flex h-full flex-col justify-between gap-4 rounded-2xl p-5 bg-neutral-900/70 border border-neutral-800/60">
                <div className="space-y-3">
                  <div className="w-fit rounded-xl border border-neutral-700 bg-neutral-950 p-2.5 text-white shadow-inner">
                    <ShieldCheck className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-display text-lg font-bold text-white">Zero-Execution Sandboxing</h3>
                    <p className="text-xs text-neutral-400 mt-1 leading-relaxed">
                      Safe archive decompression with Zip Slip protection, bomb size quotas, and zero untrusted bytecode execution.
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-neutral-800 text-[10px] font-mono text-neutral-400">
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-white" />
                    <span>Zip Slip Block</span>
                  </span>
                  <span className="flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3 text-white" />
                    <span>Quota Enforced</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </section>
    </AuroraBackground>
  );
}

