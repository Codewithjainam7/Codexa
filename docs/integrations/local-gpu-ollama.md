# Local GPU AI Remediation with Ollama & NVIDIA RTX

This guide provides setup instructions for running **100% local, offline, zero-cost AI code explanations and remediation fixes** in **Codexa** using **Ollama** and an NVIDIA RTX GPU (such as an RTX 3050, RTX 3060, or RTX 40-series).

---

## 1. Why Local GPU Inference?

| Dimension | Cloud AI (OpenRouter / OpenAI) | Local GPU (Ollama / RTX 3050) |
|:---|:---|:---|
| **Privacy & Security** | Source code transmitted over HTTPS | **Zero egress**; code never leaves the workstation |
| **Cost** | Per-token API subscription fees | **$0.00 / Free unlimited queries** |
| **Air-Gapped Operation** | Requires active internet connection | **100% Offline operation** |
| **Latency** | 800ms – 2,500ms network round-trip | **150ms – 600ms** local tensor generation |

---

## 2. Hardware Suitability (NVIDIA RTX 3050 Specs)

The NVIDIA GeForce RTX 3050 Laptop GPU (4GB / 6GB GDDR6 VRAM) is an optimal edge accelerator for quantized 7B parameter coding models:

| Recommended Model | Parameter Count | Quantization | VRAM Footprint | Generation Speed |
|:---|:---:|:---:|:---:|:---:|
| **Qwen 2.5 Coder** (`qwen2.5-coder:7b`) | 7.6B | Q4_K_M | **~4.4 GB** | 35 – 45 tok/sec |
| **DeepSeek Coder** (`deepseek-coder:6.7b`) | 6.7B | Q4_K_M | **~4.1 GB** | 40 – 50 tok/sec |
| **CodeLlama** (`codellama:7b-instruct`) | 7.0B | Q4_0 | **~3.8 GB** | 42 – 52 tok/sec |
| **Qwen 2.5 Coder 1.5B** (`qwen2.5-coder:1.5b`)| 1.5B | Q8_0 | **~1.6 GB** | 90+ tok/sec |

---

## 3. Quickstart Installation Guide

### Step 1: Install Ollama
Download and run the installer for Windows from [ollama.com](https://ollama.com/download/windows).

Verify NVIDIA GPU acceleration:
```powershell
ollama --version
nvidia-smi
```

### Step 2: Pull the Recommended Coding Model
```powershell
# Pull Qwen 2.5 Coder 7B (State of the art 7B coding model)
ollama pull qwen2.5-coder:7b

# Or for ultra-low VRAM laptops (under 4GB):
ollama pull qwen2.5-coder:1.5b
```

### Step 3: Verify Model Execution
```powershell
ollama run qwen2.5-coder:7b "Explain SQL Injection in Java and suggest a fix"
```

---

## 4. Configuring Codexa for Local Ollama

In your `.env` file or environment variables:

```bash
# Set AI provider to local Ollama
CODEXA_AI_PROVIDER=ollama
CODEXA_AI_ENDPOINT=http://localhost:11434/v1/chat/completions
CODEXA_AI_MODEL=qwen2.5-coder:7b
CODEXA_AI_FALLBACK_MODEL=deepseek-coder:6.7b
```

Or in `backend/src/main/resources/application.yml`:

```yaml
codexa:
  ai:
    enabled: true
    provider: ollama
    endpoint: http://localhost:11434/v1/chat/completions
    model: qwen2.5-coder:7b
    fallback-model: deepseek-coder:6.7b
    timeout-ms: 15000
```

---

## 5. Resilient AI Cascading Architecture

When configured for local Ollama, Codexa maintains a 3-tier safety net:

```
[ Static Analysis Finding ]
            │
            ▼
┌───────────────────────────────────────┐
│ Tier 1: Local Ollama / RTX GPU        │  <-- Ultra-fast, $0 cost, zero egress
│ (http://localhost:11434)              │
└───────────────────┬───────────────────┘
                    │ (If Ollama stopped or timeout)
                    ▼
┌───────────────────────────────────────┐
│ Tier 2: OpenRouter Cloud Cascade      │  <-- Free tier cloud fallback
│ (meta-llama/llama-3.3-70b-instruct)   │
└───────────────────┬───────────────────┘
                    │ (If network offline or rate limited)
                    ▼
┌───────────────────────────────────────┐
│ Tier 3: Deterministic Template Engine │  <-- 0ms latency, zero dependencies
│ (Built-in CWE & OWASP Expert System)  │
└───────────────────────────────────────┘
```
