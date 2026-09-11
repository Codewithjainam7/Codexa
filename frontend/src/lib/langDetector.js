/**
 * Syntax language detector based on file path extension.
 */
const EXTENSION_MAP = {
  java: 'java',
  js: 'javascript',
  jsx: 'jsx',
  ts: 'typescript',
  tsx: 'tsx',
  py: 'python',
  go: 'go',
  rs: 'rust',
  json: 'json',
  xml: 'xml',
  yml: 'yaml',
  yaml: 'yaml',
  sql: 'sql',
  sh: 'bash',
  dockerfile: 'dockerfile',
};

export function detectLanguageFromPath(filePath = '') {
  if (!filePath) return 'plaintext';
  const clean = filePath.split('?')[0].split('#')[0];
  const parts = clean.split('.');
  if (parts.length <= 1) return 'plaintext';
  const ext = parts.pop().toLowerCase();
  return EXTENSION_MAP[ext] || ext;
}
