/**
 * Compact number formatting for metric counters (1.2k, 4.5M).
 */
export function formatCompactNumber(num) {
  if (num === null || num === undefined || isNaN(num)) return '0';
  const n = Number(num);
  if (n < 1000) return String(n);
  if (n < 1_000_000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M';
}

export function clamp(val, min, max) {
  return Math.min(Math.max(val, min), max);
}
