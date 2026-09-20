# Rule: CR-QUAL-004 — Duplicated Code Blocks

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-QUAL-004` |
| **Category** | `QUALITY` |
| **Severity** | `LOW` |
| **Confidence** | `MEDIUM` |
| **Window Threshold** | $\ge 15$ continuous normalized statement shingles |

---

## 1. Description

Detects identical sequences of code containing 15 or more normalized statements occurring in multiple locations. Duplicated code ("copy-paste programming") creates substantial maintenance debt, leads to divergent bug fixes, and multiplies defect vulnerability across the codebase.

---

## 2. Shingle Normalization Algorithm

To avoid flagging boilerplate declarations, Codexa filters out the following lines prior to shingle hashing:
- Empty lines and standalone structural braces (`{`, `}`, `);`).
- Package declarations and `import` statements.
- Method and class annotations (`@Override`, `@Transactional`, etc.).
- Fluent builder chaining calls (lines starting with `.`).
- Comments and docstrings (`//`, `/*`, `*`).
- Method / record parameter declarations (lines ending with `,`).

---

## 3. Vulnerable Duplicated Example

```java
// Location 1: OrderService.java
public OrderDto buildOrder(OrderEntity entity) {
    OrderDto dto = new OrderDto();
    dto.setId(entity.getId());
    dto.setTotal(entity.getTotal());
    dto.setCustomerName(entity.getCustomer().getName());
    dto.setAddress(entity.getAddress());
    dto.setCity(entity.getCity());
    dto.setCountry(entity.getCountry());
    dto.setPostalCode(entity.getPostalCode());
    dto.setStatus(entity.getStatus().name());
    dto.setCreatedAt(entity.getCreatedAt().toString());
    dto.setUpdatedAt(entity.getUpdatedAt().toString());
    dto.setDiscount(entity.getDiscount());
    dto.setTax(entity.getTax());
    dto.setShippingCost(entity.getShippingCost());
    dto.setDeliveryNotes(entity.getDeliveryNotes());
    dto.setTrackingNumber(entity.getTrackingNumber());
    return dto;
}

// Location 2: InvoiceService.java contains identical 15-line mapping sequence!
```

---

## 4. Remediated DRY Example

```java
// SECURE & MAINTAINABLE: Single source of truth in OrderMapper.java
@Component
public class OrderMapper {
    public OrderDto toDto(OrderEntity entity) {
        if (entity == null) return null;
        // Centralized mapping logic
        ...
    }
}
```
