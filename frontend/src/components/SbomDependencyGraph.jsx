import React, { useState, useMemo } from 'react';
import { 
  Shield, AlertTriangle, CheckCircle, Search, Filter, 
  ExternalLink, ArrowUpRight, Cpu, Layers, GitBranch, Zap, Info, Package
} from 'lucide-react';

const DEFAULT_DEPENDENCY_DATA = [
  {
    id: 'pkg-1',
    name: 'org.springframework.boot:spring-boot-starter-web',
    version: '3.2.0',
    type: 'direct',
    license: 'Apache-2.0',
    risk: 'CLEAN',
    blastRadiusScore: 20,
    impactedModules: ['ApiController', 'WebSecurityConfig'],
    cves: [],
    recommendedVersion: '3.2.3'
  },
  {
    id: 'pkg-2',
    name: 'org.apache.logging.log4j:log4j-core',
    version: '2.14.1',
    type: 'direct',
    license: 'Apache-2.0',
    risk: 'CRITICAL',
    blastRadiusScore: 95,
    impactedModules: ['AuditLogger', 'OrderService', 'PaymentGateway', 'AuthFilter'],
    cves: [
      { id: 'CVE-2021-44228', score: 10.0, summary: 'Remote Code Execution in JNDI lookup (Log4Shell)' },
      { id: 'CVE-2021-45046', score: 9.0, summary: 'Thread Context Message Pattern RCE' }
    ],
    recommendedVersion: '2.22.1'
  },
  {
    id: 'pkg-3',
    name: 'com.fasterxml.jackson.core:jackson-databind',
    version: '2.13.0',
    type: 'transitive',
    license: 'Apache-2.0',
    risk: 'HIGH',
    blastRadiusScore: 78,
    impactedModules: ['JsonParserService', 'WebhookReceiver'],
    cves: [
      { id: 'CVE-2020-36518', score: 7.5, summary: 'Denial of Service via deep nesting' }
    ],
    recommendedVersion: '2.17.0'
  },
  {
    id: 'pkg-4',
    name: 'org.postgresql:postgresql',
    version: '42.6.0',
    type: 'direct',
    license: 'BSD-2-Clause',
    risk: 'CLEAN',
    blastRadiusScore: 35,
    impactedModules: ['UserRepository', 'TransactionDbConfig'],
    cves: [],
    recommendedVersion: '42.7.2'
  },
  {
    id: 'pkg-5',
    name: 'org.gnu:copyleft-helper',
    version: '1.4.0',
    type: 'transitive',
    license: 'AGPL-3.0',
    risk: 'LEGAL_COPYLEFT',
    blastRadiusScore: 85,
    impactedModules: ['CoreAnalyticsEngine'],
    cves: [
      { id: 'LIC-RISK-01', score: 8.5, summary: 'Viral Copyleft AGPL-3.0 license mandates open-sourcing proprietary SaaS backend' }
    ],
    recommendedVersion: 'MIT alternative: helper-core 2.0'
  },
  {
    id: 'pkg-6',
    name: 'io.jsonwebtoken:jjwt-api',
    version: '0.11.5',
    type: 'direct',
    license: 'Apache-2.0',
    risk: 'CLEAN',
    blastRadiusScore: 40,
    impactedModules: ['JwtTokenProvider', 'SecurityFilter'],
    cves: [],
    recommendedVersion: '0.12.5'
  }
];

export default function SbomDependencyGraph({ job, findings = [] }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [filterRisk, setFilterRisk] = useState('ALL'); // 'ALL' | 'VULNERABLE' | 'COPYLEFT' | 'CLEAN'
  const [selectedPackageId, setSelectedPackageId] = useState('pkg-2'); // Default to critical log4j

  // In a real run, augment with findings mentioning dependencies
  const dependencies = useMemo(() => {
    return DEFAULT_DEPENDENCY_DATA;
  }, []);

  const filteredDependencies = useMemo(() => {
    return dependencies.filter(pkg => {
      const matchesSearch = pkg.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            pkg.license.toLowerCase().includes(searchQuery.toLowerCase());
      if (!matchesSearch) return false;

      if (filterRisk === 'VULNERABLE') return pkg.risk === 'CRITICAL' || pkg.risk === 'HIGH';
      if (filterRisk === 'COPYLEFT') return pkg.risk === 'LEGAL_COPYLEFT';
      if (filterRisk === 'CLEAN') return pkg.risk === 'CLEAN';
      return true;
    });
  }, [dependencies, searchQuery, filterRisk]);

  const selectedPkg = useMemo(() => {
    return dependencies.find(p => p.id === selectedPackageId) || dependencies[0];
  }, [dependencies, selectedPackageId]);

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <Package className="w-4 h-4 text-blue-500" />
              <h3 className="text-sm sm:text-base font-bold text-[var(--text-primary)] font-display">
                Interactive Visual Dependency &amp; SBOM Blast-Radius Graph
              </h3>
            </div>
            <p className="text-xs text-[var(--text-secondary)]">
              Map direct &amp; transitive dependency trees, evaluate CVE blast radiuses, and audit viral open-source licensing.
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <span className="px-2.5 py-1 rounded-full text-[11px] font-mono font-bold bg-blue-500/10 text-blue-400 border border-blue-500/25">
              CycloneDX v1.5 / SPDX
            </span>
          </div>
        </div>

        {/* Filter controls */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-[var(--border-subtle)]">
          <div className="flex items-center gap-1.5 overflow-x-auto">
            {['ALL', 'VULNERABLE', 'COPYLEFT', 'CLEAN'].map((f) => (
              <button
                key={f}
                onClick={() => setFilterRisk(f)}
                className={`px-3 py-1 rounded-lg text-xs font-semibold font-display transition-all cursor-pointer ${
                  filterRisk === f
                    ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30 shadow-sm'
                    : 'cdx-recessed text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                }`}
              >
                {f === 'ALL' && 'All Packages'}
                {f === 'VULNERABLE' && 'Vulnerabilities Only'}
                {f === 'COPYLEFT' && 'License Risks'}
                {f === 'CLEAN' && 'Clean / Secure'}
              </button>
            ))}
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              type="text"
              placeholder="Search dependencies..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 rounded-xl bg-[var(--bg-recessed)] border border-[var(--border-subtle)] text-xs text-[var(--text-primary)] placeholder-[var(--text-muted)] outline-none focus:border-blue-500/50"
            />
          </div>
        </div>
      </div>

      {/* Main Graph & Inspector Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Visual Graph Canvas (Left 7 cols) */}
        <div className="lg:col-span-7 rounded-2xl cdx-card border border-[var(--border-subtle)] p-5 relative overflow-hidden flex flex-col justify-between min-h-[440px]">
          <div className="flex items-center justify-between text-xs font-mono text-[var(--text-muted)] pb-3 border-b border-[var(--border-subtle)]">
            <span>Visual Dependency Topology</span>
            <span className="flex items-center space-x-2">
              <span className="flex items-center space-x-1"><span className="w-2 h-2 rounded-full bg-rose-500"></span><span>Critical</span></span>
              <span className="flex items-center space-x-1"><span className="w-2 h-2 rounded-full bg-amber-500"></span><span>Legal</span></span>
              <span className="flex items-center space-x-1"><span className="w-2 h-2 rounded-full bg-emerald-500"></span><span>Safe</span></span>
            </span>
          </div>

          {/* Node Link Topology Canvas */}
          <div className="relative py-6 flex flex-col items-center justify-center flex-1">
            {/* Root Application Node */}
            <div className="p-3 rounded-2xl bg-gradient-to-r from-blue-600/30 to-indigo-600/30 border border-blue-500/50 text-center shadow-lg mb-8 max-w-xs w-full">
              <div className="text-[10px] font-mono text-blue-300 font-bold uppercase tracking-wider">Root Ingestion Target</div>
              <div className="text-xs font-bold font-display text-white truncate">
                {job?.sourceIdentifier || 'Target Repository'}
              </div>
            </div>

            {/* Connecting Lines (SVG overlay) */}
            <div className="w-full grid grid-cols-1 sm:grid-cols-2 gap-3 relative z-10">
              {filteredDependencies.map((pkg) => {
                const isSelected = selectedPackageId === pkg.id;
                const isCritical = pkg.risk === 'CRITICAL';
                const isHigh = pkg.risk === 'HIGH';
                const isCopyleft = pkg.risk === 'LEGAL_COPYLEFT';

                let borderStyle = 'border-[var(--border-subtle)]';
                let tagColor = 'bg-emerald-500/10 text-emerald-400 border-emerald-500/25';
                if (isCritical) {
                  borderStyle = 'border-rose-500/60 shadow-md shadow-rose-500/10';
                  tagColor = 'bg-rose-500/15 text-rose-400 border-rose-500/30';
                } else if (isHigh) {
                  borderStyle = 'border-orange-500/60 shadow-md shadow-orange-500/10';
                  tagColor = 'bg-orange-500/15 text-orange-400 border-orange-500/30';
                } else if (isCopyleft) {
                  borderStyle = 'border-amber-500/60 shadow-md shadow-amber-500/10';
                  tagColor = 'bg-amber-500/15 text-amber-400 border-amber-500/30';
                }

                return (
                  <div
                    key={pkg.id}
                    onClick={() => setSelectedPackageId(pkg.id)}
                    className={`p-3 rounded-xl cdx-recessed border transition-all cursor-pointer relative overflow-hidden group ${borderStyle} ${
                      isSelected ? 'ring-2 ring-blue-500 scale-[1.02]' : 'hover:scale-[1.01]'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-1.5 mb-1.5">
                      <span className={`px-1.5 py-0.5 rounded text-[9px] font-mono font-bold uppercase border ${tagColor}`}>
                        {pkg.risk.replace('_', ' ')}
                      </span>
                      <span className="text-[10px] font-mono text-[var(--text-muted)]">
                        v{pkg.version}
                      </span>
                    </div>

                    <div className="text-xs font-bold font-mono text-[var(--text-primary)] truncate group-hover:text-blue-400 transition-colors">
                      {pkg.name.split(':').pop() || pkg.name}
                    </div>

                    <div className="flex items-center justify-between text-[10px] font-mono text-[var(--text-muted)] mt-2 pt-1 border-t border-[var(--border-subtle)]">
                      <span>{pkg.type}</span>
                      <span>Blast: {pkg.blastRadiusScore}%</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="pt-3 border-t border-[var(--border-subtle)] flex items-center justify-between text-[11px] font-mono text-[var(--text-muted)]">
            <span>Showing {filteredDependencies.length} evaluated packages</span>
            <span>Click any node to inspect blast radius</span>
          </div>
        </div>

        {/* Selected Package Blast-Radius Inspector (Right 5 cols) */}
        <div className="lg:col-span-5 flex flex-col space-y-4">
          <div className="p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-4 flex-1">
            <div className="flex items-start justify-between gap-2">
              <div className="space-y-1">
                <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold uppercase bg-blue-500/15 text-blue-400 border border-blue-500/30">
                  Node Inspector
                </span>
                <h4 className="text-sm font-bold font-mono text-[var(--text-primary)] break-all pt-1">
                  {selectedPkg.name}
                </h4>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 text-xs font-mono">
              <div className="p-2.5 rounded-xl cdx-recessed border border-[var(--border-subtle)]">
                <div className="text-[10px] text-[var(--text-muted)] uppercase">Version</div>
                <div className="font-bold text-[var(--text-primary)] mt-0.5">v{selectedPkg.version}</div>
              </div>
              <div className="p-2.5 rounded-xl cdx-recessed border border-[var(--border-subtle)]">
                <div className="text-[10px] text-[var(--text-muted)] uppercase">License</div>
                <div className="font-bold text-[var(--text-primary)] mt-0.5">{selectedPkg.license}</div>
              </div>
            </div>

            {/* Blast Radius Gauge */}
            <div className="p-3.5 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
              <div className="flex items-center justify-between text-xs font-mono">
                <span className="text-slate-400">Blast Radius Exposure</span>
                <span className={`font-bold ${
                  selectedPkg.blastRadiusScore > 70 ? 'text-rose-400' :
                  selectedPkg.blastRadiusScore > 40 ? 'text-amber-400' : 'text-emerald-400'
                }`}>
                  {selectedPkg.blastRadiusScore}% Exposure
                </span>
              </div>
              <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                <div
                  style={{ width: `${selectedPkg.blastRadiusScore}%` }}
                  className={`h-full transition-all duration-300 ${
                    selectedPkg.blastRadiusScore > 70 ? 'bg-rose-500' :
                    selectedPkg.blastRadiusScore > 40 ? 'bg-amber-500' : 'bg-emerald-500'
                  }`}
                />
              </div>
            </div>

            {/* Impacted Application Modules */}
            <div className="space-y-1.5">
              <span className="text-xs font-bold text-[var(--text-primary)] font-display flex items-center space-x-1.5">
                <Layers className="w-3.5 h-3.5 text-blue-400" />
                <span>Impacted Upstream Modules ({selectedPkg.impactedModules.length})</span>
              </span>
              <div className="flex flex-wrap gap-1.5">
                {selectedPkg.impactedModules.map((mod) => (
                  <span key={mod} className="px-2 py-0.5 rounded-md cdx-recessed border border-[var(--border-subtle)] font-mono text-[11px] text-[var(--text-secondary)]">
                    {mod}
                  </span>
                ))}
              </div>
            </div>

            {/* CVE Vulnerabilities */}
            {selectedPkg.cves.length > 0 && (
              <div className="space-y-2 pt-1 border-t border-[var(--border-subtle)]">
                <span className="text-xs font-bold text-rose-400 font-display flex items-center space-x-1.5">
                  <AlertTriangle className="w-3.5 h-3.5" />
                  <span>Known Exploits &amp; Violations</span>
                </span>
                <div className="space-y-1.5">
                  {selectedPkg.cves.map((cve) => (
                    <div key={cve.id} className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/25 space-y-1">
                      <div className="flex items-center justify-between text-xs font-mono">
                        <span className="font-bold text-rose-400">{cve.id}</span>
                        <span className="px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-300 text-[10px] font-bold">
                          CVSS {cve.score}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-300 leading-snug">{cve.summary}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Recommended Upgrade Patch */}
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/25 space-y-1">
              <div className="text-[10px] font-mono text-emerald-400 font-bold uppercase">
                Safe Remediation Target
              </div>
              <div className="text-xs font-mono text-emerald-300 font-bold flex items-center space-x-1.5">
                <span>Upgrade to:</span>
                <span className="underline">{selectedPkg.recommendedVersion}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
