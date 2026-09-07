# Common Troubleshooting Guide

Resolutions for OpenRouter rate limits, high file count repos, and staging permission issues.


### 502 Bad Gateway and Cold Starts on Free PaaS (Render)

- **Cause**: Free tier containers spin down after 15 minutes of inactivity.
- **Symptom**: 502 Bad Gateway while container boots up (approx 30-45 seconds).
- **Resolution**: Refresh the page after 30 seconds once the JVM completes its startup routine.
