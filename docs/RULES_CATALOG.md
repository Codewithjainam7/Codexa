# Codexa Complete Static Analysis & Security Rule Catalog 🛡️

> Codexa analyzes source code against **30+ deterministic static rules** spanning cryptographic security, injection vulnerabilities, structural code quality, operational readiness, and polyglot architecture patterns. Every rule is mapped to **OWASP Top 10 (2021)** and the **MITRE Common Weakness Enumeration (CWE)**.

---

## 1. Security & Cryptographic Rules

| Rule ID | Title | Severity | OWASP Top 10 | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-SQL-001` | SQL Injection via Dynamic Query Construction | CRITICAL | A03:2021-Injection | CWE-89 | Detects unescaped string concatenation or formatting inside raw SQL statements, `Statement.executeQuery()`, or JPA native queries. |
| `CR-CMD-001` | Command Injection & Process Execution | CRITICAL | A03:2021-Injection | CWE-78 | Detects invocation of `Runtime.getRuntime().exec(...)` or `ProcessBuilder` with user-controllable input or unsanitized strings. |
| `CR-SEC-005` | Insecure Object Deserialization | CRITICAL | A08:2021-Software and Data Integrity | CWE-502 | Identifies untrusted stream deserialization via `ObjectInputStream.readObject()` lacking class-filter validation. |
| `CR-SEC-001` | Hardcoded Secrets & High-Entropy API Keys | HIGH | A07:2021-Identification & Auth | CWE-798 | Detects exposed AWS access keys (`AKIA...`), GitHub personal access tokens (`ghp_...`), private keys, and high-entropy secret assignments. |
| `CR-SEC-002` | Fallback Secret Assignment | HIGH | A07:2021-Identification & Auth | CWE-798 | Detects fallback secrets in environment variable retrieval (e.g. `process.env.MAILTRAP_API_TOKEN \|\| 'secret'` or `?? 'secret'`). |
| `CR-SEC-003` | Path Traversal & Arbitrary File Access | HIGH | A01:2021-Broken Access Control | CWE-22 | Detects instantiation of `File` or `Path` objects using request parameters without canonical boundary checks (`normalize()`, `startsWith()`). |
| `CR-SEC-004` | Server-Side Request Forgery (SSRF) | HIGH | A10:2021-SSRF | CWE-918 | Detects outbound HTTP requests (`HttpURLConnection`, `HttpClient`, `RestTemplate`) constructed with unvalidated user-supplied URLs. |
| `CR-SEC-006` | CSRF & State Mutation in Safe GET Endpoints | HIGH | A01:2021-Broken Access Control | CWE-352 | Flags state-modifying database operations (insert, update, delete) invoked within HTTP GET endpoint handlers. |
| `CR-SEC-007` | Insecure Random Number Generator | HIGH | A02:2021-Cryptographic Failures | CWE-330 | Flags use of non-cryptographic PRNGs (`java.util.Random`, `Math.random()`) for security-sensitive tokens, site access PINs, door codes, or session IDs. |
| `CR-SEC-008` | Hardcoded Internal RFC 1918 IP Address | MEDIUM | A05:2021-Security Misconfiguration | CWE-668 | Flags hardcoded private IP addresses (`10.x.x.x`, `192.168.x.x`, `172.16-31.x.x`) leaking internal infrastructure topology. |
| `CR-SEC-009` | Weak Cryptographic Hash Function | HIGH | A02:2021-Cryptographic Failures | CWE-328 | Detects obsolete hashing algorithms (`MD5`, `MD2`, `SHA-1`) initialized via `MessageDigest.getInstance(...)`. |
| `CR-SEC-010` | Disabled TLS/SSL Certificate Validation | CRITICAL | A02:2021-Cryptographic Failures | CWE-295 | Detects empty or disabled `checkServerTrusted()` or `checkClientTrusted()` implementations in custom `X509TrustManager` classes. |
| `CR-SEC-013` | Hardcoded JSON Web Token (JWT) | HIGH | A07:2021-Identification & Auth | CWE-798 | Detects embedded JWT authorization tokens (`eyJhbGci...`) in source code, configuration files, or seed scripts. |
| `CR-AUTH-001` | Missing Endpoint Access Control Annotation | HIGH | A01:2021-Broken Access Control | CWE-862 | Flags HTTP controller route handlers lacking authorization guards (`@PreAuthorize`, `@Secured`, `@RolesAllowed`). |
| `CR-XSS-001` | Cross-Site Scripting (XSS) in Controllers | HIGH | A03:2021-Injection | CWE-79 | Detects unescaped request input written directly into HTTP responses, raw HTML templates, or `@ResponseBody` payloads. |
| `CR-PASS-001` | Weak Password Storage / Hashing | HIGH | A02:2021-Cryptographic Failures | CWE-328 | Detects user passwords hashed with plain MD5 or SHA-1 instead of salt-stretched algorithms (Argon2, bcrypt, PBKDF2). |
| `CR-CRYPTO-001`| Obsolete Cipher Algorithm or Mode | MEDIUM | A02:2021-Cryptographic Failures | CWE-327 | Identifies use of insecure symmetric ciphers (`DES`, `3DES`, `Blowfish`) or unauthenticated ECB mode (`AES/ECB`). |
| `CR-CONFIG-001`| Overly Permissive CORS Configuration | MEDIUM | A05:2021-Security Misconfiguration | CWE-942 | Flags wildcard CORS origin configuration (`allowedOrigins("*")`) combined with credentials enabled (`allowCredentials(true)`). |
| `CR-DEP-001` | Known Vulnerable or Outdated Dependency | MEDIUM | A06:2021-Vulnerable Components | CWE-1395 | Flags dependencies with known CVEs declared in `pom.xml` or `package.json`. |

---

## 2. Universal & Polyglot Architecture Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-RLS-001` | Insecure Row-Level Security (RLS) Policy | CRITICAL | SECURITY | CWE-862 | Detects SQL policy statements that disable RLS or grant unconditional access (e.g. `CREATE POLICY ... FOR ALL USING (true)`). Evaluates UTF-16LE, UTF-16BE, and UTF-8 encoded files. |
| `CR-EDGE-001` | Unauthenticated Serverless Edge Function | HIGH | SECURITY | CWE-306 | Detects Supabase / Deno serverless edge functions performing mutating actions (emails, database writes) without authenticating caller credentials (`req.headers.get('authorization')` or `auth.getUser()`). |
| `CR-ARCH-001` | Dev Middleware Production Trap | HIGH | ARCHITECTURE | CWE-1068 | Detects API route handlers defined inside development server middleware (e.g. Vite `configureServer` `server.middlewares.use('/api/...')`), which fail with HTTP 404 when built for production. |
| `CR-MULTI-001` | Polyglot High-Entropy Secret Detection | HIGH | SECURITY | CWE-798 | Scans non-Java files (TypeScript, Python, Go, PHP, C#) for hardcoded secrets, database credentials, and unescaped SQL strings. |

---

## 3. Structural Quality & Maintainability Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-QUAL-001` | High Cyclomatic Complexity ($CC > 15$) | MEDIUM | QUALITY | CWE-1074 | Calculates McCabe cyclomatic complexity per method. Methods exceeding 15 decision paths signal excessive branching and defect propensity. |
| `CR-QUAL-002` | Monolithic Long Method Code Smell | LOW | QUALITY | CWE-1075 | Identifies method bodies exceeding 50 executable lines of code, violating the Single Responsibility Principle. |
| `CR-QUAL-003` | Deep Control Flow Nesting Depth | LOW | QUALITY | CWE-1075 | Flags AST statement blocks nested deeper than 4 levels of conditionals and loops (`if`, `for`, `while`, `try`). |
| `CR-QUAL-004` | Substantial Duplicate Logic Blocks | LOW | QUALITY | CWE-1041 | Identifies repetitive identical AST token blocks across functions or classes, violating DRY principles. |
| `CR-QUAL-005` | Direct Controller-to-Persistence Access | MEDIUM | ARCHITECTURE | CWE-1068 | Flags direct autowiring or invocation of `Repository` or `EntityManager` instances inside `@Controller` classes, bypassing the service layer. |
| `CR-QUAL-006` | Swallowed Exception in Empty Catch Block | MEDIUM | QUALITY | CWE-390 | Detects empty `catch` blocks or swallowed exceptions where caught errors are ignored without logging or re-throwing. |

---

## 4. Operational Readiness & Performance Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-OPS-001` | Missing Circuit Breaker / Timeout Handlers | MEDIUM | OPERATIONS | CWE-755 | Identifies external HTTP or RPC client calls lacking explicit connect and read timeouts or circuit breaker wrappers. |
| `CR-OPS-002` | Sensitive Information in Log Statements | MEDIUM | OPERATIONS | CWE-532 | Detects variables representing sensitive information (passwords, tokens, credentials, SSNs) passed directly to logger methods. |
| `CR-OPS-003` | Unbounded Cached Thread Pool Creation | HIGH | OPERATIONS | CWE-400 | Flags instantiation of `Executors.newCachedThreadPool()` which creates unbounded threads leading to OS thread exhaustion and OOM. |
| `CR-OPS-004` | Missing Input Validation Annotations | MEDIUM | OPERATIONS | CWE-20 | Flags mutating `@RequestBody` controller parameters lacking `@Valid` or `@Validated` annotations. |
| `CR-PERF-001` | Quadratic String Concatenation in Loops | MEDIUM | PERFORMANCE | CWE-400 | Detects string `+=` concatenation inside loop bodies (`for`, `while`), causing $O(N^2)$ memory re-allocations instead of using `StringBuilder`. |
