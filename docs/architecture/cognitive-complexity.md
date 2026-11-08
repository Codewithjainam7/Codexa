# Cognitive Complexity Metric Engine

Codexa implements the **Cognitive Complexity** metric (originated by G. Ann Campbell / SonarSource) to assess code maintainability from a cognitive load perspective.

---

## Cyclomatic Complexity vs Cognitive Complexity

| Factor | Cyclomatic Complexity (McCabe) | Cognitive Complexity (Codexa) |
| :--- | :--- | :--- |
| **Primary Goal** | Measures testability & minimum test paths | Measures human mental burden & readability |
| **Mathematical Basis** | Graph theory ($E - N + 2P$) | Incremental nesting & flow-break penalties |
| **Nesting Penalty** | Zero penalty for deep nesting | Multiplies penalty by indentation depth |
| **Recommended Cap** | Flagged when $CC > 15$ | Flagged when $CogC > 15$ |

---

## Scoring Rules
1. **Increments**: `if`, `ternary ? :`, `switch`, `for`, `while`, `catch`, and boolean sequences increment by $+1$.
2. **Nesting Penalty**: Each nested conditional or loop inherits an additional $+N$ penalty equal to its current block nesting level.
