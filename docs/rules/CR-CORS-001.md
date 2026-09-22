# Rule Specification: CR-CORS-001 (Permissive Wildcard CORS with Credentials)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-CORS-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-942: Permissive Cross-Domain Policy with Untrusted Domains](https://cwe.mitre.org/data/definitions/942.html) |
| **OWASP Top 10** | A01:2021 — Broken Access Control |
| **Target Scope** | Java AST (Spring Web MVC / WebFlux), Node.js (cors), Python (FastAPI/Flask) |

---

## 1. Vulnerability Overview

Cross-Origin Resource Sharing (CORS) is a browser mechanism that allows a server to indicate any origins other than its own from which a browser should permit loading resources.

A critical misconfiguration occurs when:
1. An application dynamically echoes the incoming `Origin` header or sets `Access-Control-Allow-Origin: *`.
2. Simultaneously enables `Access-Control-Allow-Credentials: true`.

Under this configuration, any malicious website visited by an authenticated user can perform cross-origin XMLHttpRequests or fetch calls to read private user data, authorization headers, and session cookies.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**:
  - Class/method annotations `@CrossOrigin(origins = "*", allowCredentials = "true")`.
  - Spring `CorsConfiguration.addAllowedOrigin("*")` or `.addAllowedOriginPattern("*")` paired with `.setAllowCredentials(true)`.
- **Node.js**: `cors({ origin: '*', credentials: true })` or dynamic reflection `origin: (origin, callback) => callback(null, true)`.
- **Python**: FastAPI/Starlette `CORSMiddleware(allow_origins=["*"], allow_credentials=True)`.

---

## 3. Vulnerable Code Example

```java
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // VULNERABLE: Wildcard origin pattern with credentials allows any external origin to read responses
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

---

## 4. Secure Remediation

Explicitly enumerate trusted domains from verified configuration properties:

```java
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${codexa.security.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // SECURE: Only explicitly whitelisted origins are permitted with credentials
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Correlation-ID")
                .maxAge(3600);
    }
}
```

Never reflect the HTTP `Origin` header blindly back into `Access-Control-Allow-Origin` without strict whitelist validation.
