import React from 'react';
import { AlertTriangle, Github } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="border-t border-slate-900 bg-slate-950 py-8 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4 mb-6 text-xs text-amber-300 flex items-start space-x-3">
          <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-400 mt-0.5" />
          <div>
            <span className="font-semibold uppercase tracking-wider block mb-1">Advisory Disclaimer</span>
            Codexa is an automated static code review, security analysis, and production-readiness assessment platform. It does not compile or execute untrusted code. A clean scan does not guarantee the absence of all vulnerabilities, and suggested remediations must be verified and tested by human engineers prior to deployment.
          </div>
        </div>

        <div className="flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
          <p>© 2026 Codexa Security Platform. Built for developers, students, and early-stage startups.</p>
          <div className="flex items-center space-x-4 mt-4 sm:mt-0">
            <a
              href="https://github.com/Codewithjainam7/Codexa"
              target="_blank"
              rel="noreferrer"
              className="hover:text-slate-300 flex items-center space-x-1"
            >
              <Github className="w-4 h-4" />
              <span>GitHub Repository</span>
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
