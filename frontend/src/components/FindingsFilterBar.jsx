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
    <div className="cdx-glass-card rounded-2xl p-3 sm:p-4 flex flex-col md:flex-row gap-3 sm:gap-4 items-stretch md:items-center justify-between shadow-xl relative z-30">
      {/* Search Input */}
      <div className="relative w-full md:w-72 lg:w-80 flex-1">
        <Search className="w-4 h-4 text-[var(--text-muted)] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by title, file, rule ID..."
          className="w-full pl-10 pr-3.5 py-2 sm:py-2.5 bg-[var(--bg-recessed)] border border-[var(--border-subtle)] rounded-xl text-xs text-[var(--text-primary)] placeholder-[var(--text-muted)] focus:outline-none focus:border-[var(--accent-primary)] focus:ring-2 focus:ring-[var(--accent-primary)]/30 transition-all font-medium"
        />
      </div>

      {/* Category & Severity Custom Dropdowns (Clean 2-column grid on mobile, inline flex on desktop) */}
      <div className="grid grid-cols-2 gap-2 sm:flex sm:items-center sm:gap-3 w-full md:w-auto shrink-0">
        <CustomSelect
          label="Category"
          value={category}
          onChange={setCategory}
          options={categoryOptions}
          icon={Filter}
          align="left"
        />

        <CustomSelect
          label="Severity"
          value={severity}
          onChange={setSeverity}
          options={severityOptions}
          icon={AlertTriangle}
          align="right"
        />
      </div>
    </div>
  );
}
