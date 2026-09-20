# Common Troubleshooting Guide & Operational FAQ

This guide provides diagnostic steps and resolutions for common issues encountered when deploying, ingesting repositories, or running static analysis scans with Codexa.

---

## 1. Ingestion & Archive Errors

### 1.1 `413 Payload Too Large` / `ARCHIVE_FILE_COUNT_EXCEEDED`
- **Symptom**: Client receives HTTP 413 error when uploading a repository ZIP archive.
- **Root Cause**: The archive contains more than 50,000 files or exceeds the 3.0 GB decompressed size limit, typically due to including build directories or package managers.
- **Resolution**: Exclude non-source folders prior to compression:
  ```bash
  zip -r codebase.zip . -x "*.git*" "*node_modules*" "*target*" "*build*" "*dist*" "*.venv*"
  ```

### 1.2 `400 Bad Request` / `INVALID_GITHUB_URL`
- **Symptom**: Submitting a GitHub URL fails with `Must be a valid public GitHub HTTPS repository URL`.
- **Root Cause**: The URL format is not public HTTPS or contains query parameters, branch references, or credentials.
- **Resolution**: Use the canonical repository URL:
  - Correct: `https://github.com/Codewithjainam7/Codexa`
  - Incorrect: `git@github.com:...` or `https://github.com/owner/repo/tree/main`

---

## 2. Server & Runtime Issues

### 2.1 502 Bad Gateway / Cold Starts on Free PaaS (Render)
- **Symptom**: The browser displays a 502 Bad Gateway or 30-second delay on first load.
- **Root Cause**: Free tier instances spin down after 15 minutes of inactivity. The Spring Boot backend takes ~15–20 seconds to boot on resource-constrained containers.
- **Resolution**: Wait 30 seconds and refresh. For automated uptime, configure an external keep-alive ping to `/actuator/health` every 10 minutes.

### 2.2 `429 Too Many Requests` / `RATE_LIMIT_EXCEEDED`
- **Symptom**: Automated scripts or CI workflows receive HTTP 429 errors during status polling.
- **Root Cause**: Client exceeded the sliding-window token bucket quota (e.g. polling every 50ms).
- **Resolution**: Check the `Retry-After` response header and poll at reasonable intervals (2–3 seconds).

### 2.3 `OutOfMemoryError` on Massive Repositories
- **Symptom**: Scanner crashes with `java.lang.OutOfMemoryError: Java heap space` during AST traversal.
- **Root Cause**: Heap memory exhausted when parsing thousands of large Java AST compilation units simultaneously.
- **Resolution**: Increase container heap allocation and set G1GC flags:
  ```bash
  export JAVA_OPTS="-Xmx3g -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
  ```

### 2.4 Staging Directory Permission Denied
- **Symptom**: Exception logged: `AccessDeniedException: .staging/...`
- **Root Cause**: The non-root container user (`codexa`) lacks write permissions to the configured staging directory.
- **Resolution**: Ensure the host mount has ownership set to UID/GID 1000:
  ```bash
  chown -R 1000:1000 /var/run/codexa/staging
  ```
