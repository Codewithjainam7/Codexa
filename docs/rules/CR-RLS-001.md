# Rule Specification: CR-RLS-001 (Insecure Direct Object Reference & Broken RLS)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-RLS-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-639: Authorization Bypass Through User-Controlled Key](https://cwe.mitre.org/data/definitions/639.html) |
| **OWASP Top 10** | A01:2021 — Broken Access Control |
| **Target Scope** | Java AST (Spring Data Repositories), SQL, Multi-Tenant Services |

---

## 1. Vulnerability Overview

Insecure Direct Object References (IDOR) occur when an application accepts a user-supplied object identifier (e.g. database ID, file path, record UUID) to retrieve, update, or delete sensitive data without verifying whether the authenticated caller owns or has permission to access that specific entity.

In multi-tenant SaaS environments, failing to filter queries by `tenant_id` or `user_id` allows users in Tenant A to read or overwrite records belonging to Tenant B simply by enumerating sequential or guessed record IDs.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Repository Queries**: Controller methods accepting a `@PathVariable` or request ID and invoking `.findById(id)` without filtering by caller context (`user_id`, `organization_id`, or `tenant_id`).
- **Entity Definitions**: Multi-tenant entities lacking tenant foreign keys or database-level Row-Level Security (RLS) policies.
- **SQL / JPQL**: Native queries selecting entities solely by primary key (`SELECT * FROM documents WHERE id = :id`) in authenticated user-facing endpoints.

---

## 3. Vulnerable Code Example

```java
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    // VULNERABLE: Direct lookup by ID allows User A to view User B's invoices
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(invoice);
    }
}
```

---

## 4. Secure Remediation

Always enforce ownership verification in repository lookups:

```java
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    // SECURE: Enforces tenant/user ownership constraint in query criteria
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Invoice invoice = invoiceRepository.findByIdAndOwnerId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
        return ResponseEntity.ok(invoice);
    }
}
```

### Database Row-Level Security (PostgreSQL)
In PostgreSQL, define declarative RLS policies:
```sql
ALTER TABLE invoices ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON invoices
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```
