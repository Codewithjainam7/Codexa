# Cross-Site Request Forgery (CSRF) Modern SPA Defense

## 1. Overview
Cross-Site Request Forgery tricks an authenticated victim's browser into executing unauthorized actions on a trusted web application.

---

## 2. Modern Defense Strategy

Codexa utilizes a multi-layered defense model:
1. **Stateless Authorization Tokens**: Primary APIs require `Authorization: Bearer <JWT>` headers. Browsers do not automatically attach Bearer headers across cross-site requests.
2. **`SameSite=Lax` or `SameSite=Strict` Cookie Attributes**: Ensures session and refresh cookies are not transmitted on cross-origin POST or state-mutating requests.
3. **Double Submit Cookie Pattern**: For cookie-authenticated endpoints, the server sets a cryptographic random CSRF cookie and validates that incoming requests contain matching `X-XSRF-TOKEN` headers.

---

## 3. Spring Security Implementation

```java
http
    .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
    );
```

---

## 4. Verification Checklist
- [ ] Sensitive session cookies declare `SameSite=Lax; Secure; HttpOnly`.
- [ ] State-mutating methods (`POST`, `PUT`, `DELETE`) require custom headers (`X-Requested-With` or `X-XSRF-TOKEN`).
- [ ] No state changes are triggered via HTTP `GET` requests.
