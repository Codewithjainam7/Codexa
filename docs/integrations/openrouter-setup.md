# OpenRouter AI Model Configuration & Setup Guide

This guide details the integration, configuration, model selection, rate-limit tuning, and troubleshooting procedures for connecting **Codexa** to OpenRouter's multi-model AI inference gateway.

---

## 1. Overview & Architecture

Codexa leverages OpenRouter as an optional AI remediation layer. When a security finding or architectural defect is detected by the static analysis engine, Codexa can optionally consult frontier LLMs to generate contextual AST refactoring patches, code diffs, and remediation rationale.

```
+-----------------------------------------------------------------------------------+
|                        Codexa Static AST Analysis Engine                          |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                     SecretMaskingSecurityService (CWE-532)                        |
|       - Redacts API keys, credentials, and high Shannon entropy tokens            |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                Content-Addressable Remediation Cache (SHA-256)                    |
|       - Returns cached remediation in < 1ms on duplicate AST pattern              |
+---------------------+-------------------------------------------------------------+
                      | Cache Miss
                      v
+-----------------------------------------------------------------------------------+
|               OpenRouter Gateway Client (https://openrouter.ai/api/v1)            |
|       - Headers: Authorization, HTTP-Referer, X-Title                             |
|       - Cascade: Claude 3.5 Sonnet -> GPT-4o -> DeepSeek Coder -> Llama 3.1 70B   |
+---------------------+-------------------------------------------------------------+
                      | Failure / 429 / Timeout / No Key
                      v
+-----------------------------------------------------------------------------------+
|             Deterministic Offline Template Generator (Zero Network)               |
+-----------------------------------------------------------------------------------+
```

---

## 2. API Key Provisioning & Quickstart

### Step 1: Obtain OpenRouter API Key
1. Sign in to [OpenRouter](https://openrouter.ai/).
2. Navigate to **Account Keys** and generate a new key with prefix `sk-or-v1-...`.
3. Set your account credit limits and usage alerts.

### Step 2: Configure Environment Variables
Set the following environment variable in your deployment environment or `.env` file:

```bash
# Linux / macOS
export OPENROUTER_API_KEY="sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Windows PowerShell
$env:OPENROUTER_API_KEY="sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

---

## 3. Application Configuration (`application.yml`)

Codexa provides granular control over OpenRouter endpoints, models, timeouts, and fallback policies:

```yaml
codexa:
  ai:
    enabled: true
    provider: openrouter
    api-key: ${OPENROUTER_API_KEY:}
    base-url: https://openrouter.ai/api/v1
    primary-model: anthropic/claude-3.5-sonnet
    fallback-models:
      - openai/gpt-4o
      - deepseek/deepseek-coder
      - meta-llama/llama-3.1-70b-instruct
    temperature: 0.1
    max-tokens: 1024
    connect-timeout-ms: 3000
    read-timeout-ms: 12000
    circuit-breaker:
      failure-rate-threshold: 50.0
      wait-duration-in-open-state-ms: 60000
      sliding-window-size: 10
      minimum-number-of-calls: 5
    cache:
      enabled: true
      max-entries: 10000
      ttl-hours: 720
```

---

## 4. Model Cascade Hierarchy & Cost Optimization

Codexa implements automated model cascading to balance patch quality against inference costs:

| Tier | Model Identifier | Primary Specialty | Context Window | Relative Cost |
|:---|:---|:---|:---:|:---:|
| **Tier 1 (Primary)** | `anthropic/claude-3.5-sonnet` | Complex multi-file refactoring, AST transforms | 200k | Standard |
| **Tier 2 (Fallback)** | `openai/gpt-4o` | Enterprise Java/Spring Boot idioms | 128k | Standard |
| **Tier 3 (Budget)** | `deepseek/deepseek-coder` | Specialized code synthesis & AST patch generation | 64k | Low |
| **Tier 4 (Open)** | `meta-llama/llama-3.1-70b-instruct` | Open-weights fallback | 128k | Low |

### Dynamic Model Fallback Payload
When OpenRouter receives multiple models in the request body, it automatically attempts downstream fallbacks if upstream providers return HTTP 429 (rate-limited) or HTTP 503 (overloaded):

```json
{
  "models": [
    "anthropic/claude-3.5-sonnet",
    "openai/gpt-4o",
    "deepseek/deepseek-coder"
  ],
  "route": "fallback",
  "temperature": 0.1,
  "max_tokens": 1024
}
```

---

## 5. Offline & Air-Gapped Mode

In high-security, air-gapped, or regulated environments where outbound internet egress is prohibited, Codexa operates seamlessly without OpenRouter:

```bash
# Explicitly disable external AI calls
export CODEXA_AI_ENABLED=false
```

When disabled:
- The static AST analysis engine functions at 100% full capacity.
- All security vulnerabilities, quality issues, and readiness scores are computed identically.
- Code remediations are generated via the **Deterministic Offline Template Engine**, utilizing parameterized AST rewrite rules without sending a single byte outside the local container.

---

## 6. HTTP Request Headers & Audit Compliance

OpenRouter requests include required attribution headers:

```http
POST /api/v1/chat/completions HTTP/1.1
Host: openrouter.ai
Authorization: Bearer sk-or-v1-...
HTTP-Referer: https://codexa.dev
X-Title: Codexa Code Security Auditor
Content-Type: application/json
User-Agent: Codexa-Backend/1.3.0
```

---

## 7. Troubleshooting & Error Codes

| HTTP Status | Cause | Codexa Behavior |
|:---:|:---|:---|
| **401 Unauthorized** | Missing or invalid `OPENROUTER_API_KEY` | Logs warning once; trips to offline deterministic templates. |
| **402 Payment Required** | OpenRouter account out of credits | Trips circuit breaker to `OPEN`; uses offline templates. |
| **429 Rate Limit Exceeded** | Quota exhausted or burst rate hit | Parses `Retry-After` header; retries once or fails over to secondary model. |
| **503 Service Unavailable** | OpenRouter upstream model outage | Automatically cascades to next model in list; falls back to offline engine. |
