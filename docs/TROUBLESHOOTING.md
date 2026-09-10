# Codexa Troubleshooting & Operations Guide

## Common Issues

### 1. Browser Loads Stale JS Bundle
- **Symptom:** `ReferenceError: useCallback is not defined`
- **Root Cause:** Browser disk cache or stale backend static resource cache.
- **Fix:** Press `Ctrl + Shift + R` (Windows/Linux) or `Cmd + Shift + R` (macOS) to force browser cache bypass.

### 2. Large Repository OutOfMemoryError
- **Fix:** Increase JVM heap space: `java -Xmx4g -jar codexa-backend.jar`.

### 3. Port In Use (8080 or 5173)
- **Backend:** `SERVER_PORT=8081 java -jar codexa-backend.jar`
- **Frontend:** Set `VITE_PORT=5174` in `frontend/.env`.
