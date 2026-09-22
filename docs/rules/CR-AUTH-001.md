# Rule Specification: CR-AUTH-001 (Missing Access Control & Unauthenticated Endpoints)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-AUTH-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-306: Missing Authentication for Critical Function](https://cwe.mitre.org/data/definitions/306.html) / [CWE-862: Missing Authorization](https://cwe.mitre.org/data/definitions/862.html) |
| **OWASP Top 10** | A01:2021 — Broken Access Control |
| **Target Scope** | Java AST (Spring Web MVC / WebFlux), Polyglot Controller Endpoints |

---

## 1. Vulnerability Overview

Exposing web controller routes or API endpoints that perform mutating or privileged operations without proper authentication and role-based access control (RBAC) allows unauthenticated external actors to manipulate data, trigger administrative procedures, or access restricted information.

In Spring Boot applications, controllers marked with `@RestController` or `@Controller` often expose mutating routes (`@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`) that rely solely on application perimeter filters without method-level access controls (`@PreAuthorize`, `@Secured`, `@RolesAllowed`).

---

## 2. Detection Logic & AST Matching

Codexa's AST parser inspects class-level and method-level declarations:
1. Identifies classes annotated with `@RestController`, `@Controller`, or polyglot route handlers (Express, FastAPI, Gin).
2. Traverses all declared methods annotated with request mapping annotations (`@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@RequestMapping(method = RequestMethod.POST)`).
3. Evaluates if neither class-level nor method-level annotations contain:
   - `@PreAuthorize("hasRole(...)")` or `@PreAuthorize("isAuthenticated()")`
   - `@Secured(...)`
   - `@RolesAllowed(...)`
   - Framework security filters (e.g. `SecurityRequirement` in OpenAPI specs)
4. Excludes public registration/login endpoints matching naming heuristics (`login`, `register`, `signup`, `authenticate`, `health`, `callback`, `webhook`, `public`).

---

## 3. Vulnerable Code Example

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // VULNERABLE: No authorization check on sensitive administrative endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. Secure Remediation

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // SECURE: Enforces strict role-based access control
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 5. Defense-in-Depth Best Practices

1. **Global SecurityFilterChain Configuration**: Enforce `anyRequest().authenticated()` by default in Spring Security configuration.
2. **Method Security Enablement**: Include `@EnableMethodSecurity(prePostEnabled = true)` to activate method-level expression authorization.
3. **Multi-Tenant Scoping**: Ensure authorization rules verify tenant boundaries to prevent Insecure Direct Object References (IDOR).
