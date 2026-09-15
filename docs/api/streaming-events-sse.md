# Codexa Server-Sent Events (SSE) Streaming API

## Overview
Codexa provides real-time progress updates for running analysis jobs via Server-Sent Events (SSE) pursuant to the W3C EventSource standard.

## Connection Endpoint
```http
GET /api/v1/analysis/{jobId}/events HTTP/1.1
Accept: text/event-stream
Authorization: Bearer <TOKEN>
```

## Event Types

| Event Name | Description | Payload Structure |
| :--- | :--- | :--- |
| `stage_changed` | Analysis pipeline transitioned to a new stage | `{"stage": "AST_PARSING", "progressPercent": 25}` |
| `finding_detected` | A rule violation was flagged in real time | `{"ruleId": "CR-SEC-001", "file": "Auth.java", "line": 42}` |
| `metrics_updated` | Cyclomatic complexity and lines-of-code updated | `{"loc": 14200, "complexity": 18.4}` |
| `job_completed` | All pipeline stages successfully concluded | `{"qualityScore": 94, "verdict": "PASSED"}` |
| `job_failed` | Unrecoverable error occurred | `{"errorCode": "ARCHIVE_CORRUPTED", "reason": "..."}` |

## Wire Protocol Example
```text
event: stage_changed
data: {"jobId":"job-102","stage":"TOKENIZING","progressPercent":15,"timestamp":"2026-09-15T17:52:00Z"}

event: finding_detected
data: {"jobId":"job-102","ruleId":"CR-SEC-002","severity":"HIGH","message":"Hardcoded API secret found"}

event: job_completed
data: {"jobId":"job-102","qualityScore":88,"totalViolations":3,"executionTimeMs":1420}
```

## Client Reconnection Behavior
If the TCP connection drops, standard `EventSource` clients will automatically reconnect. Codexa transmits an `id:` field per event, allowing resuming via the `Last-Event-ID` header.
