# Cross-Origin Resource Sharing (CORS) Security Architecture

## 1. Overview
Cross-Origin Resource Sharing (CORS) defines browser rules for allowing web applications running at one domain to access resources hosted on another domain.

---

## 2. High-Risk Misconfigurations

### Wildcard Origin with Credentials
```http
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
```
*Browsers block this, but custom API clients and weak reverse proxies may mishandle it.*

### Dynamic Reflection of Origin Header
Reflecting the incoming `Origin` request header back in `Access-Control-Allow-Origin` allows any malicious website to steal authenticated data via standard `fetch()` or `XMLHttpRequest`.

---

## 3. Production Configuration Pattern

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(
        "https://codexa-ye85.onrender.com",
        "https://app.codexa.dev"
    ));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
}
```

---

## 4. Verification Checklist
- [ ] No endpoints reflect untrusted `Origin` headers into `Access-Control-Allow-Origin`.
- [ ] Null origins (`Origin: null`) are rejected.
- [ ] Pre-flight `OPTIONS` requests are cached with appropriate `Max-Age`.
