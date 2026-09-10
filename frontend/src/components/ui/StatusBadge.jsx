import React from 'react';
import { getSeverityBadgeStyle } from '../../lib/severityBadges';

export default function StatusBadge({ severity, count, className = '' }) {
  const style = getSeverityBadgeStyle(severity);
  return (
    <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-mono font-medium border ${style.bg} ${style.text} ${style.border} ${className}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${style.dot}`} aria-hidden="true" />
      <span>{style.label}</span>
      {count !== undefined && <span className="opacity-75 font-bold">({count})</span>}
    </span>
  );
}
