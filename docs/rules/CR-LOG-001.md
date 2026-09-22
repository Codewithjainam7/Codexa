# Rule Specification: CR-LOG-001 (Sensitive Data & Credential Logging)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-LOG-001` |
| **Category** | Security |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-532: Insertion of Sensitive Information into Log File](https://cwe.mitre.org/data/definitions/532.html) |
| **OWASP Top 10** | A09:2021 — Security Logging and Monitoring Failures |
| **Target Scope** | Java AST (SLF4J, Logback, Log4j2), Python (logging), Node.js (Winston, Pino) |

---

## 1. Vulnerability Overview

Writing sensitive information—such as user passwords, credit card numbers, JWT bearer tokens, cloud secret keys, or PII (SSNs, medical records)—to application log streams exposes credentials to unauthorized internal employees, compromised log aggregators (e.g. Datadog, Splunk, ElasticSearch), and third-party monitoring vendors.

However, a naive regex check looking for the substring `"token"` frequently creates **false positives** in modern AI and NLP codebases where terms like `"estimated_tokens"`, `"prompt_tokens"`, or `"total tokens: 500"` describe LLM context metrics rather than cryptographic credentials.

---

## 2. Detection Logic & Context Heuristics

Codexa checks:
1. Invocations of logging methods (`log.info`, `log.debug`, `log.warn`, `app_logger.info`, `logger.log`) where logged arguments match credential variable names:
   - `password`, `pwd`, `passphrase`, `cvv`, `creditCard`
   - `bearerToken`, `accessToken`, `refreshToken`, `apiKey`, `clientSecret`
2. **AI & Metric Exclusion Heuristics**:
   - Excludes LLM metric patterns: `estimated_tokens`, `prompt_tokens`, `completion_tokens`, `total_tokens`, `tokens/sec`.
   - Excludes statements where `"token"` is preceded by numerical count indicators (e.g. `"with {package.total_estimated_tokens} tokens"`).

---

## 3. Vulnerable Code Example

```java
// VULNERABLE: Logging bearer token into log stream exposes customer session secret
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    AuthResponse response = authService.authenticate(req);
    log.info("User {} authenticated successfully with token: {}", req.getUsername(), response.getAccessToken());
    return ResponseEntity.ok(response);
}
```

---

## 4. Secure Remediation

Redact or mask sensitive credentials prior to logging:

```java
import com.codexa.security.hardening.SecretMasker;

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    AuthResponse response = authService.authenticate(req);
    // SECURE: Audit event records success without leaking bearer secret
    log.info("User {} authenticated successfully. Session ID: {}", 
             req.getUsername(), response.getSessionId());
    return ResponseEntity.ok(response);
}
```

### Logback Masking Pattern
Configure masking converters in `logback-spring.xml` using regex pattern replacers to sanitize credit card and token formats automatically at the logging framework level.
