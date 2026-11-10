# Codexa Complete Static Analysis & Security Rule Catalog 🛡️

> Codexa analyzes source code against **65+ deterministic static rules** spanning cryptographic security, injection vulnerabilities, structural code quality, operational readiness, cloud infrastructure, AI agent boundaries, and polyglot architecture patterns. Every rule is mapped to **OWASP Top 10 (2021)**, **CWE Top 25**, **OWASP API Security**, **CIS Controls v8**, and **OWASP LLM (2025)**.

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

## 3. Parameter & Input Validation Security Rules

| Rule ID | Title | Severity | OWASP Top 10 | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-PARAM-001` | Mass Assignment / DTO Over-Posting | HIGH | A04:2021-Insecure Design | CWE-915 | Detects unvalidated raw request body payloads (`req.body`, `data: req.body`, `...req.body`) passed directly to ORM entity creation or database mutations (`prisma.create()`, `supabase.insert()`, Mongoose, TypeORM), allowing attackers to overwrite sensitive administrative fields (e.g. `role`, `isAdmin`, `verified`). |
| `CR-PARAM-002` | IDOR & Missing Tenant Ownership on Resource Parameters | HIGH | A01:2021-Broken Access Control | CWE-639 | Detects destructive or lookup queries driven solely by user-supplied identifier parameters (`req.params.id`) without scoping by the authenticated user's ID or tenant ID, enabling Insecure Direct Object References. |
| `CR-PARAM-003` | Open Redirect via Unvalidated Destination Parameter | MEDIUM | A01:2021-Broken Access Control | CWE-601 | Detects HTTP redirect responses (`res.redirect()`, `window.location.href`) driven directly by client-controlled URL parameters (`?returnUrl=`, `?redirect=`, `?next=`) without allowlist validation or relative path enforcement. |
| `CR-PARAM-004` | Unbounded Pagination Parameter (Potential Memory DoS) | MEDIUM | A04:2021-Insecure Design | CWE-770 | Flags pagination query parameters (`req.query.limit`, `size`, `pageSize`) parsed and passed to database queries without enforcing a strict maximum ceiling clamp (`Math.min(limit, 100)`), exposing systems to Out-Of-Memory (OOM) crashes. |
| `CR-PARAM-005` | Prototype Pollution via Unsafe Request Parameter Merging | HIGH | A03:2021-Injection | CWE-1321 | Detects recursive merging of untrusted request payloads (`req.body`) into internal objects using `Object.assign()`, `lodash.merge()`, or direct bracket keys, allowing attackers to pollute `Object.prototype`. |

---

## 4. Structural Quality & Maintainability Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-QUAL-001` | High Cyclomatic Complexity ($CC > 15$) | MEDIUM | QUALITY | CWE-1074 | Calculates McCabe cyclomatic complexity per method. Methods exceeding 15 decision paths signal excessive branching and defect propensity. |
| `CR-QUAL-002` | Monolithic Long Method Code Smell | LOW | QUALITY | CWE-1075 | Identifies method bodies exceeding 50 executable lines of code, violating the Single Responsibility Principle. |
| `CR-QUAL-003` | Deep Control Flow Nesting Depth | LOW | QUALITY | CWE-1075 | Flags AST statement blocks nested deeper than 4 levels of conditionals and loops (`if`, `for`, `while`, `try`). |
| `CR-QUAL-004` | Substantial Duplicate Logic Blocks | LOW | QUALITY | CWE-1041 | Identifies repetitive identical AST token blocks across functions or classes, violating DRY principles. |
| `CR-QUAL-005` | Direct Controller-to-Persistence Access | MEDIUM | ARCHITECTURE | CWE-1068 | Flags direct autowiring or invocation of `Repository` or `EntityManager` instances inside `@Controller` classes, bypassing the service layer. |
| `CR-QUAL-006` | Swallowed Exception in Empty Catch Block | MEDIUM | QUALITY | CWE-390 | Detects empty `catch` blocks or swallowed exceptions where caught errors are ignored without logging or re-throwing. |

---

## 5. Operational Readiness & Performance Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-OPS-001` | Missing Circuit Breaker / Timeout Handlers | MEDIUM | OPERATIONS | CWE-755 | Identifies external HTTP or RPC client calls lacking explicit connect and read timeouts or circuit breaker wrappers. |
| `CR-OPS-002` | Sensitive Information in Log Statements | MEDIUM | OPERATIONS | CWE-532 | Detects variables representing sensitive information (passwords, tokens, credentials, SSNs) passed directly to logger methods. |
| `CR-OPS-003` | Unbounded Cached Thread Pool Creation | HIGH | OPERATIONS | CWE-400 | Flags instantiation of `Executors.newCachedThreadPool()` which creates unbounded threads leading to OS thread exhaustion and OOM. |
| `CR-OPS-004` | Missing Input Validation Annotations | MEDIUM | OPERATIONS | CWE-20 | Flags mutating `@RequestBody` controller parameters lacking `@Valid` or `@Validated` annotations. |
| `CR-PERF-001` | Quadratic String Concatenation in Loops | MEDIUM | PERFORMANCE | CWE-400 | Detects string `+=` concatenation inside loop bodies (`for`, `while`), causing $O(N^2)$ memory re-allocations instead of using `StringBuilder`. |

---

## 6. Advanced Web & Modern Framework Rules

| Rule ID | Title | Severity | OWASP Top 10 | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-XXE-001` | XML External Entity (XXE) Injection | HIGH | A05:2021-Security Misconfiguration | CWE-611 | Detects XML parsers (`DocumentBuilderFactory`, `SAXParserFactory`, `XMLInputFactory`, `lxml`) initialized without disabling DOCTYPE declarations or external entity expansion. |
| `CR-SSTI-001` | Server-Side Template Injection (SSTI) | CRITICAL | A03:2021-Injection | CWE-1336 | Detects untrusted user input concatenated into template engines (Jinja2, FreeMarker, Velocity, Handlebars), enabling Remote Code Execution. |
| `CR-JWT-001` | Insecure JWT Signature & Algorithm None | CRITICAL | A02:2021-Cryptographic Failures | CWE-347 | Detects disabled JWT signature verification (`verify=false`, `parseClaimsJwt`) or acceptance of the insecure 'none' algorithm. |
| `CR-REDOS-001`| Regular Expression Denial of Service (ReDoS) | HIGH | A06:2021-Vulnerable Components | CWE-1333 | Detects catastrophic exponential/polynomial backtracking patterns (nested quantifiers like `(a+)+`) in compiled regular expressions. |
| `CR-RSC-001` | Unauthenticated Next.js Server Action | HIGH | A01:2021-Broken Access Control | CWE-306 | Detects exported `'use server'` Server Actions performing database mutations or payments without verifying caller session authentication (`auth()`). |
| `CR-CRYPTO-002`| Insecure Cipher Mode (ECB), Static IV & Weak Keys | HIGH | A02:2021-Cryptographic Failures | CWE-326 / CWE-329 | Detects `AES/ECB` unauthenticated mode, static zero-filled IV byte arrays in `IvParameterSpec`, and short RSA key sizes (< 2048-bit). |

---

## 7. Cloud Infrastructure-as-Code (IaC) Rules

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-IAC-001` | Dockerfile Implicit Root User Execution | HIGH | OPERATIONS | CWE-250 | Flags Dockerfiles that do not specify a non-root `USER` directive before the container entrypoint. |
| `CR-IAC-002` | Dockerfile Untagged Base Image (:latest) | MEDIUM | OPERATIONS | CWE-1104 | Flags `FROM image:latest` declarations in Dockerfiles that cause non-deterministic production deployments. |
| `CR-IAC-003` | Kubernetes Privileged Container Escalation | CRITICAL | SECURITY | CWE-250 | Flags Kubernetes pod manifests specifying `securityContext.privileged: true` or `allowPrivilegeEscalation: true`. |
| `CR-IAC-005` | Terraform Unrestricted Ingress (0.0.0.0/0) | HIGH | SECURITY | CWE-284 | Flags security group ingress rules permitting unrestricted global access (`0.0.0.0/0`) on sensitive administrative ports (22, 3389, 5432, 3306). |
| `CR-IAC-006` | Terraform Unencrypted Cloud Storage Volumes | MEDIUM | SECURITY | CWE-311 | Flags cloud storage volumes (`aws_ebs_volume`, RDS, GCS) provisioned with encryption disabled. |
| `CR-IAC-007` | Overly Permissive Cloud IAM Wildcard Policy | CRITICAL | SECURITY | CWE-732 | Flags IAM policies granting wildcard actions (`Action: "*"`) across cloud service APIs, violating Least Privilege. |
| `CR-IAC-008` | Public Cloud Storage Bucket Exposure | CRITICAL | SECURITY | CWE-284 | Flags storage buckets configured with public-read ACLs or disabled S3 Block Public Access controls. |

---

## 8. OWASP Top 10 for LLMs & AI Agent Rules (2025)

| Rule ID | Title | Severity | OWASP LLM | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-LLM-001` | Direct Prompt Injection via String Formatting | HIGH | LLM01:2025 | CWE-20 | Detects untrusted user input directly concatenated into LLM system prompts without role separation. |
| `CR-LLM-002` | Insecure LLM Output Handling & Sinks | HIGH | LLM02:2025 | CWE-79 / CWE-78 | Detects model outputs passed directly to HTML rendering, SQL queries, or command sinks without sanitization. |
| `CR-LLM-003` | Insecure Autonomous AI Agent Tool Execution | CRITICAL | LLM07:2025 | CWE-74 | Flags autonomous AI agent systems binding model outputs directly into system shells or execution REPLs without human confirmation (HITL). |

---

## 9. Distributed Protocols, Microservices & Data Stores

| Rule ID | Title | Severity | OWASP Top 10 | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-GRAPHQL-001` | GraphQL Query Depth Limiting & Introspection | HIGH | A04:2021-Insecure Design | CWE-770 / CWE-200 | Flags GraphQL servers initialized without query depth limiting (DoS vulnerability) or exposing schema introspection in production. |
| `CR-WEBSOCKET-001`| Insecure WebSocket Origin Validation (CSWSH) | HIGH | A01:2021-Broken Access Control | CWE-1385 | Detects WebSocket handlers with wildcard origin (`*`), enabling Cross-Site WebSocket Hijacking. |
| `CR-GRPC-001` | gRPC Plaintext Channel & Unauthenticated Service | HIGH | A02:2021-Cryptographic Failures | CWE-319 | Detects gRPC channels instantiated via `.usePlaintext()` or `insecure.NewCredentials()` transmitting payloads without TLS. |
| `CR-REDIS-001` | Redis Command & Lua Script Injection | HIGH | A03:2021-Injection | CWE-94 | Detects dynamic string concatenation into Redis `eval()` or `evalsha()` enabling cache poisoning and Lua sandbox escapes. |
| `CR-NOSQL-001` | NoSQL Operator Injection & $where Execution | CRITICAL | A03:2021-Injection | CWE-943 | Flags raw `req.body` passed directly to NoSQL queries allowing query operator bypass (`{"$ne": null}`) or dynamic `$where` evaluation. |
| `CR-RACE-001` | Time-of-Check to Time-of-Use (TOCTOU) | MEDIUM | A04:2021-Insecure Design | CWE-367 | Detects non-atomic file/resource presence checks immediately preceding operations without locks or exclusive creation flags. |
| `CR-MEMORY-001` | Unclosed Stream & Native Resource Leak | MEDIUM | Maintainability | CWE-775 | Detects `AutoCloseable` streams or buffers instantiated outside try-with-resources blocks, risking descriptor leaks. |
| `CR-DEAD-001` | Dead Code & Unreachable Branch Detection | LOW | Maintainability | CWE-561 | Detects statements placed after unconditional return/throw, `if (false)` clauses, and uninvoked private methods. |
| `CR-CONF-002` | Multi-Environment Configuration Drift | HIGH | A05:2021-Security Misconfiguration | CWE-16 | Detects destructive database configurations (`ddl-auto: create`) or disabled security controls inside production profiles. |

---

## 10. Kubernetes & Supply Chain Hardening

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-K8S-001` | Kubernetes HostPath Mount & Node Escapes | CRITICAL | SECURITY | CWE-284 | Detects hostPath mounts targeting sensitive node paths (`/var/run/docker.sock`, `/etc`, `/proc`), risking node breakout. |
| `CR-HELM-001` | Hardcoded Secrets in Helm Chart Values | HIGH | SECURITY | CWE-312 | Detects plaintext database credentials and tokens committed into Helm `values.yaml` files. |
| `CR-SUPPLY-001` | Dependency Confusion & Insecure Registry | HIGH | SECURITY | CWE-829 | Flags pip or npm configs using `extra-index-url` with public registries, exposing builds to malicious package substitution. |

---

## 11. Advanced Application Protocols, CI/CD & Cloud Infrastructure (v3.5)

| Rule ID | Title | Severity | Category | CWE ID | Detection Target & Logic |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CR-HTTP-001` | HTTP Request Smuggling & Hop-by-Hop Injection | CRITICAL | SECURITY | CWE-444 | Detects ambiguous Transfer-Encoding / Content-Length headers and client-controlled hop-by-hop Connection header proxying. |
| `CR-PROTO-001` | Server-Side Prototype Pollution | HIGH | SECURITY | CWE-1321 | Detects recursive Object.assign, lodash merge, or JSON revivers accepting `__proto__` / `constructor` properties, risking RCE. |
| `CR-OAUTH-001` | OAuth2 Open Redirect & Missing PKCE | HIGH | SECURITY | CWE-601 | Detects OAuth2 authorization requests missing code_challenge (PKCE) or utilizing dynamic client-supplied redirect URIs. |
| `CR-DESER-002` | Polyglot Insecure Deserialization (Python/Node) | CRITICAL | SECURITY | CWE-502 | Detects dangerous Python `pickle.loads` or `yaml.load` without SafeLoader, and Node.js `node-serialize` unserialize. |
| `CR-CORS-002` | CORS Origin Reflection & Credential Leak | HIGH | SECURITY | CWE-942 | Detects CORS configurations reflecting arbitrary request Origin headers combined with `Allow-Credentials: true` or unanchored regexes. |
| `CR-K8S-002` | Kubernetes Secrets in Environment Variables | MEDIUM | SECURITY | CWE-526 | Detects container manifests mapping secrets into environment variables via `secretKeyRef` or `secretRef`, risking subprocess exposure. |
| `CR-IAC-009` | Dockerfile SUID Escalation & Sudo Installation | HIGH | SECURITY | CWE-250 | Detects container builds adding SUID/SGID bits (`chmod +s`, `4755`) or installing the `sudo` package inside images. |
| `CR-IAC-010` | Terraform RDS Unencrypted & KMS Rotation Disabled | HIGH | SECURITY | CWE-311 | Detects Terraform `aws_db_instance` without `storage_encrypted = true`, publicly accessible RDS, or disabled KMS key rotation. |
| `CR-IAC-011` | S3 Public Website & CloudFront Insecure Protocol | HIGH | SECURITY | CWE-319 | Detects CloudFront distributions allowing plain HTTP (`allow-all`) and public-read S3 bucket configurations. |
| `CR-CICD-001` | CI/CD Pipeline Context Script Injection | CRITICAL | SECURITY | CWE-94 | Detects unescaped `${{ github.event... }}` context interpolations inside workflow `run:` shell steps. |
| `CR-ARCH-002` | Architectural Layer Boundary Inversion | MEDIUM | QUALITY | CWE-1061 | Detects presentation Controllers bypassing domain Services to directly inject or invoke database Repositories/DAOs. |
| `CR-THREAD-001` | ThreadLocal Context Leakage & Missing Cleanup | HIGH | SECURITY | CWE-404 | Detects ThreadLocal variables in thread-pooled web containers lacking guaranteed `.remove()` invocation inside finally blocks. |


