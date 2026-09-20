# Secret Masking & Redaction Specifications

To ensure Codexa never acts as an inadvertent credential exfiltration vector, all code evidence lines, finding titles, log statements, and AI prompt payloads pass through `SecretMasker.java` prior to serialization or database persistence.

---

## 1. Masking Scope & Security Boundary

Secret redaction is enforced at the earliest ingestion boundary:
- **Rule Findings Evidence**: Code snippets extracted from AST or regex matches are masked.
- **LLM Prompt Enrichment**: When sending snippets to OpenRouter / AI models, all secrets are replaced with placeholder tokens (`[REDACTED_API_KEY]`).
- **Audit Reports**: PDF, HTML, Markdown, JSON, and SARIF exports contain zero raw secrets.
- **Application Logs**: Slf4j / Logback logging pipelines sanitize URLs and authorization parameters.

---

## 2. Redaction Signatures & Replacement Formats

| Secret Class | Regex Pattern / Token | Masked Representation |
| :--- | :--- | :--- |
| **AWS Access Key** | `AKIA[0-9A-Z]{16}` | `AKIA****************` (preserves prefix for triage) |
| **AWS Secret Key** | High-entropy 40-character base64 strings | `[REDACTED_AWS_SECRET]` |
| **GitHub Token** | `gh[pors]_[a-zA-Z0-9]{36,255}` | `ghp_************************************` |
| **JWT Bearer Token** | `eyJ[a-zA-Z0-9_-]+\.eyJ[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+` | `eyJ*****.eyJ*****.*****` |
| **Private Keys** | `-----BEGIN (?:RSA \|EC \|DSA \|OPENSSH )?PRIVATE KEY-----` | `[REDACTED_PRIVATE_KEY]` |
| **Database Passwords** | `(?<=://[^:]+:)[^@]+(?=@)` | `********` |
| **OpenAI / Anthropic Keys** | `sk-[a-zA-Z0-9_-]{20,}` | `sk-************************` |
| **Generic Password Assignments** | `(?i)(password\|secret\|apiKey\|token)\s*[:=]\s*["'][^"']+["']` | `$1 = "[REDACTED]"` |

---

## 3. Shannon Entropy Heuristics

For unstructured strings that do not match vendor-specific prefixes, Codexa computes the Shannon Entropy:

$$H(X) = -\sum_{i=1}^{n} P(x_i) \log_2 P(x_i)$$

- Strings with length $\ge 20$ characters and $H(X) \ge 4.5$ bits/symbol in variable assignments are flagged as potential high-entropy credentials.
- Known false positives (UUIDs, SHA256 hashes of test fixtures, base64-encoded SVG icons) are explicitly whitelisted.

---

## 4. Test Verification

Validated by automated test suite:
- `SecretMaskingSecurityTest`: Tests all 8 secret token categories against synthetic credential samples to guarantee complete redaction.
