const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';

/**
 * Pings server health check with status code verification
 */
export async function checkHealth() {
  const res = await fetch(`${API_BASE}/health`);
  if (!res.ok) throw new Error('Health check failed');
  return res.json();
}

export async function getLimits() {
  const res = await fetch(`${API_BASE}/config/limits`);
  if (!res.ok) throw new Error('Failed to fetch upload limits');
  return res.json();
}

export async function submitZip(file) {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${API_BASE}/analyses/zip`, {
    method: 'POST',
    body: formData,
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || 'Failed to upload ZIP archive');
  }
  return data;
}

export async function submitGitHubUrl(repoUrl) {
  const res = await fetch(`${API_BASE}/analyses/github`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ repoUrl }),
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || 'Failed to submit GitHub repository URL');
  }
  return data;
}

const inFlightJobRequests = new Map();

export async function getAnalysisJob(jobId) {
  if (inFlightJobRequests.has(jobId)) {
    return inFlightJobRequests.get(jobId);
  }

  const requestPromise = (async () => {
    try {
      const res = await fetch(`${API_BASE}/analyses/${jobId}`);
      if (!res.ok) {
        const error = new Error(res.status === 404 ? 'Analysis job not found' : 'Failed to fetch analysis job');
        error.status = res.status;
        throw error;
      }
      return await res.json();
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
  if (!res.ok) throw new Error('Failed to fetch findings');
  return res.json();
}
