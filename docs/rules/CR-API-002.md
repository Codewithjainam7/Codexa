# CR-API-002: Unrestricted Resource Consumption & Missing Pagination

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-API-002` |
| **Category** | `OPERATIONS` / `SECURITY` |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-770: Allocation of Resources Without Limits or Throttling](https://cwe.mitre.org/data/definitions/770.html) |
| **OWASP API Top 10** | API4:2023 – Unrestricted Resource Consumption |
| **Scanner Target** | Spring `@RestController`, JAX-RS methods returning Collections |

---

## 1. Vulnerability Summary
API endpoints returning complete collections or lists (`List<T>`, `Set<T>`) directly from the database without query pagination (`Pageable`, `limit`, `offset`) allow callers to trigger full table scans. In production environments where tables grow to tens or hundreds of thousands of rows, a single unpaginated request can:
1. Exhaust JVM heap memory leading to `java.lang.OutOfMemoryError` crashes.
2. Saturate database I/O and connection pool threads, starving legitimate users.
3. Cause Denial of Service (DoS) across dependent microservices.

---

## 2. Insecure Code Example

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    // VIOLATION: Returns unbounded List of all database records
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }
}
```

---

## 3. Secure Remediation Pattern

Always enforce paginated responses with explicit page limits:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // REMEDIATION: Spring Data Pageable with bounded max size
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @PageableDefault(size = 20, maxPageSize = 100) Pageable pageable
    ) {
        Page<OrderResponse> page = orderService.getOrders(pageable);
        return ResponseEntity.ok(page);
    }
}
```

If custom query parameters are used, enforce upper bounds programmatically:
```java
int sanitizedSize = Math.min(Math.max(1, size), 100);
```

---

## 4. Verification Checklist
- [ ] No GET endpoints return raw `List<T>` without pagination limits.
- [ ] Page size parameters are strictly clamped (`maxPageSize <= 100`).
- [ ] Database repository queries utilize indexed columns for paginated sorting.
