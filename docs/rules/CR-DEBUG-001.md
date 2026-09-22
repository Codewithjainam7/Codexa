# Rule Specification: CR-DEBUG-001 (Exposed Debug & Actuator Endpoints)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-DEBUG-001` |
| **Category** | Security |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-489: Active Debug Code in Production](https://cwe.mitre.org/data/definitions/489.html) / [CWE-200: Exposure of Sensitive Information](https://cwe.mitre.org/data/definitions/200.html) |
| **OWASP Top 10** | A05:2021 — Security Misconfiguration |
| **Target Scope** | Spring Boot Configuration (`application.yml`, `application.properties`), Dockerfiles |

---

## 1. Vulnerability Overview

Exposing administrative and diagnostic endpoints in production environments without authentication reveals critical runtime state. In Spring Boot applications, exposing endpoints such as `/actuator/env`, `/actuator/heapdump`, `/actuator/threaddump`, or the embedded H2 database console (`/h2-console`) allows attackers to extract:
- Environment variables containing plaintext database credentials and cloud API keys (`/actuator/env`).
- Full JVM process memory dumps containing user passwords and decryption keys (`/actuator/heapdump`).
- Arbitrary SQL execution or JNDI injection via unauthenticated web consoles (`/h2-console`).

---

## 2. Detection Logic & Configuration Parsing

Codexa analyzes configuration files (`application*.yml`, `application*.properties`):
1. **Actuator Exposure**: Checks for wildcard exposures:
   - `management.endpoints.web.exposure.include: "*"`
   - Inclusion of high-risk endpoints: `env`, `heapdump`, `threaddump`, `beans`, `mappings`, `shutdown`.
2. **H2 Console Exposure**: Checks for `spring.h2.console.enabled: true` in production profiles.
3. **Debug Flag**: Checks for `debug: true` or verbose tracing flags active in production profiles.

---

## 3. Vulnerable Configuration Example

```yaml
# VULNERABLE: Wildcard exposure leaks environment variables and memory dumps
management:
  endpoints:
    web:
      exposure:
        include: "*"

spring:
  h2:
    console:
      enabled: true # VULNERABLE: Web database console active
```

---

## 4. Secure Remediation

Explicitly restrict Actuator web exposure to non-sensitive health probes, and disable development consoles:

```yaml
# SECURE: Only liveness and readiness health probes are exposed publicly
management:
  endpoints:
    web:
      exposure:
        include: health, info
  endpoint:
    health:
      show-details: when-authorized
      roles: SYSTEM_ADMIN

spring:
  h2:
    console:
      enabled: false
```

For administrative monitoring, isolate management traffic on a dedicated internal port (e.g. `management.server.port: 8081`) blocked from external ingress.
