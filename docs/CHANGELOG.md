# Changelog & Version History

All notable changes to the Codexa Code Security & Production Readiness Platform.

## [1.0.0] - 2026-09-11

### Added
- **30 Deterministic AST Static Rules**:
  - Security rules: `CR-SEC-001` through `CR-SEC-010` (SQLi, Command Injection, Weak Hash MD5/SHA-1, Disabled TLS, Hardcoded Secrets, SSRF, Path Traversal, Insecure Random, Insecure Deserialization, CSRF).
  - Operational readiness rules: `CR-OPS-001` through `CR-OPS-003` (Api Readiness, Observability Logging, Unbounded Thread Pool Construction).
  - Code quality rules: `CR-QUAL-001` through `CR-QUAL-006` (Cyclomatic Complexity, Deep Nesting, Architecture Separation, Duplication, Long Methods, Empty Catch Block Detection).
  - Performance rules: `CR-PERF-001` (String Concatenation in Loops).
- **Multi-Format Report Generation**:
  - SARIF v2.1.0 JSON with CWE and OWASP taxonomy extensions for GitHub Advanced Security.
  - Formatted PDF export with executive summary and remediation snippets.
  - Standalone interactive HTML reports with responsive dark-mode styling.
  - RFC-4180 CSV export with formula injection sanitization.
  - GitHub-flavored Markdown export for PR review automation.
- **Frontend Architecture**:
  - Monochromatic luxury cyber-security aesthetic (zero sparkles icons).
  - Resilient WebSocket / Polling fallback with jittered exponential backoff.
  - Keyboard shortcuts (`/` search focus, `ESC` dashboard back).
  - Unified vendor bundle eliminating chunk dispatcher isolation.

### Security Hardening
- OWASP baseline security response headers (`CSP`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`).
- Constant-time token equality validation preventing timing attacks.
