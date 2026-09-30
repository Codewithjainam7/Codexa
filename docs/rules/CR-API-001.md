# CR-API-001: Mass Assignment & Broken Object Property Authorization (BOPLA)

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-API-001` |
| **Category** | `SECURITY` |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-915: Improperly Controlled Modification of Dynamically-Determined Object Attributes](https://cwe.mitre.org/data/definitions/915.html) |
| **OWASP API Top 10** | API3:2023 – Broken Object Property Level Authorization (BOPLA) |
| **Scanner Target** | Spring `@RestController`, JAX-RS, Express route handlers |

---

## 1. Vulnerability Summary
Mass Assignment occurs when an API endpoint binds incoming JSON or form parameters directly to persistent database entities (e.g. `@Entity` classes) or domain models without an explicit, restricted Data Transfer Object (DTO) whitelist. Attackers exploit this by injecting sensitive payload attributes that they should not have permission to alter, such as:
- `role: "ADMIN"` or `permissions: ["ALL"]`
- `isVerified: true` or `emailVerified: true`
- `balance: 999999` or `credits: 1000`
- `tenantId` / `organizationId` (leading to Multi-Tenant isolation breach)

---

## 2. Insecure Code Example

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // VIOLATION: Directly deserializing request body into the database entity
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User incomingUser) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUsername(incomingUser.getUsername());
        // If incomingUser contains isAdmin=true, user becomes admin!
        user.setAdmin(incomingUser.isAdmin());
        return ResponseEntity.ok(userRepository.save(user));
    }
}
```

---

## 3. Secure Remediation Pattern

Always enforce strict DTO (Data Transfer Object) separation. The DTO must only expose fields that the caller is explicitly permitted to mutate, annotated with bean validation constraints:

```java
public record UpdateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @Email String email
) {}

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // REMEDIATION: Accept validated DTO, never accept raw DB Entity
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }
}
```

---

## 4. Verification Checklist
- [ ] No `@RestController` methods accept `@Entity` types as `@RequestBody` parameters.
- [ ] Dedicated request records/classes define strictly allowed fields with `@Valid` constraints.
- [ ] Internal administrative properties (`isAdmin`, `role`, `status`) can only be modified through dedicated, permission-gated administrative endpoints (`@PreAuthorize("hasRole('ADMIN')")`).
