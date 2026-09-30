# CR-API-003: Broken Function Level Authorization (BFLA)

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-API-003` |
| **Category** | `SECURITY` |
| **Default Severity** | `CRITICAL` |
| **CWE Mapping** | [CWE-285: Improper Authorization](https://cwe.mitre.org/data/definitions/285.html) |
| **OWASP API Top 10** | API5:2023 – Broken Function Level Authorization (BFLA) |
| **Scanner Target** | Spring `@RestController`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` |

---

## 1. Vulnerability Summary
Broken Function Level Authorization (BFLA) occurs when sensitive state-mutating endpoints (e.g. creating administrative users, modifying billing tiers, deleting audit logs, or purging database tables) are exposed without explicit role-based or permission-based access controls (`@PreAuthorize`, `@Secured`, or SecurityFilterChain matcher rules). An authenticated regular user can forge HTTP requests to administrative paths (e.g. `POST /api/v1/admin/users`) and successfully execute privileged business functions.

---

## 2. Insecure Code Example

```java
@RestController
@RequestMapping("/api/v1/system")
public class SystemAdminController {

    @Autowired
    private SystemConfigurationService configService;

    // VIOLATION: Mutating administrative action without @PreAuthorize or role verification
    @DeleteMapping("/reset-tenant/{tenantId}")
    public ResponseEntity<Void> purgeTenantData(@PathVariable String tenantId) {
        configService.purgeTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 3. Secure Remediation Pattern

Protect administrative and state-mutating endpoints with method-level authorization constraints:

```java
@RestController
@RequestMapping("/api/v1/system")
public class SystemAdminController {

    @Autowired
    private SystemConfigurationService configService;

    // REMEDIATION: Explicit role and authority check via Spring Security SpEL
    @DeleteMapping("/reset-tenant/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('TENANT_PURGE')")
    public ResponseEntity<Void> purgeTenantData(@PathVariable String tenantId) {
        configService.purgeTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

Ensure `@EnableMethodSecurity(prePostEnabled = true)` is declared on the security configuration class:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // SecurityFilterChain definition
}
```

---

## 4. Verification Checklist
- [ ] All `@PostMapping`, `@PutMapping`, `@PatchMapping`, and `@DeleteMapping` endpoints declare `@PreAuthorize`.
- [ ] Administrative sub-paths (`/admin/**`, `/manage/**`, `/system/**`) require administrative scopes in `SecurityFilterChain`.
- [ ] Unit and integration tests verify `403 FORBIDDEN` when regular users access privileged endpoints.
