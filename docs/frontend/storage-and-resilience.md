# Frontend Storage & Client Resilience Architecture

In production web applications, interacting directly with `window.localStorage` or `window.sessionStorage` without defensive abstraction poses significant reliability risks in private browsing modes and embedded iframes.

---

## 1. Browser Storage Failure Modes

1. **Private Browsing Mode (Safari / Firefox / Chrome Incognito)**:
   Accessing `window.localStorage.setItem()` frequently throws a DOM `QuotaExceededError` or `SecurityError` when third-party cookies or persistent storage is disabled.
2. **Server-Side Rendering (SSR) & Static Prerendering**:
   `window` is `undefined` during static build pipelines (e.g. Vite prerender / Next.js), causing uncaught reference exceptions.
3. **Cross-Origin Iframes**:
   Browsers block access to storage partitions within sandboxed iframes.

---

## 2. The `safeStorage` In-Memory Fallback Architecture

To guarantee zero unhandled client-side exceptions, Codexa routes all persistence through `frontend/src/lib/storageUtils.js`:

```javascript
/**
 * Resilient localStorage wrapper with in-memory fallback for SSR and private browsing.
 */
const memoryStore = new Map();

export const safeStorage = {
  getItem(key) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        return window.localStorage.getItem(key);
      }
    } catch (_) {
      // Handled intentionally: fallback to memory map
    }
    return memoryStore.get(key) || null;
  },

  setItem(key, val) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        window.localStorage.setItem(key, val);
        return;
      }
    } catch (_) {
      // Handled intentionally: fallback to memory map
    }
    memoryStore.set(key, String(val));
  },

  removeItem(key) {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        window.localStorage.removeItem(key);
      }
    } catch (_) {
      // Handled intentionally: fallback to memory map
    }
    memoryStore.delete(key);
  }
};
```

---

## 3. Resilience Guarantees

- **Transparent Fallback**: If persistent storage is unavailable, the application degrades gracefully to in-memory state without crashing UI components.
- **Session Continuity**: Active job IDs and user preferences are retained for the lifetime of the browsing tab even in restricted incognito windows.
- **Scanner Cleanliness**: The intentional `catch (_) {}` syntax complies with modern JavaScript optional catch conventions and is recognized by Codexa static analysis rules as deliberate error suppression.
