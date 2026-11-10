# CR-THREAD-001: ThreadLocal Context Leakage & Missing Cleanup

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-THREAD-001` |
| **Category** | Security & Reliability |
| **Severity** | High |
| **Confidence** | High |
| **CWE** | [CWE-404: Improper Resource Shutdown or Release](https://cwe.mitre.org/data/definitions/404.html) |
| **OWASP Top 10** | A04:2021 - Insecure Design |

---

## Description
Java web containers (Tomcat, Jetty, Undertow) maintain thread pools to handle incoming HTTP requests. When a thread finishes processing a request, it is returned to the pool without clearing its `ThreadLocal` storage. If an application stores tenant information, authentication tokens, or user security contexts in `ThreadLocal` without calling `.remove()`, a subsequent request processed by the same thread can access or mutate the previous user's credentials and tenant data, leading to severe cross-tenant security vulnerabilities.

---

## Non-Compliant Code Example
```java
public class UserContextHolder {
    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

    public static void set(UserContext user) {
        context.set(user);
    }

    public static UserContext get() {
        return context.get();
    }
    // Missing remove() cleanup!
}
```

---

## Compliant Solution
```java
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        try {
            UserContextHolder.set(resolveContext(req));
            chain.doFilter(req, res);
        } finally {
            UserContextHolder.clear(); // Always clean up in finally!
        }
    }
}
```
