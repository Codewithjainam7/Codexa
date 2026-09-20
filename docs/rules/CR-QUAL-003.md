# Rule: CR-QUAL-003 — Deep Statement Nesting

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-QUAL-003` |
| **Category** | `QUALITY` |
| **Severity** | `LOW` |
| **Confidence** | `HIGH` |
| **Threshold** | Nesting Depth > 6 levels |

---

## 1. Description

Detects deeply nested control flow structures (`if`, `for`, `while`, `switch`) exceeding 6 levels of indentation within a single method. Deeply nested code, commonly termed the "Arrow Anti-Pattern", dramatically increases cognitive complexity, obscures edge-case error handling, and increases the likelihood of regression bugs.

---

## 2. AST Flattening Heuristics

To prevent false positives on idiomatic code structures, Codexa applies specific AST heuristics:
- **`else if` Exclusion**: Sequential `if ... else if ... else if` ladders are treated as alternative branches at the same depth level rather than nested sub-blocks.
- **Defensive `try` Blocks**: Standard try-with-resources and exception handling blocks are excluded from nesting penalties.

---

## 3. Vulnerable Nested Example

```java
public void processBatch(Batch batch) {
    if (batch != null) {
        if (!batch.isEmpty()) {
            for (Item item : batch.getItems()) {
                if (item.isActive()) {
                    while (item.hasPendingTasks()) {
                        if (item.canExecute()) {
                            if (item.isPriority()) {
                                executeItem(item); // 7 levels deep!
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## 4. Remediated Guard Clause Example

```java
public void processBatch(Batch batch) {
    // SECURE & READABLE: Guard clauses flatten the hierarchy
    if (batch == null || batch.isEmpty()) {
        return;
    }

    for (Item item : batch.getItems()) {
        processItem(item);
    }
}

private void processItem(Item item) {
    if (!item.isActive()) return;

    while (item.hasPendingTasks()) {
        if (item.canExecute() && item.isPriority()) {
            executeItem(item);
        }
    }
}
```
