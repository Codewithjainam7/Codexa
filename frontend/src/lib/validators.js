/**
 * Validates GitHub repository URLs.
 */
export function isValidGitHubUrl(url) {
  if (!url || typeof url !== 'string') return false;
  const githubRegex = /^https:\/\/github\.com\/[a-zA-Z0-9_.-]+\/[a-zA-Z0-9_.-]+(\/?|\.git)?$/;
  return githubRegex.test(url.trim());
}

/**
 * Validates ZIP archive file extensions and size constraints.
 */
export function isValidZipFile(file, maxBytes = 3221225472) { // 3GB default max
  if (!file) return { valid: false, reason: 'No file provided' };
  const isZip = file.name.endsWith('.zip') || file.type === 'application/zip' || file.type === 'application/x-zip-compressed';
  if (!isZip) return { valid: false, reason: 'File must be a .zip archive' };
  if (file.size > maxBytes) return { valid: false, reason: 'Archive exceeds 3GB upload limit' };
  return { valid: true };
}
