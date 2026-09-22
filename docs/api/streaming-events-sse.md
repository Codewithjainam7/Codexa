# Codexa Real-Time Server-Sent Events (SSE) Streaming Guide

This document specifies the Server-Sent Events (SSE) real-time streaming endpoint, allowing web and CLI clients to receive instant progress notifications without polling HTTP endpoints.

---

## 1. Endpoint Overview

- **Endpoint:** `GET /api/v1/analyses/{jobId}/events`
- **Accept:** `text/event-stream`
- **Connection:** `keep-alive`

Establishes an HTTP persistent streaming connection. The server pushes JSON progress frames as the analysis pipeline transitions through stages.

---

## 2. Event Types & Lifecycle

| Event Name | Stage | Progress | Description |
| :--- | :--- | :---: | :--- |
| `STAGE_TRANSITION` | `EXTRACTING` | 5% | Repository archive downloaded and queued for decompression. |
| `STAGE_TRANSITION` | `PARSING_AST` | 25% | ForkJoinPool initializing AST compilation units. |
| `FILE_ANALYZED` | `SCANNING` | 25–85% | Emitted per batch of files scanned with finding counter. |
| `DIAGNOSTICS_READY`| `SCORING` | 90% | Complexity and API route mappings compiled. |
| `ANALYSIS_COMPLETE`| `COMPLETED` | 100% | Final readiness score and verdict calculated. |
| `ANALYSIS_FAILED` | `FAILED` | 100% | Unhandled ingestion error or format violation. |

---

## 3. Sample SSE Wire Format

```http
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

event: STAGE_TRANSITION
data: {"jobId":"a8eeeb48-e81d-4d18-a587-8288d2277d47","stage":"SECURITY_AND_QUALITY_RULES","progress":65}

event: FILE_ANALYZED
data: {"jobId":"a8eeeb48-e81d-4d18-a587-8288d2277d47","file":"src/main/java/UserService.java","findingsCount":2}

event: ANALYSIS_COMPLETE
data: {"jobId":"a8eeeb48-e81d-4d18-a587-8288d2277d47","overallScore":100.0,"verdict":"REVIEW_COMPLETE","durationMs":18500}
```

---

## 4. JavaScript Client Integration

```javascript
const eventSource = new EventSource('/api/v1/analyses/a8eeeb48-e81d-4d18-a587-8288d2277d47/events');

eventSource.addEventListener('STAGE_TRANSITION', (e) => {
  const data = JSON.parse(e.data);
  console.log(`Pipeline stage: ${data.stage} (${data.progress}%)`);
});

eventSource.addEventListener('ANALYSIS_COMPLETE', (e) => {
  const data = JSON.parse(e.data);
  console.log(`Analysis complete! Score: ${data.overallScore}/100, Verdict: ${data.verdict}`);
  eventSource.close();
});
```
