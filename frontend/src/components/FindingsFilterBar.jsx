import React from 'react';
import { Search, Filter } from 'lucide-react';

export default function FindingsFilterBar({
  category, setCategory,
  severity, setSeverity,
  search, setSearch
}) {
  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-3.5 flex flex-col md:flex-row gap-3 items-center justify-between">
      {/* Search Input */}
      <div className="relative w-full md:w-80">
        <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by title, file, rule ID..."
          className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 transition-colors"
        />
      </div>

      {/* Category & Severity Dropdowns */}
      <div className="flex items-center gap-2 w-full md:w-auto">
        <div className="flex items-center space-x-1.5 text-xs text-slate-400">
          <Filter className="w-3.5 h-3.5" />
          <span>Category:</span>
        </div>
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
        >
          <option value="">All Categories</option>
          <option value="SECURITY">Security</option>
          <option value="QUALITY">Quality</option>
          <option value="OPERATIONS">Operations</option>
        </select>

        <div className="flex items-center space-x-1.5 text-xs text-slate-400 pl-2">
          <span>Severity:</span>
        </div>
        <select
          value={severity}
          onChange={(e) => setSeverity(e.target.value)}
          className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
        >
          <option value="">All Severities</option>
          <option value="CRITICAL">Critical</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
      </div>
    </div>
  );
}
