# Rule Specification: CR-CSRF-001 (Disabled CSRF Protection)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-CSRF-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-352: Cross-Site Request Forgery (CSRF)](https://cwe.mitre.org/data/definitions/352.html) |
| **OWASP Top 10** | A05:2021 — Security Misconfiguration |
| **Target Scope** | Java AST (Spring Security), Django, Express (csurf), Rails |

---

## 1. Vulnerability Overview

Cross-Site Request Forgery (CSRF) allows malicious third-party websites to exploit ambient browser credentials (such as cookies or HTTP basic auth headers) to execute unwanted, unauthorized state-changing actions (e.g. transfers, password updates, deletions) on behalf of an authenticated victim.

Disabling CSRF protection (`csrf.disable()` in Spring Security) is a common anti-pattern introduced during development to bypass 403 Forbidden errors during API testing. While stateless REST APIs utilizing `Authorization: Bearer <token>` headers are immune to browser ambient credential replay, session-based cookie-authenticated applications become completely vulnerable when CSRF protection is removed.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**: Invocations of `http.csrf().disable()` or `http.csrf(AbstractHttpConfigurer::disable)` in Spring Security configuration classes.
- **Context Evaluation**: Evaluates whether session management is configured as `SessionCreationPolicy.STATELESS` (stateless API) vs `IF_REQUIRED` / cookie-based session tracking. If cookie authentication or browser endpoints are present, a `HIGH` finding is raised.

---

## 3. Vulnerable Code Example

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // VULNERABLE: Globally disabling CSRF leaves cookie-authenticated routes vulnerable
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }
}
```

---

## 4. Secure Remediation

### A. For Cookie-Authenticated Web Applications
Enable CSRF with a secure cookie repository using double-submit cookies or standard synchronized token patterns:

```java
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // SECURE: Enforces CSRF tokens with HttpOnly=false so Single Page Apps can read and transmit header
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }
}
```

### B. For Pure Stateless Bearer Token APIs
If using purely non-browser clients or `Authorization: Bearer` JWT headers where no cookies are used for authentication:
```java
// SECURE: Stateless APIs not using ambient cookie credentials
http
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
```
And ensure cookies set `SameSite=Strict` on all authentication tokens.
