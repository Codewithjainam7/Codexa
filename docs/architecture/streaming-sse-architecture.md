# Server-Sent Events (SSE) Live Review Streaming Engine

## 1. Overview
To deliver real-time progress updates during codebase ingestion and AST parsing, Codexa utilizes **Server-Sent Events (SSE)** via Spring MVC's `SseEmitter`.

---

## 2. Event Stream Lifecycle

```
Client GET /api/v1/analyses/{jobId}/events
                  │
                  ▼
         [Connection Established]
                  │
                  ├──> event: STAGE_TRANSITION { stage: "AST_PARSING", progress: 45 }
                  ├──> event: FINDING_DETECTED { ruleId: "CR-SEC-001", file: "User.java" }
                  ├──> event: METRICS_UPDATE   { totalFiles: 142, scanDurationMs: 820 }
                  └──> event: JOB_COMPLETED    { verdict: "REVIEW_COMPLETE", score: 94 }
```

---

## 3. Heartbeat & Automatic Reconnection
* Server sends a comment ping (`:heartbeat\n\n`) every 15 seconds to prevent intermediate proxy timeout.
* The frontend EventSource automatically reconnects on transient disconnects with exponential backoff.
