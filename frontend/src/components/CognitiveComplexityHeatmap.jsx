import React, { useState } from 'react';
import { Brain, Layers, AlertCircle, ArrowUpRight, Flame } from 'lucide-react';

export default function CognitiveComplexityHeatmap({ files = [] }) {
  const [selectedFile, setSelectedFile] = useState(files[0] || null);

  const sampleData = files.length > 0 ? files : [
    { name: 'OrderProcessingService.java', cognitive: 28, cyclomatic: 18, methods: 12, loc: 340 },
    { name: 'AuthenticationFilter.java', cognitive: 19, cyclomatic: 14, methods: 8, loc: 210 },
    { name: 'StripePaymentGateway.java', cognitive: 14, cyclomatic: 11, methods: 6, loc: 180 },
    { name: 'UserProfileController.java', cognitive: 7, cyclomatic: 8, methods: 14, loc: 160 },
    { name: 'DatabaseConnectionPool.java', cognitive: 4, cyclomatic: 5, methods: 5, loc: 95 }
  ];

  const getHeatmapColor = (score) => {
    if (score >= 20) return 'bg-rose-500/20 text-rose-400 border-rose-500/40';
    if (score >= 12) return 'bg-amber-500/20 text-amber-400 border-amber-500/40';
    return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40';
  };

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
            <Brain className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-semibold text-white">Cognitive Complexity & Maintainability Heatmap</h3>
            <p className="text-xs text-slate-400">Measures human mental cognitive load (SonarQube specification)</p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs">
          <span className="flex items-center gap-1 text-emerald-400"><span className="w-2 h-2 rounded-full bg-emerald-400"></span>Low (&lt;12)</span>
          <span className="flex items-center gap-1 text-amber-400"><span className="w-2 h-2 rounded-full bg-amber-400"></span>Moderate (12-19)</span>
          <span className="flex items-center gap-1 text-rose-400"><span className="w-2 h-2 rounded-full bg-rose-400"></span>High (&ge;20)</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {sampleData.map((f, i) => (
          <div
            key={i}
            onClick={() => setSelectedFile(f)}
            className={`cursor-pointer p-4 rounded-xl border transition-all ${
              selectedFile?.name === f.name ? 'border-purple-500 bg-purple-500/5' : 'border-slate-800 bg-slate-950/40 hover:border-slate-700'
            }`}
          >
            <div className="flex items-start justify-between mb-3">
              <span className="text-sm font-medium text-slate-200 truncate pr-2">{f.name}</span>
              <span className={`px-2 py-0.5 text-xs font-semibold rounded-md border ${getHeatmapColor(f.cognitive)}`}>
                {f.cognitive} CogC
              </span>
            </div>
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span>Cyclomatic: {f.cyclomatic} CC</span>
              <span>{f.methods} methods</span>
              <span>{f.loc} LOC</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
