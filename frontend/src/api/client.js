const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export async function fetchWithTimeout(url, options = {}, timeoutMs = 15000) {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(url, { ...options, signal: controller.signal });
  } catch (err) {
    if (err.name === 'AbortError') {
      const timeoutError = new Error(`Request timed out after ${timeoutMs}ms`);
      timeoutError.isTimeout = true;
      throw timeoutError;
    }
    throw err;
  } finally {
    clearTimeout(id);
  }
}

/**
 * Pings server health check with status code verification
 */
export async function checkHealth() {
  const res = await fetchWithTimeout(`${API_BASE}/health`, {}, 5000);
  return await parseJsonResponse(res, 'Health check failed');
}

async function parseJsonResponse(res, fallbackMessage) {
  const contentType = res.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');

  if (isJson) {
    let data;
    try {
      data = await res.json();
    } catch {
      throw new Error(`Invalid JSON response from server (HTTP ${res.status}).`);
    }

    if (!res.ok) {
      throw new Error(data.message || data.error || `${fallbackMessage} (HTTP ${res.status})`);
    }
    return data;
  }

  // Handle non-JSON responses (e.g. 502 Bad Gateway HTML from Render, 504 Gateway Timeout, 404 HTML)
  if (!res.ok) {
    if (res.status === 502 || res.status === 503) {
      throw new Error('Backend service is starting up or temporarily unavailable (HTTP 502/503 Bad Gateway). Please wait 30 seconds and retry.');
    }
    if (res.status === 504) {
      throw new Error('Gateway timed out waiting for backend response (HTTP 504). Please retry.');
    }
    if (res.status === 404) {
      const err = new Error('Requested resource or endpoint not found (HTTP 404).');
      err.status = 404;
      throw err;
    }
    throw new Error(`Server returned non-JSON error (HTTP ${res.status} ${res.statusText || ''}).`);
  }

  throw new Error(fallbackMessage);
}

export async function getLimits() {
  const res = await fetchWithTimeout(`${API_BASE}/config/limits`, {}, 8000);
  return await parseJsonResponse(res, 'Failed to fetch upload limits');
}

export async function submitZip(file) {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${API_BASE}/analyses/zip`, {
    method: 'POST',
    body: formData,
  });

  return await parseJsonResponse(res, 'Failed to upload ZIP archive');
}

export async function submitGitHubUrl(repoUrl) {
  const res = await fetch(`${API_BASE}/analyses/github`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ repoUrl }),
  });

  return await parseJsonResponse(res, 'Failed to submit GitHub repository URL');
}

const inFlightJobRequests = new Map();

export async function getAnalysisJob(jobId) {
  if (inFlightJobRequests.has(jobId)) {
    return inFlightJobRequests.get(jobId);
  }

  const requestPromise = (async () => {
    try {
      const res = await fetch(`${API_BASE}/analyses/${jobId}`);
      return await parseJsonResponse(res, 'Failed to fetch analysis job');
    } finally {
      inFlightJobRequests.delete(jobId);
    }
  })();

  inFlightJobRequests.set(jobId, requestPromise);
  return requestPromise;
}

export async function getFindings(jobId, params = {}) {
  const url = new URL(`${window.location.origin}${API_BASE}/analyses/${jobId}/findings`);
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      url.searchParams.append(key, params[key]);
    }
  });

  const res = await fetch(url.toString());
  return await parseJsonResponse(res, 'Failed to fetch findings');
}


/**
 * Calculates exponential backoff with full jitter to avoid thundering herd.
 */
export function calculateBackoffJitter(attempt, baseMs = 300, maxMs = 4000) {
  const exp = Math.min(maxMs, baseMs * Math.pow(2, attempt));
  return Math.floor(Math.random() * exp);
}
