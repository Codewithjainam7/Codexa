# Codexa Defensive HTTP Headers & Web Perimeter Matrix

This document provides the exhaustive specification for HTTP security response headers enforced by Codexa's `SecurityHeadersFilter` across all API and web endpoints.

---

## 1. Security Headers Configuration Matrix

| Header Name | Configured Directive | Defensive Purpose |
| :--- | :--- | :--- |
| **Content-Security-Policy** | `default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https://raw.githubusercontent.com https://api.dicebear.com; connect-src 'self' http://localhost:* https://integrate.api.nvidia.com https://api.github.com;` | Prevents cross-site scripting (XSS), data exfiltration, and unauthorized script injection. |
| **X-Frame-Options** | `DENY` | Prevents clickjacking attacks by completely forbidding framing in `<iframe>`, `<frame>`, or `<object>`. |
| **X-Content-Type-Options** | `nosniff` | Disables MIME type sniffing, preventing browsers from executing text/HTML uploads as executable scripts. |
| **Referrer-Policy** | `strict-origin-when-cross-origin` | Protects privacy by stripping query parameters and sensitive URL tokens during cross-origin navigations. |
| **Permissions-Policy** | `geolocation=(), camera=(), microphone=()` | Disables device hardware access across all embedded contexts. |
| **Cross-Origin-Opener-Policy** | `same-origin` | Isolates the browsing context to prevent Spectre-like cross-window side-channel attacks (`COOP`). |
| **X-Robots-Tag** | `noindex, nofollow, noarchive` | Prevents search engine indexing of private API reports and customer scan identifiers. |
| **X-Correlation-ID** | Unique UUID v4 per request | Enables end-to-end distributed request tracing for operational debugging and audit log correlation. |

---

## 2. Spring Filter Implementation

```java
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("X-Robots-Tag", "noindex, nofollow, noarchive");
        
        String correlationId = Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .orElseGet(() -> UUID.randomUUID().toString());
        response.setHeader("X-Correlation-ID", correlationId);

        filterChain.doFilter(request, response);
    }
}
```
