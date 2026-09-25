# Codexa MITRE CWE Top 25 Coverage Matrix

This specification details the comprehensive mapping between **Codexa's static analysis rules** and the **MITRE CWE Top 25 Most Dangerous Software Weaknesses**, including detection heuristics, AST inspection strategies, severity classifications, and compliance audit guidelines.

---

## 1. Executive Summary & Methodology

The MITRE Common Weakness Enumeration (CWE) Top 25 represents the most widespread and critical security vulnerabilities plaguing contemporary software systems. Codexa provides native static detection coverage for CWE weaknesses through deterministic AST node inspection, taint flow heuristics, and lexical Shannon entropy analysis.

### Evaluation Criteria:
- **Direct AST Match**: Detection via concrete syntax tree inspection (e.g., detecting `Runtime.getRuntime().exec()` method invocations).
- **Entropy & Heuristic Match**: Detection via Shannon entropy, pattern recognition, and string token extraction.
- **Universal Multi-Language Scanner**: Detection across Python, JavaScript, TypeScript, Go, and configuration manifests (YAML, Dockerfile).

---

## 2. Complete MITRE CWE Top 25 Coverage Table

| Rank | CWE ID | Weakness Name | Codexa Rule ID | Severity | Default Confidence | AST / Detection Mechanism |
|:---:|:---|:---|:---|:---:|:---:|:---|
| **1** | **CWE-79** | Improper Neutralization of Input During Web Page Generation (XSS) | `CR-XSS-001` | HIGH | HIGH | Identifies unescaped user inputs passed to `HttpServletResponse.getWriter().write()` or unescaped React JSX DOM injection (`dangerouslySetInnerHTML`). |
| **2** | **CWE-89** | Improper Neutralization of Special Elements in SQL Command (SQLi) | `CR-SQL-001` | CRITICAL | HIGH | Flags dynamic string concatenation inside `Statement.executeQuery()`, JPA entity queries, or unparameterized raw SQL clauses. |
| **3** | **CWE-78** | OS Command Injection | `CR-CMD-001` | CRITICAL | HIGH | Traverses AST for `Runtime.getRuntime().exec()`, `ProcessBuilder`, or Node `child_process.exec()` called with variable expressions. |
| **4** | **CWE-22** | Improper Limitation of a Pathname to a Restricted Directory (Path Traversal) | `CR-PATH-001` | HIGH | HIGH | Inspects `new File(base, userInput)` and `Path.resolve()` lacking `.normalize().startsWith()` containment checks. |
| **5** | **CWE-352** | Cross-Site Request Forgery (CSRF) | `CR-CSRF-001` | MEDIUM | HIGH | Analyzes Spring Security filter chain configurations for `http.csrf().disable()` or missing anti-CSRF token verification on state-mutating HTTP methods. |
| **6** | **CWE-434** | Unrestricted Upload of File with Dangerous Type | `CR-PATH-001` | HIGH | MEDIUM | Evaluates multipart file handlers lacking MIME-type whitelisting or destination filename sanitization. |
| **7** | **CWE-306** | Missing Authentication for Critical Function | `CR-AUTH-001` | CRITICAL | HIGH | Flags public REST controllers (`@RestController`, `@RequestMapping`) that lack `@PreAuthorize`, `@Secured`, or authenticated security matcher rules. |
| **8** | **CWE-502** | Deserialization of Untrusted Data | `CR-DESER-001` | CRITICAL | HIGH | Discovers invocations of `ObjectInputStream.readObject()`, XMLDecoder, or unsafe `Yaml.load()` without type filtering. |
| **9** | **CWE-798** | Use of Hard-coded Credentials | `CR-SECRET-001` | CRITICAL | HIGH | Analyzes string literals using Shannon entropy calculation ($H > 4.5$) combined with secret regex patterns (AWS, GitHub, Slack tokens). |
| **10** | **CWE-287** | Improper Authentication | `CR-AUTH-001` / `CR-JWT-001` | HIGH | HIGH | Detects disabled JWT signature verification (`algorithm: none`) and empty authentication provider filters. |
| **11** | **CWE-476** | NULL Pointer Dereference | `CR-QUAL-006` | MEDIUM | MEDIUM | Evaluates unvalidated optional unwrap operations and missing null-checks on external DTO boundaries. |
| **12** | **CWE-862** | Missing Authorization | `CR-AUTH-001` | HIGH | HIGH | Flags tenant-scoped repository access without user identity or tenancy predicate binding. |
| **13** | **CWE-77** | Command Injection (General) | `CR-CMD-001` | CRITICAL | HIGH | Identifies command array construction with concatenated arguments passed to system shells. |
| **14** | **CWE-918** | Server-Side Request Forgery (SSRF) | `CR-SSRF-001` | HIGH | HIGH | Audits `HttpClient`, `RestTemplate`, and `WebClient` URI builders constructed from raw request parameters without IP/host whitelisting. |
| **15** | **CWE-119** | Improper Restriction of Operations within Bounds of a Memory Buffer | `CR-PERF-001` | HIGH | HIGH | Inspects memory buffer allocations, array slicing, and off-heap direct byte buffer allocations in Go/Java. |
| **16** | **CWE-327** | Use of a Broken or Risky Cryptographic Algorithm | `CR-CRYPTO-001` | HIGH | HIGH | Traverses `Cipher.getInstance()` arguments matching `DES`, `3DES`, `Blowfish`, `RC4`, or ECB mode (`AES/ECB/PKCS5Padding`). |
| **17** | **CWE-328** | Use of Weak Hash Algorithm | `CR-HASH-001` | HIGH | HIGH | Discovers `MessageDigest.getInstance("MD5")` and `MessageDigest.getInstance("SHA-1")` used in security-sensitive contexts. |
| **18** | **CWE-338** | Use of Cryptographically Weak Pseudo-Random Number Generator | `CR-RAND-001` | MEDIUM | HIGH | Flags `java.util.Random` or `Math.random()` used where `java.security.SecureRandom` is required (token generation, salt, IV). |
| **19** | **CWE-916** | Use of Password Hash With Insufficient Computational Effort | `CR-PASS-001` | HIGH | HIGH | Detects password hashing using fast digests (MD5, SHA-256) instead of adaptive KDFs (BCrypt, Argon2id, PBKDF2). |
| **20** | **CWE-942** | Overly Permissive Cross-Domain Whitelist (CORS) | `CR-CORS-001` | MEDIUM | HIGH | Flags `@CrossOrigin(origins = "*")` combined with `allowCredentials = true` in Spring Web endpoints. |
| **21** | **CWE-1321**| Improper Control of Generation of Code ('Prototype Pollution') | `CR-PARAM-001` | HIGH | HIGH | Identifies recursive object merging or object key assignment (`obj[key] = val`) without `__proto__` / `constructor` guarding. |
| **22** | **CWE-532** | Insertion of Sensitive Information into Log File | `CR-LOG-001` | MEDIUM | HIGH | Detects logging of variables containing sensitive identifiers (`password`, `ssn`, `creditCard`, `token`, `secret`). |
| **23** | **CWE-295** | Improper Certificate Validation | `CR-CONFIG-001`| HIGH | HIGH | Flags custom `TrustManager` implementations that swallow certificate verification or `HostnameVerifier` returning `true`. |
| **24** | **CWE-400** | Uncontrolled Resource Consumption | `CR-PERF-001` / `CR-QUAL-001` | MEDIUM | HIGH | Identifies high cyclomatic complexity ($CC > 25$), unbounded loop allocations, and un-pooled thread instantiations. |
| **25** | **CWE-639** | Authorization Bypass Through User-Controlled Key (IDOR) | `CR-RLS-001` | HIGH | HIGH | Flags direct database lookups using request path parameters without tenant or ownership verification predicates. |

---

## 3. High-Priority AST Detection Heuristics

### A. SQL Injection (`CWE-89` / `CR-SQL-001`)
```
AST Search:
  MethodCallExpr(name: "executeQuery" | "executeUpdate" | "createNativeQuery")
  Argument: BinaryExpr(operator: PLUS) OR StringLiteral formatted with `%s`
Heuristic:
  If argument contains non-literal variable references and no PreparedStatement parameter binding ('?'),
  emit CRITICAL finding.
```

### B. Hardcoded Secrets & Credentials (`CWE-798` / `CR-SECRET-001`)
```
AST Search:
  VariableDeclarator(name ~= "password|token|secret|apiKey|accessKey", init: StringLiteral)
Heuristic:
  Compute Shannon Entropy: H = - sum( p(x) * log2(p(x)) )
  If H > 4.5 and length >= 16, emit CRITICAL finding.
```

### C. Server-Side Request Forgery (`CWE-918` / `CR-SSRF-001`)
```
AST Search:
  MethodCallExpr(scope ~= "restTemplate|webClient|httpClient", name: "getForObject|exchange|uri")
Heuristic:
  If URI argument originates from method parameter without invocation of `InetAddress` validation
  or whitelist check, emit HIGH finding.
```

---

## 4. Compliance Auditing & Export Integration

When exporting analysis results to external auditors or compliance platforms:
1. **SARIF Taxonomy Relationship**: Every result contains a relationship pointer linking back to the official MITRE taxonomy:
   ```json
   "relationships": [
     {
       "target": {
         "id": "CWE-89",
         "toolComponent": {
           "name": "CWE"
         }
       }
     }
   ]
   ```
2. **Executive Summary Rollup**: The audit report provides a dedicated CWE Top 25 compliance section indicating which percentage of top weaknesses were audited and certified clean.
