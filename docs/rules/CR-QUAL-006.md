# Rule: CR-QUAL-006 — Empty Catch Block & Swallowed Exceptions

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-QUAL-006` |
| **Category** | `QUALITY` |
| **Severity** | `MEDIUM` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-390: Detection of Error Condition Without Action](https://cwe.mitre.org/data/definitions/390.html) |
| **OWASP Top 10** | [A09:2021 - Security Logging and Monitoring Failures](https://owasp.org/Top10/2021/A09_2021-Security_Logging_and_Monitoring_Failures/) |

---

## 1. Description

Detects `catch` blocks that silently suppress caught exceptions without logging contextual details or re-throwing a domain-specific error. Swallowing exceptions masks critical runtime failures, database deadlocks, network timeouts, and authentication errors, causing applications to fail in unpredictable states without diagnostic audit trails.

---

## 2. Intentional Error Suppression Conventions

Codexa recognizes standard language idioms for intentional exception ignoring and does NOT flag them as defects:
- **Java**: Catch clauses naming the parameter `ignored`, `expected`, or starting with `_` (e.g. `catch (IOException ignored) { }`).
- **JavaScript / TypeScript**: Unused parameter convention `catch (_) {}`, `catch (_e) {}`, `catch (_err) {}`, or optional catch binding `catch {}`.
- **Python**: Explicit `except Exception as ignored: pass`.

---

## 3. Vulnerable Code Example (Java)

```java
public void updateCustomerProfile(Customer customer) {
    try {
        customerRepository.save(customer);
    } catch (Exception e) {
        // VULNERABLE: Silent error swallowing prevents incident triage
    }
}
```

---

## 4. Remediated Code Example (Java)

```java
public void updateCustomerProfile(Customer customer) {
    try {
        customerRepository.save(customer);
    } catch (DataAccessException e) {
        // SECURE: Structured contextual logging with stack trace preserved
        log.error("Failed to update customer profile for ID {}: {}", customer.getId(), e.getMessage(), e);
        throw new ServiceException("Database update failed for customer: " + customer.getId(), e);
    }
}
```

---

## 5. Defensive Exception Handling Checklist

1. Always preserve the root cause exception when re-throwing (`throw new CustomException("message", cause)`).
2. Never catch top-level `Throwable` unless implementing an unhandled top-level process supervisor.
3. Catch the narrowest specific exception types before generic `Exception`.
