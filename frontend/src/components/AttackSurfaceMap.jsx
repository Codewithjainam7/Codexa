import React, { useState, useMemo } from 'react';
import { 
  Shield, Globe, Server, Database, Cloud, AlertTriangle, 
  CheckCircle, ArrowRight, Eye, Layers, Lock, Unlock, Cpu, Zap
} from 'lucide-react';

export default function AttackSurfaceMap({ job, findings = [] }) {
  const [selectedLayerId, setSelectedLayerId] = useState('layer-api');
  const [selectedPinId, setSelectedPinId] = useState(null);

  // Group findings into architectural layers
  const layers = useMemo(() => {
    const layerDefs = [
      {
        id: 'layer-ingress',
        name: '1. Ingress & Perimeter',
        subtitle: 'External Edge, TLS & Webhook Handlers',
        icon: Globe,
        color: 'from-blue-600/20 to-sky-600/20',
        borderColor: 'border-blue-500/40',
        matches: (f) => f.ruleId?.includes('TLS') || f.ruleId?.includes('CORS') || f.ruleId?.includes('IP') || f.title?.toLowerCase().includes('tls')
      },
      {
        id: 'layer-api',
        name: '2. API Gateway & Controllers',
        subtitle: 'RestControllers, Auth Filters & Routing',
        icon: Server,
        color: 'from-purple-600/20 to-indigo-600/20',
        borderColor: 'border-purple-500/40',
        matches: (f) => f.ruleId?.includes('API') || f.ruleId?.includes('ACCESS') || f.ruleId?.includes('CSRF') || f.ruleId?.includes('XSS') || f.category === 'SECURITY' && (f.filePath?.toLowerCase().includes('controller') || f.filePath?.toLowerCase().includes('api'))
      },
      {
        id: 'layer-service',
        name: '3. Application Core & AI Logic',
        subtitle: 'Business Services, LLM Prompts & Deserialization',
        icon: Cpu,
        color: 'from-emerald-600/20 to-teal-600/20',
        borderColor: 'border-emerald-500/40',
        matches: (f) => f.ruleId?.includes('LLM') || f.ruleId?.includes('CMD') || f.ruleId?.includes('DESERIAL') || f.ruleId?.includes('THREAD') || f.filePath?.toLowerCase().includes('service')
      },
      {
        id: 'layer-data',
        name: '4. Data Sinks & Cloud Resources',
        subtitle: 'Postgres DB, Secret Stores & IaC Configs',
        icon: Database,
        color: 'from-amber-600/20 to-orange-600/20',
        borderColor: 'border-amber-500/40',
        matches: (f) => f.ruleId?.includes('SQL') || f.ruleId?.includes('SECRET') || f.ruleId?.includes('IAC') || f.ruleId?.includes('SSRF') || f.filePath?.toLowerCase().includes('repo') || f.filePath?.toLowerCase().includes('docker')
      }
    ];

    return layerDefs.map(layer => {
      const matchedFindings = findings.filter(layer.matches);
      const criticalCount = matchedFindings.filter(f => f.severity === 'CRITICAL').length;
      const highCount = matchedFindings.filter(f => f.severity === 'HIGH').length;

      return {
        ...layer,
        findings: matchedFindings,
        criticalCount,
        highCount,
        hasThreats: matchedFindings.length > 0
      };
    });
  }, [findings]);

  const activeLayer = useMemo(() => {
    return layers.find(l => l.id === selectedLayerId) || layers[0];
  }, [layers, selectedLayerId]);

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-2">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <Shield className="w-4 h-4 text-purple-400" />
              <h3 className="text-sm sm:text-base font-bold text-[var(--text-primary)] font-display">
                Attack Surface Threat Map &amp; Architectural Perimeter
              </h3>
            </div>
            <p className="text-xs text-[var(--text-secondary)]">
              Visualizes ingress flows from external clients down to persistence layers, overlaying active AST vulnerability pins.
            </p>
          </div>
          <span className="px-3 py-1 rounded-full text-[11px] font-mono font-bold bg-purple-500/15 text-purple-400 border border-purple-500/30">
            Threat Modeling Mode
          </span>
        </div>
      </div>

      {/* 4 Architectural Tier Pipeline */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 relative">
        {layers.map((layer, idx) => {
          const Icon = layer.icon;
          const isSelected = selectedLayerId === layer.id;

          return (
            <div
              key={layer.id}
              onClick={() => setSelectedLayerId(layer.id)}
              className={`p-4 rounded-2xl cdx-recessed border transition-all cursor-pointer relative overflow-hidden group ${
                isSelected
                  ? `${layer.borderColor} ring-2 ring-blue-500/50 scale-[1.02] shadow-xl`
                  : 'border-[var(--border-subtle)] hover:scale-[1.01]'
              }`}
            >
              {/* Layer Header */}
              <div className="flex items-center justify-between mb-3">
                <div className="w-9 h-9 rounded-xl bg-slate-800/80 border border-slate-700 flex items-center justify-center">
                  <Icon className="w-4 h-4 text-slate-200" />
                </div>
                <div className="flex items-center space-x-1">
                  {layer.criticalCount > 0 && (
                    <span className="px-1.5 py-0.5 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30 text-[10px] font-mono font-bold">
                      {layer.criticalCount} Crit
                    </span>
                  )}
                  {layer.highCount > 0 && (
                    <span className="px-1.5 py-0.5 rounded-full bg-orange-500/20 text-orange-400 border border-orange-500/30 text-[10px] font-mono font-bold">
                      {layer.highCount} High
                    </span>
                  )}
                  {layer.findings.length === 0 && (
                    <span className="px-1.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 text-[10px] font-mono font-bold">
                      Guarded
                    </span>
                  )}
                </div>
              </div>

              <div className="text-xs font-bold font-display text-[var(--text-primary)] group-hover:text-blue-400 transition-colors">
                {layer.name}
              </div>
              <div className="text-[11px] text-[var(--text-muted)] line-clamp-1 mt-0.5">
                {layer.subtitle}
              </div>

              {/* Threat Pin count badge */}
              <div className="mt-4 pt-2 border-t border-[var(--border-subtle)] flex items-center justify-between text-[10px] font-mono text-[var(--text-muted)]">
                <span>{layer.findings.length} Threat Pin(s)</span>
                <span className="text-blue-400 flex items-center space-x-0.5">
                  <span>Inspect</span>
                  <ArrowRight className="w-3 h-3" />
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Layer Detail & Threat Pin Inspector */}
      <div className="p-5 rounded-2xl cdx-card border border-[var(--border-subtle)] space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-[var(--border-subtle)] gap-2">
          <div className="space-y-0.5">
            <span className="text-[10px] font-mono uppercase font-bold text-blue-400">
              Active Tier Focus
            </span>
            <h4 className="text-sm sm:text-base font-bold font-display text-[var(--text-primary)]">
              {activeLayer.name} &bull; Vulnerability Matrix
            </h4>
          </div>
          <span className="text-xs font-mono text-[var(--text-muted)]">
            Total Violations in Tier: <strong>{activeLayer.findings.length}</strong>
          </span>
        </div>

        {activeLayer.findings.length === 0 ? (
          <div className="py-8 text-center space-y-2">
            <CheckCircle className="w-10 h-10 text-emerald-500 mx-auto" />
            <div className="text-xs font-bold font-display text-[var(--text-primary)]">
              Zero Vulnerabilities Detected in {activeLayer.name}
            </div>
            <p className="text-[11px] text-[var(--text-secondary)] max-w-sm mx-auto">
              Perimeter security, authentication configurations, and cryptographic controls at this architectural tier satisfy production baselines.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {activeLayer.findings.map((f, i) => (
              <div
                key={f.id || i}
                className="p-3.5 rounded-xl cdx-recessed border border-[var(--border-subtle)] hover:border-blue-500/40 transition-all space-y-2"
              >
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center space-x-1.5">
                    <span className={`px-1.5 py-0.5 rounded text-[10px] font-mono font-bold uppercase ${
                      f.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30' :
                      f.severity === 'HIGH' ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30' :
                      'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                    }`}>
                      {f.severity}
                    </span>
                    <span className="font-mono text-blue-400 font-bold">{f.ruleId}</span>
                  </div>
                  <span className="text-[10px] font-mono text-[var(--text-muted)] truncate max-w-[140px]">
                    {f.filePath?.split('/').pop()}:{f.startLine}
                  </span>
                </div>

                <div className="text-xs font-bold text-[var(--text-primary)] font-display">
                  {f.title}
                </div>

                <p className="text-[11px] text-[var(--text-secondary)] leading-relaxed line-clamp-2">
                  {f.message}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
