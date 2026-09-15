# Codexa AI Provider Resilience & Circuit Breakers

## Overview
Codexa supports optional AI-assisted code review via OpenRouter, Claude, or GPT models. Because external cloud APIs are subject to latency spikes and outages, Codexa ensures total system reliability through automated circuit breakers.

## Circuit Breaker States

```
[ CLOSED: Normal Operation ]
      │ (Failure rate > 50% over 20 requests)
      ▼
[ OPEN: Instant Fallback to Offline Rules ]
      │ (Wait duration: 60s)
      ▼
[ HALF-OPEN: Canary Probes (3 trial requests) ]
      ├── (All succeeded) ──> [ CLOSED ]
      └── (Any failed)    ──> [ OPEN ]
```

## Graceful Degradation Guarantees
1. **Never Fail the Build**: If the AI model endpoint times out or returns HTTP 5xx, Codexa transparently completes the review using offline AST heuristic rules.
2. **Quality Score Transparency**: The final report clearly marks whether AI synthesis was `ACTIVE` or `DEGRADED_FALLBACK`.
