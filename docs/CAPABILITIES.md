# Codexa Technical Capabilities & Feature Reference 🛡️

> **Codexa** is an enterprise-grade, autonomous static security auditor, structural code quality inspector, and pre-deployment production readiness gatekeeper. It evaluates untrusted codebases across multiple languages without executing a single line of target code.

---

## 1. High-Scale Ingestion & Sandboxing Engine

Codexa is engineered to ingest large-scale codebases safely and efficiently:

### Ingestion Vectors
- **ZIP Archive Upload**: Supports compressed archives up to **3.0 GB (3,072 MB)**, handling enterprise repositories with up to **50,000 files**.
- **Public GitHub Repository Cloning**: Ingests public repositories via URL (`https://github.com/org/repo`), with support for an optional `GITHUB_TOKEN` to bypass anonymous GitHub API rate limits.
- **Selective Shallow Fetching**: GitHub ingestion uses shallow cloning and selective branch retrieval to minimize network latency.

### Defensive Sandboxing & Ingestion Security
- **Strict Zero Code Execution**: Codexa never compiles, interprets, or runs untrusted code in a JVM classloader, Node runtime, or OS subprocess. All analysis is strictly static, lexical, and AST-driven.
- **Zip Slip & Directory Traversal Protection**: Every archive entry is resolved against canonical real paths (`.toRealPath()` / `getCanonicalPath()`). Entries attempting directory traversal (`../` or leading root paths) are rejected with an immediate `400 Bad Request`.
- **SSRF (Server-Side Request Forgery) Prevention**: Remote repository cloning permits only `https://` URLs pointing to verified domain names (`github.com`, `api.github.com`). All IP resolutions block loopback addresses (`127.0.0.0/8`), private RFC 1918 subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), link-local IPs (`169.254.0.0/16`), and cloud metadata services (`169.254.169.254`).
- **High-Throughput Streaming Extraction**: Utilizes **64 KB buffer allocations** (an 8x throughput boost over standard 8 KB buffers), extracting large archives at rates exceeding **670 MB/s**.
- **Isolated Staging Quotas**: Staging areas are isolated by UUID (`.staging/<uuid>`) and automatically purged on sweep intervals or immediately upon job completion.
- **Smart Asset Filtering**: Automatically filters out vendor dependencies (`node_modules/`, `vendor/`, `.venv/`, `bower_components/`), build artifacts (`target/`, `build/`, `dist/`), compiled binaries (`.class`, `.jar`, `.exe`, `.dll`), minified scripts (`*.min.js`, `*.bundle.js`), and sourcemaps (`*.map`).

---

## 2. Dual-Spectrum Inspection Engine

Codexa pioneers the **Dual-Spectrum Inspection** methodology, unifying internal code maintainability analysis with external black-box attack surface discovery:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Codexa Dual-Spectrum Engine                          │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
         ┌───────────────────────────┴───────────────────────────┐
         ▼                                                       ▼
┌─────────────────────────────────┐             ┌─────────────────────────────────┐
│     White-Box AST Code Audit    │             │ Black-Box Attack Surface Scanner│
├─────────────────────────────────┤             ├─────────────────────────────────┤
│ • McCabe Cyclomatic Complexity  │             │ • Automated HTTP Route Discovery│
│ • Maximum Nesting Depth (AST)   │             │ • Ingress Parameter Extraction  │
│ • Monolithic Method & Class LOC │             │ • Access Control Verification   │
│ • Structural Tally (LOC/Classes)│             │ • Route Risk Profiling          │
│ • Complex Files Refactoring Top │             │ • Perimeter Security Posture    │
└─────────────────────────────────┘             └─────────────────────────────────┘
```

### A. White-Box AST Structural Audit
- **McCabe Cyclomatic Complexity**: Calculates cyclomatic complexity ($M = E - N + 2P$) for every method, function, and routine. Methods with $CC > 15$ are flagged as maintainability hazards; $CC > 25$ triggers high-severity alerts.
- **Maximum Block Nesting Depth**: Analyzes AST block hierarchy to detect deeply nested conditional branches and loops ($> 4$ levels), diagnosing the "arrow anti-pattern".
- **Method & Class Dimensions**: Identifies oversized monolithic methods ($> 50$ executable lines) and God-classes.
- **Codebase Structural Inventory**: Calculates total executable LOC, comment LOC, blank lines, class counts, interface counts, method counts, and field tallies.
- **Top Complex Files Leaderboard**: Ranks the top 10 most complex files across the project with actionable refactoring recommendations.

### B. Black-Box Attack Surface Simulation
- **Automated HTTP Route Discovery**: Scans controllers and route declarations across Spring MVC (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@RequestMapping`), Express/Node.js routers, and FastAPI/Flask endpoints.
- **Ingress Vector & Payload Extraction**: Maps incoming request parameters (`@PathVariable`, `@RequestParam`, `@RequestHeader`, `@RequestBody`).
- **Access Control Boundary Checks**: Evaluates whether mutating endpoints are guarded by authorization annotations (`@PreAuthorize`, `@Secured`, `@RolesAllowed`) or exposed publicly without authentication.
- **Perimeter Defense Posture**: Inspects CORS configurations, CSRF token protections, rate limiting filters, and input validation annotations (`@Valid`, `@Validated`, `@NotNull`).
- **Attack Surface Risk Rating**: Profiles each endpoint as `HIGH`, `MEDIUM`, or `LOW` risk based on HTTP method (mutating state vs read-only), input complexity, and authorization boundaries.

---

## 3. Polyglot Multi-Language Analysis & Encoding Engine

Codexa inspects polyglot repositories containing Java, TypeScript, JavaScript, Python, SQL, Go, PHP, and configuration files:

### Multi-Encoding & BOM Auto-Detection
- **UTF-16LE & UTF-16BE Decoding**: Automatically detects and decodes UTF-16 Little Endian and Big Endian files (with or without Byte Order Marks `0xFF 0xFE` / `0xFE 0xFF`).
- **UTF-8 BOM Detection**: Automatically strips UTF-8 BOM headers (`0xEF 0xBB 0xBF`).
- **Null-Byte Elimination**: Strips binary null bytes (`\0`) resulting from Windows PowerShell output redirects (`>`) and SQL query dumps, preventing pattern matching misses and regex failures.
- **Encoding Fallback Chain**: Evaluates UTF-16LE $\rightarrow$ UTF-16BE $\rightarrow$ UTF-8 $\rightarrow$ ISO-8859-1 gracefully without throwing `MalformedInputException`.

### Polyglot Detection Capabilities
- **Fallback Secret & Token Extraction**: Catches hardcoded API keys and tokens hidden behind fallback operators:
  - Logical OR (`process.env.TOKEN || 'hardcoded_secret'`)
  - Nullish Coalescing (`process.env.TOKEN ?? 'hardcoded_secret'`)
  - Supported token patterns: Mailtrap, SendGrid, Resend, AWS, Stripe, Slack, Twilio, JWT secrets, and private keys.
- **Dev Middleware Production Trap Detection**: Detects development server middleware API routes (e.g. `configureServer(server) { server.middlewares.use('/api/...') }` in `vite.config.ts`), which cause critical 404 failures when deployed to production without a real backend.
- **Predictable PINs & Security Tokens**:
  - Flags insecure random site access PINs, door codes, and OTPs generated using `Math.random()` (e.g., `Math.floor(1000 + Math.random() * 9000)`).
  - Flags predictable magic links and session tokens constructed with timestamp concatenation (e.g., `tok_${id}_${Date.now()}`).
- **Zero-Trust Supabase Edge Function Audits**:
  - Distinguishes real authorization checks (`req.headers.get('authorization')`, `auth.getUser()`, `auth.getSession()`, `verifyJwt()`) from decorative CORS headers (`'Access-Control-Allow-Headers': 'authorization'`).
  - Flags Edge Functions that perform mutating operations (email dispatch, database writes) without authenticating the caller.
- **Insecure Row-Level Security (RLS) Disabling**:
  - Flags SQL policies that bypass access control by granting unconditional anonymous access (e.g., `CREATE POLICY "Allow anon access" ON ... FOR ALL USING (true)`).
- **Hardcoded JWT Tokens**:
  - Detects hardcoded JSON Web Tokens (`eyJhbGci...`) in seed scripts, configuration files, and source code.

---

## 4. Mathematical Scoring & Prioritization Engine

Codexa replaces arbitrary letter grades with formal, explainable mathematical formulas:

### Finding Priority Formula ($P$)
$$P = W_{\text{severity}} \times W_{\text{confidence}} \times W_{\text{exploitability}} \times W_{\text{impact}}$$

- **Severity Weight ($W_s$)**: `CRITICAL` = 1.00, `HIGH` = 0.80, `MEDIUM` = 0.50, `LOW` = 0.20
- **Confidence Weight ($W_c$)**: `CONFIRMED` = 1.00, `HIGH` = 0.80, `MEDIUM` = 0.60, `SUSPECTED` = 0.40
- **Exploitability ($W_e$)**: Scaled 0.10 to 1.00 based on proximity to external HTTP ingress vectors.
- **Impact ($W_i$)**: Scaled 0.10 to 1.00 based on potential blast radius (RCE = 1.00, Information Leak = 0.50, Style = 0.10).

### Production Readiness Score (0–100)
$$\text{Readiness Score} = (\text{Security Score} \times 0.60) + (\text{Quality Score} \times 0.25) + (\text{Operations Score} \times 0.15)$$

### Maintainability Index & Architectural Health
- **Maintainability Index (0–100)**: Incorporates cyclomatic complexity, method length, nesting depth, and defect count.
- **Architectural Health (0–100)**: Penalizes layer bleeding (e.g. direct persistence calls from controllers `CR-QUAL-005`), God classes, and cyclic controller dependencies.

### Hard Production Verdict Overrides
| Condition | Verdict | Score Cap | Action |
| :--- | :--- | :--- | :--- |
| Any `CRITICAL` finding present | **`NOT_READY`** | $\le 40$ | Blocked from production deployment. |
| Any `HIGH` finding present | **`NEEDS_URGENT_FIXES`** | $\le 65$ | Requires urgent remediation prior to release. |
| Only `MEDIUM` / `LOW` findings | **`REVIEW_RECOMMENDED`** | $66 - 79$ | Technical debt cleanup advised. |
| Clean scan / zero critical or high | **`REVIEW_COMPLETE`** | $80 - 100$ | Production-ready baseline established. |

---

## 5. Dual-Path AI & Deterministic Remediation Engine

Codexa provides instant remediation recommendations for every flagged finding:

### In-Flight Secret Sanitization
Before transmitting code snippets to an AI provider:
- High-entropy tokens (`AKIA...`, `ghp_...`, `Bearer eyJ...`) are replaced with `[REDACTED_SECRET]`.
- Database URLs with inline credentials are sanitized to `user:***@host:port/db`.
- Internal RFC 1918 IPs and domain names are masked to prevent infrastructure disclosure.

### Dual-Path Remediation
1. **Cloud Generative AI**: Uses NVIDIA Nemotron (`nvidia/nemotron-3-ultra-550b-a55b:free` with automatic fallback to `nvidia/nemotron-3.5-lightning:free` via OpenRouter) to generate contextual code explanations and Git-style refactoring diffs.
2. **Offline Deterministic Fallback**: If the AI endpoint is disabled, unreachable, or times out, Codexa instantly injects deterministic remediation templates with **zero latency** and **zero cost**.
3. **1-Click Copy Patch**: Users can copy git-style remediation diffs directly into their clipboard from the UI.

---

## 6. Multi-Format Reporting & CI/CD Export

Codexa provides five export formats for executive, regulatory, and developer workflows:

1. **SARIF v2.1.0 JSON**: OASIS Static Analysis Results Format compliant, enabling direct integration into GitHub Advanced Security Code Scanning tabs.
2. **Executive PDF Report**: Styled print report featuring executive scorecards, OWASP distribution charts, and finding breakdowns.
3. **Interactive Standalone HTML**: Offline-capable single-file audit report with responsive dark-mode layout and collapsible findings.
4. **GitHub-Flavored Markdown**: Structured markdown tables and collapsible diff blocks, optimized for posting as automated GitHub Pull Request review comments.
5. **RFC-4180 CSV**: Sanitized tabular data export with spreadsheet formula injection protection (`=`, `+`, `-`, `@` escaped).

---

## 7. Interactive Dashboard & Cross-Platform Client

- **React 18 + Vite 5 + TailwindCSS 3.4**: High-performance single-page web client with responsive layout and zero sparkle/clutter iconography.
- **5-Tab Deep Inspection Workspace**:
  - **Tab 1: Overview**: Executive scorecard, gauge meters, code composition breakdown, and pre-deployment readiness checklist.
  - **Tab 2: Findings & Triage**: Interactive file tree explorer with finding density badges, multi-criteria filters, and side-by-side git diffs.
  - **Tab 3: White-Box AST**: Structural complexity distribution, nesting depth meters, and top complex files leaderboard.
  - **Tab 4: Attack Surface**: Route discovery catalog, HTTP verb badges, ingress parameter maps, and access control status.
  - **Tab 5: OWASP & Compliance**: Finding distribution across OWASP Top 10 (2021) and CWE taxonomy.
- **Cross-Platform Android App**: Built with Capacitor Android (`@capacitor/android`), featuring native safe-area insets (`env(safe-area-inset-bottom)`), optimized mobile touch targets, and offline report caching.
