/**
 * Resilient clipboard utility that works across secure (HTTPS) and local contexts.
 */
export async function copyToClipboard(text) {
  if (!text) return false;
  try {
    if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch (_) {}

  try {
    if (typeof document !== 'undefined') {
      const el = document.createElement('textarea');
      el.value = text;
      el.setAttribute('readonly', '');
      el.style.position = 'absolute';
      el.style.left = '-9999px';
      document.body.appendChild(el);
      el.select();
      const success = document.execCommand('copy');
      document.body.removeChild(el);
      return success;
    }
  } catch (_) {}

  return false;
}
