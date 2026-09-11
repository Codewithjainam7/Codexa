/**
 * Truncates text with optional word boundary preservation.
 * @param {string} str Input string
 * @param {number} maxLen Maximum character length
 * @param {boolean} [preserveWords=true] Whether to break only at word boundaries
 * @returns {string} Truncated string
 */
export function truncate(str, maxLen = 80, preserveWords = true) {
  if (!str || str.length <= maxLen) return str || '';
  if (!preserveWords) return str.slice(0, maxLen) + '...';
  const truncated = str.slice(0, maxLen);
  const lastSpace = truncated.lastIndexOf(' ');
  return (lastSpace > 0 ? truncated.slice(0, lastSpace) : truncated) + '...';
}

export function capitalize(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}
