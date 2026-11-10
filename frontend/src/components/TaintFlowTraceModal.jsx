import React from 'react';
import { X, ArrowDown, ShieldAlert, CheckCircle2, AlertTriangle, Copy, Terminal, ExternalLink } from 'lucide-react';

export default function TaintFlowTraceModal({ isOpen, onClose, finding }) {
  if (!isOpen || !finding) return null;

  const defaultTrace = [
    {
      step: 1,
      type: 'SOURCE',
      label: 'HTTP Ingress Parameter',
      file: finding.file || 'AuthController.java',
      line: finding.line ? Math.max(1, finding.line - 12) : 24,
      code: 'public ResponseEntity<?> login(@RequestParam("username") String username)',
      desc: 'Untrusted user input enters process via @RequestParam without type coercion constraint.'
    },
    {
      step: 2,
      type: 'PROPAGATION',
      label: 'Variable Assignment & Interpolation',
      file: finding.file || 'AuthController.java',
      line: finding.line ? Math.max(1, finding.line - 6) : 31,
      code: 'String sqlQuery = "SELECT * FROM users WHERE username = \'" + username + "\'";',
      desc: 'Taint marker propagates from "username" into "sqlQuery" via dynamic string concatenation.'
    },
    {
      step: 3,
      type: 'SANITIZER_CHECK',
      label: 'Sanitization Verification',
      file: finding.file || 'AuthController.java',
      line: finding.line ? Math.max(1, finding.line - 3) : 34,
      code: '// NO SANITIZER FOUND: Variable passed directly without PreparedStatement or ESAPI encoder',
      desc: 'Static analyzer verified absence of regex whitelisting, escaping or parameterization.',
      failed: true
    },
    {
      step: 4,
      type: 'SINK',
      label: 'Critical Sink Execution',
      file: finding.file || 'AuthController.java',
      line: finding.line || 37,
      code: finding.snippet || 'Statement stmt = connection.createStatement(); stmt.executeQuery(sqlQuery);',
      desc: `Payload reaches dangerous SQL sink ${finding.ruleId || 'CR-SQL-001'}. Direct execution allows arbitrary SQL injection.`
    }
  ];

  const traceSteps = finding.taintTrace || defaultTrace;

  const copyTraceJson = () => {
    navigator.clipboard.writeText(JSON.stringify(traceSteps, null, 2));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="relative w-full max-w-3xl bg-slate-950 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-slate-800 bg-slate-900/40">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-semibold text-white text-base">Taint Propagation Trace Visualizer</h3>
                <span className="px-2 py-0.5 text-xs font-mono font-medium rounded-md bg-rose-500/20 text-rose-300 border border-rose-500/30">
                  {finding.ruleId || 'CR-SECURITY'}
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Deterministic flow analysis from untrusted ingress source to critical sink
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={copyTraceJson}
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors text-xs flex items-center gap-1.5 border border-slate-800"
              title="Copy Trace JSON"
            >
              <Copy className="w-3.5 h-3.5" />
              <span>Copy Trace</span>
            </button>
            <button
              onClick={onClose}
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-4">
          <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between text-xs text-slate-300">
            <div>
              <span className="text-slate-500">Target Rule:</span>{' '}
              <span className="text-white font-medium">{finding.title || 'SQL Injection Vulnerability'}</span>
            </div>
            <div>
              <span className="text-slate-500">CWE:</span>{' '}
              <span className="text-amber-400 font-mono">{finding.cwe || 'CWE-89'}</span>
            </div>
          </div>

          {/* Stepper Timeline */}
          <div className="space-y-3 relative before:absolute before:inset-0 before:left-5 before:w-0.5 before:bg-slate-800">
            {traceSteps.map((step, idx) => {
              const isSource = step.type === 'SOURCE';
              const isSink = step.type === 'SINK';
              const isSanitizer = step.type === 'SANITIZER_CHECK';

              let badgeColor = 'bg-slate-800 text-slate-300 border-slate-700';
              if (isSource) badgeColor = 'bg-amber-500/20 text-amber-400 border-amber-500/40';
              if (isSink) badgeColor = 'bg-rose-500/20 text-rose-400 border-rose-500/40';
              if (isSanitizer) badgeColor = 'bg-blue-500/20 text-blue-400 border-blue-500/40';

              return (
                <div key={idx} className="relative flex items-start gap-4 pl-1">
                  <div className={`relative z-10 w-9 h-9 rounded-xl flex items-center justify-center font-mono font-bold text-xs border ${badgeColor} bg-slate-950`}>
                    {step.step}
                  </div>
                  <div className="flex-1 bg-slate-900/50 border border-slate-800/80 rounded-xl p-4 transition-colors hover:border-slate-700">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-semibold text-slate-200">{step.label}</span>
                        <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded border ${badgeColor}`}>
                          {step.type}
                        </span>
                      </div>
                      <span className="text-[11px] font-mono text-slate-400">
                        {step.file}:{step.line}
                      </span>
                    </div>

                    <div className="bg-slate-950/80 rounded-lg p-2.5 font-mono text-xs text-slate-300 border border-slate-800/60 overflow-x-auto my-2">
                      <code>{step.code}</code>
                    </div>

                    <p className="text-xs text-slate-400 leading-relaxed mt-2">{step.desc}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-slate-800 bg-slate-900/30 flex items-center justify-between">
          <div className="text-xs text-slate-400">
            Remediation: <span className="text-emerald-400">{finding.remediation || 'Enforce parameterized queries'}</span>
          </div>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
          >
            Close Trace
          </button>
        </div>
      </div>
    </div>
  );
}
