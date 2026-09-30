# Executive Trust Badges & README Integration Guide

## 1. Overview
Codexa generates vector SVG shield trust badges directly from analysis job results, providing instant proof of automated security compliance for open-source repositories and commercial projects.

These badges can be embedded directly in a repository's `README.md` or displayed on enterprise status pages.

---

## 2. API Endpoint Specification

```http
GET /api/v1/analyses/{jobId}/badge?standard={standard}
```

### Parameters
| Parameter | Type | Default | Supported Values |
| :--- | :--- | :--- | :--- |
| `jobId` | `UUID` | (Required) | Target Analysis Job UUID |
| `standard` | `String` | `soc2` | `soc2`, `iso27001`, `pci-dss`, `owasp`, `readiness`, `score` |

### Response Headers
* `Content-Type: image/svg+xml`
* `Cache-Control: max-age=300, s-maxage=300` (Edge CDN cached for 5 minutes)

---

## 3. Dynamic Badge State Logic

The badge visual state and right-hand color are determined by the regulatory compliance evaluation:

| Status | Threshold | Color | Right Text Example |
| :--- | :--- | :--- | :--- |
| **AUDIT_READY** | Zero critical/high violations, $\ge 85\%$ score | `#10b981` (Emerald) | `PASS (98%)` |
| **CONDITIONAL_PASS** | Zero critical violations, $\ge 70\%$ score | `#f59e0b` (Amber) | `CONDITIONAL (82%)` |
| **AUDIT_BLOCKED** | Any blocking or critical violation | `#ef4444` (Rose Red) | `BLOCKED` |

---

## 4. Markdown & HTML Embed Examples

### Markdown (README.md)
```markdown
[![Codexa SOC 2 Compliance](https://codexa-ye85.onrender.com/api/v1/analyses/YOUR_JOB_ID/badge?standard=soc2)](https://codexa-ye85.onrender.com)
[![Codexa Readiness Score](https://codexa-ye85.onrender.com/api/v1/analyses/YOUR_JOB_ID/badge?standard=readiness)](https://codexa-ye85.onrender.com)
```

### HTML (Websites & Documentation)
```html
<a href="https://codexa-ye85.onrender.com" target="_blank" rel="noreferrer">
  <img src="https://codexa-ye85.onrender.com/api/v1/analyses/YOUR_JOB_ID/badge?standard=soc2" alt="Codexa Security Badge" />
</a>
```
