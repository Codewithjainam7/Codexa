# Codexa Structured Audit Logging (RFC 5424)

## Overview
All security-relevant events within Codexa (authentications, rule modifications, analysis submissions, report exports) are logged as immutable structured JSON objects.

## Log Schema Example

```json
{
  "@timestamp": "2026-09-15T18:00:00.123Z",
  "log.level": "INFO",
  "event.category": "iam",
  "event.action": "user.token_issued",
  "user.id": "usr_882a",
  "client.ip": "192.168.1.100",
  "http.request.method": "POST",
  "url.path": "/api/v1/auth/token",
  "security.outcome": "SUCCESS",
  "service.name": "codexa-backend",
  "service.version": "1.0.0"
}
```

## Anti-Tamper Measures
- Logs are streamed asynchronously via Syslog / Fluentbit to write-once-read-many (WORM) cloud object storage.
- File system log buffers maintain HMAC checksums per chunk to detect local file manipulation.
