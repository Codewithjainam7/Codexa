# Rule: CR-QUAL-001 — High Cyclomatic Complexity

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-QUAL-001` |
| **Category** | `QUALITY` |
| **Severity** | `LOW` (Complexity 26–35) / `MEDIUM` (Complexity > 35) |
| **Confidence** | `HIGH` |
| **Standard Threshold** | `25` (Standard business methods) |
| **Dispatcher Threshold** | `200` (Rule engines, diagnostic collectors, report exporters) |

---

## 1. Description

Calculates McCabe Cyclomatic Complexity by counting linearly independent execution paths through a method. Methods with excessive cyclomatic complexity have combinatorial test paths, elevated defect densities, and high cognitive load for code reviewers.

---

## 2. AST Calculation Formula

Each of the following constructs contributes +1 to the method's base complexity (1):
- `IfStmt` conditional branches
- `WhileStmt`, `ForStmt`, `ForEachStmt`, `DoStmt` loop statements
- `CatchClause` exception handling branches
- `ConditionalExpr` ternary operators (`? :`)
- `SwitchEntry` case labels
- Logical binary operators (`&&`, `||`)

---

## 3. Vulnerable / Complex Code Example

```java
public void processTransaction(Transaction tx) {
    if (tx != null) {
        if (tx.isValid()) {
            if (tx.getAmount() > 1000) {
                if (tx.isInternational()) {
                    // Deeply branchy logic across 30+ nested checks...
                }
            }
        }
    }
}
```

---

## 4. Refactoring & Remediation Strategies

1. **Extract Method**: Decompose large monolithic methods into smaller, cohesive private helper functions.
2. **Strategy Pattern / Polymorphism**: Replace large switch/case or if-else ladders with interface implementations.
3. **Guard Clauses**: Invert nested conditionals to exit early (`if (!tx.isValid()) return;`).
