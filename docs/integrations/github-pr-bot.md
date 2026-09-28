# Codexa GitHub PR Review Bot & Inline Suggestion Action

This guide documents the autonomous **Codexa GitHub PR Review Bot**, explaining how it analyzes pull request diffs, maps security vulnerabilities to exact code lines, posts 1-click **"Apply Suggestion"** diffs, and enforces production readiness gates.

---

## 1. Overview & Developer Experience

When developers open or update a Pull Request, the Codexa PR Review Bot automatically executes within GitHub Actions to provide instantaneous, line-by-line security and quality auditing:

```
[ Developer opens / updates Pull Request ]
                    │
                    ▼
[ GitHub Actions: .github/workflows/codexa-pr-review.yml ]
  - Checks out feature branch & base branch (main)
  - Computes modified hunks via `git diff -U0 origin/main...HEAD`
  - Runs `scripts/codexa-pr-reviewer.js` static rule engine
                    │
                    ▼
[ Automated GitHub PR Review Created ]
  ├── Executive Summary Table (Readiness Score, Verdict, Critical Counts)
  └── Inline Line-by-Line Comments with 1-Click "Apply Suggestion" Diffs
```

### Visual Example of Inline Suggestion in PR:

> **🛡️ Codexa Security Alert: [CR-SQL-001] SQL Injection in Dynamic Query**
> 
> **Severity:** `CRITICAL` | **Taxonomy:** `CWE-89` | **Category:** `SECURITY`
> 
> > Potential SQL Injection: detected dynamic string concatenation in executable query statement.
> 
> **Remediation Advice:** Use PreparedStatement query parameter placeholders (`?`) rather than raw string concatenation.
> 
> #### 💡 Suggested 1-Click Fix:
> ```suggestion
> PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
> stmt.setString(1, userId);
> ```
> *(Developers can click **"Commit suggestion"** directly inside GitHub to apply the fix without leaving the browser).*

---

## 2. Executive PR Review Summary Table

The bot posts an executive scorecard directly to the pull request conversation:

| Metric | Result | Status |
|:---|:---:|:---:|
| **Production Readiness Score** | **100 / 100** | 🟢 PASSED |
| **Total Violations Detected** | **0** | 🟢 CLEAN |
| **Critical Security Blockers** | **0** | 🟢 ZERO |
| **Audit Verdict** | `PRODUCTION_READY` | 🟢 APPROVED |

If violations are found, the PR check is marked as **failed (blocked)**, preventing unsafe code from being merged into `main`.

---

## 3. Configuration & Customization Options

The review bot can be configured via environment variables in `.github/workflows/codexa-pr-review.yml`:

| Environment Variable | Default Value | Description |
|:---|:---:|:---|
| `MIN_READINESS_SCORE` | `85` | Minimum score required to pass the PR gating check ($0-100$). |
| `BASE_REF` | `origin/${{ github.base_ref }}` | Base git branch to compute the diff against (e.g. `main` or `release`). |
| `HEAD_REF` | `HEAD` | Current pull request commit SHA. |

---

## 4. Rule Coverage in PR Reviews

The PR reviewer inspects modified lines against high-severity security and quality rules:
- **`CR-SQL-001`**: SQL Injection in dynamic concatenation queries.
- **`CR-CMD-001`**: OS Command Injection via shell calls.
- **`CR-SECRET-001`**: Committed secrets, API keys, and cloud credentials (AWS, GitHub, OpenRouter).
- **`CR-PATH-001`**: Path Traversal (Zip Slip) without path normalization.
- **`CR-HASH-001`**: Broken cryptographic hashes (MD5, SHA-1).
- **`CR-RAND-001`**: Weak pseudo-random number generators (`java.util.Random` for security keys).
- **`CR-QUAL-006`**: Swallowed exceptions in empty catch blocks.

---

## 5. Local Testing & Dry-Run

Developers can run the PR review engine locally against their working tree before pushing:

```bash
# Run against local staged or uncommitted diff
node scripts/codexa-pr-reviewer.js

# Run test suite
node scripts/test-pr-reviewer.js
```
