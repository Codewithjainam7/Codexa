import React from 'react';
import { Search, Filter, ShieldCheck, AlertTriangle } from 'lucide-react';
import CustomSelect from './CustomSelect';

export default function FindingsFilterBar({
  category, setCategory,
  severity, setSeverity,
  search, setSearch
}) {
  const categoryOptions = [
    { value: '', label: 'All Categories' },
    { value: 'SECURITY', label: 'Security' },
    { value: 'QUALITY', label: 'Code Quality' },
    { value: 'OPERATIONS', label: 'Operations' }
  ];

  const severityOptions = [
    { value: '', label: 'All Severities' },
    { value: 'CRITICAL', label: 'Critical Severity', dotColor: 'bg-rose-500' },
    { value: 'HIGH', label: 'High Severity', dotColor: 'bg-orange-500' },
    { value: 'MEDIUM', label: 'Medium Severity', dotColor: 'bg-amber-400' },
    { value: 'LOW', label: 'Low Severity', dotColor: 'bg-blue-400' }
  ];

  return (
    <div className="bg-slate-900/80 border border-slate-800/90 rounded-2xl p-4 flex flex-col md:flex-row gap-4 items-center justify-between shadow-lg backdrop-blur-md">
      {/* Search Input */}
      <div className="relative w-full md:w-80">
        <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by title, file, rule ID..."
          className="w-full pl-10 pr-3.5 py-2 bg-slate-950/90 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500/60 focus:ring-1 focus:ring-cyan-500/30 transition-all font-medium"
        />
      </div>

      {/* Category & Severity Custom Dropdowns */}
      <div className="flex flex-wrap items-center gap-3 w-full md:w-auto justify-end">
        <CustomSelect
          label="Category"
          value={category}
          onChange={setCategory}
          options={categoryOptions}
          icon={Filter}
        />

        <CustomSelect
          label="Severity"
          value={severity}
          onChange={setSeverity}
          options={severityOptions}
          icon={AlertTriangle}
        />
      </div>
    </div>
  );
}
