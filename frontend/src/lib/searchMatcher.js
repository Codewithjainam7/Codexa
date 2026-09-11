/**
 * Multi-token fuzzy-like search matcher for findings and files.
 */
export function matchSearchQuery(text, query) {
  if (!query || !query.trim()) return true;
  if (!text) return false;

  const target = text.toLowerCase();
  const tokens = query.toLowerCase().trim().split(/\\s+/);
  return tokens.every(token => target.includes(token));
}
