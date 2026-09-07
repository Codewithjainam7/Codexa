# CR-SEC-006: Cross-Site Request Forgery & State Mutation in Safe GET Endpoints

### Overview
Flags Spring HTTP GET endpoints that perform database mutations, deletions, or state-changing operations, violating RFC 7231 idempotency semantics and enabling Cross-Site Request Forgery (CSRF).

### Classification
- **Category:** SECURITY
- **Severity:** HIGH
- **OWASP Top 10:** A01:2021-Broken Access Control
- **CWE:** CWE-352 (Cross-Site Request Forgery)

### AST Pattern Detection Details
The rule inspects:
1. `MethodDeclaration` annotated with `@GetMapping` that calls state-mutating methods (`delete`, `deleteAll`, `deleteById`, `save`, `insert`, `update`).
2. Security configuration methods explicitly calling `csrf().disable()`.

### Remediation
Ensure state modifications are mapped to `@PostMapping`, `@PutMapping`, or `@DeleteMapping`, and enforce CSRF token verification for browser sessions:
```java
@PostMapping("/users/{id}/delete")
public ResponseEntity<?> deleteUser(@PathVariable String id) {
    service.deleteUser(id);
    return ResponseEntity.noContent().build();
}
```
