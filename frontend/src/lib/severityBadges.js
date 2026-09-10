/**
 * Returns accessible color classes for severity levels across dark-mode UI.
 */
export function getSeverityBadgeStyle(severity) {
  const norm = (severity || '').toUpperCase();
  switch (norm) {
    case 'CRITICAL':
      return {
        bg: 'bg-rose-500/10',
        text: 'text-rose-400',
        border: 'border-rose-500/20',
        dot: 'bg-rose-400',
        label: 'Critical'
      };
    case 'HIGH':
      return {
        bg: 'bg-amber-500/10',
        text: 'text-amber-400',
        border: 'border-amber-500/20',
        dot: 'bg-amber-400',
        label: 'High'
      };
    case 'MEDIUM':
      return {
        bg: 'bg-blue-500/10',
        text: 'text-blue-400',
        border: 'border-blue-500/20',
        dot: 'bg-blue-400',
        label: 'Medium'
      };
    case 'LOW':
    default:
      return {
        bg: 'bg-slate-500/10',
        text: 'text-slate-400',
        border: 'border-slate-500/20',
        dot: 'bg-slate-400',
        label: 'Low'
      };
  }
}
