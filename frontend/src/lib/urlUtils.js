/**
 * URL query string parser and serializer helper.
 */
export function parseQueryParams(searchString) {
  const params = {};
  if (!searchString) return params;
  const query = searchString.startsWith('?') ? searchString.slice(1) : searchString;
  query.split('&').forEach(pair => {
    const [k, v] = pair.split('=');
    if (k) params[decodeURIComponent(k)] = decodeURIComponent(v || '');
  });
  return params;
}

export function buildQueryString(params = {}) {
  const parts = Object.entries(params)
    .filter(([_, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`);
  return parts.length > 0 ? `?${parts.join('&')}` : '';
}
