# OpenRouter Multi-LLM Resilient Fallback Routing Specification

## 1. Overview
When optional AI-enhanced code review is enabled, Codexa routes requests through **OpenRouter** with automatic multi-model failover to guarantee high availability and low latency.

---

## 2. Model Hierarchy & Fallback Chain

```
[Primary Model: Claude 3.5 Sonnet]
              │ (Timeout > 15s or HTTP 429)
              ▼
[Secondary Model: GPT-4o / GPT-4o-mini]
              │ (Error or Rate Limit)
              ▼
[Tertiary Model: DeepSeek V2.5 / Mistral Large]
              │ (Exhausted)
              ▼
[Local Deterministic AST Engine Only] (Zero AI Dependency)
```

---

## 3. Fault-Tolerant Circuit Breaker
Codexa implements a sliding-window circuit breaker via Resilience4j. If OpenRouter error rates exceed 30% over 20 consecutive invocations, the circuit opens for 60 seconds, falling back purely to deterministic AST analysis.
