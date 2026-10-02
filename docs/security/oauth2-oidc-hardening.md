# OAuth2 & OpenID Connect (OIDC) Enterprise Hardening Guide

## 1. Overview
Codexa enforces strict validation of OAuth 2.0 and OpenID Connect (OIDC) flows to prevent token forgery, authorization code interception, and privilege escalation.

---

## 2. Threat Vectors & Defenses

### A. Authorization Code Interception (RFC 7636 PKCE)
* **Threat**: Malicious applications or network eavesdroppers intercept the authorization code returned from the `/authorize` endpoint.
* **Mitigation**: Require Proof Key for Code Exchange (PKCE) with `code_challenge_method=S256` across all public and confidential clients. Reject plain SHA-1 or unhashed code challenges.

### B. State Parameter & CSRF Protection
* **Threat**: Attacker initiates an authorization flow and tricks victim into binding attacker's account to victim session.
* **Mitigation**: Generate cryptographically random, high-entropy `state` parameters bound to the user's browser session with short TTLs (<= 5 minutes).

### C. Open Redirect on Redirect URI
* **Threat**: Attacker manipulates `redirect_uri` parameter to redirect authorization codes to an attacker-controlled host.
* **Mitigation**: Enforce exact string matching against pre-registered, immutable whitelist URLs. Disallow wildcard subdomains or path traversal parameters.

---

## 3. Spring Security 6 / Spring Boot 3 Configuration

```java
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {

    @Bean
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestResolver(pkceResolver())
                )
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtConverter()))
            );
        return http.build();
    }
}
```

---

## 4. Verification Checklist
- [ ] PKCE S256 is enforced on all OAuth authorization endpoints.
- [ ] Strict exact-match URI whitelisting is active in client registries.
- [ ] ID and Access tokens are signed using RS256 or ES256 (never symmetric HS256 with shared keys).
