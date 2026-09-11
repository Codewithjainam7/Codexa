/**
 * Resilient localStorage wrapper with in-memory fallback for SSR/private browsing.
 */
const memoryStore = new Map();

export const safeStorage = {
  getItem(key) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        return window.localStorage.getItem(key);
      }
    } catch (_) {}
    return memoryStore.get(key) || null;
  },

  setItem(key, val) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        window.localStorage.setItem(key, val);
        return;
      }
    } catch (_) {}
    memoryStore.set(key, String(val));
  },

  removeItem(key) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        window.localStorage.removeItem(key);
      }
    } catch (_) {}
    memoryStore.delete(key);
  }
};
