# CR-ARCH-002: Architectural Layer Boundary Inversion

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-ARCH-002` |
| **Category** | Quality & Maintainability |
| **Severity** | Medium |
| **Confidence** | High |
| **CWE** | [CWE-1061: Insufficient Encapsulation](https://cwe.mitre.org/data/definitions/1061.html) |
| **Standard** | Clean Architecture / Hexagonal Architecture / DDD |

---

## Description
In multi-tiered and domain-driven architectures, Controllers belong strictly to the Presentation/Interface layer. Controllers must not directly interact with database persistence constructs (`UserRepository`, `OrderDao`, etc.). Doing so bypasses business validation, transaction boundaries, audit logs, and security checks implemented inside the domain Service layer.

---

## Non-Compliant Code Example
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository; // VIOLATION: Direct DAO coupling

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

---

## Compliant Solution
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
```
