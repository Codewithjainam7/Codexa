# Rule: CR-PERF-001 — String Concatenation in Loop via +=

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-PERF-001` |
| **Category** | `QUALITY` |
| **Severity** | `LOW` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-400: Uncontrolled Resource Consumption](https://cwe.mitre.org/data/definitions/400.html) |
| **OWASP Top 10** | Performance & Resource Management |

---

## 1. Description

Detects repeated string concatenation using the `+=` operator inside iterative loop constructs (`for`, `while`, `do-while`). Because `java.lang.String` instances are immutable in Java, every `+=` operation allocates a new `StringBuilder`, appends the operand, and reallocates a new underlying `char[]` buffer, degrading throughput from linear $O(N)$ to quadratic $O(N^2)$ time and causing GC heap churn.

---

## 2. AST Heuristic Precision & False Positive Filter

To eliminate false positives on primitive numeric counter accumulation (`fileCount += bytesRead;`, `complexity += depth;`), Codexa inspects AST assignment operators:
- Verifies string literal presence (`"..."`).
- Verifies invocation of `.toString()`, `String.valueOf()`, or `.name()`.
- Excludes common primitive accumulator identifier names: `count`, `bytes`, `size`, `total`, `idx`, `debt`, `penalty`, `complexity`.

---

## 3. Vulnerable Code Example

```java
public String generateCsv(List<User> users) {
    String csv = "";
    // VULNERABLE: Quadratic memory allocation inside loop
    for (User u : users) {
        csv += u.getId() + "," + u.getUsername() + "\n";
    }
    return csv;
}
```

---

## 4. Remediated Code Example

```java
public String generateCsv(List<User> users) {
    // SECURE: Pre-sized StringBuilder executes in linear O(N) time
    StringBuilder sb = new StringBuilder(users.size() * 64);
    for (User u : users) {
        sb.append(u.getId()).append(',').append(u.getUsername()).append('\n');
    }
    return sb.toString();
}
```
