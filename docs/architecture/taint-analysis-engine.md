# Interprocedural Source-to-Sink Taint Analysis Engine

Codexa's static analysis engine incorporates AST-level **Taint Analysis** to track untrusted data flow from external entry points (sources) directly to high-risk execution primitives (sinks).

```
 ┌────────────────────────────────────────┐
 │ Ingress Taint Sources                  │
 │ (@RequestParam, req.body, request.args)│
 └───────────────────┬────────────────────┘
                     │
                     ▼
 ┌────────────────────────────────────────┐
 │ AST Propagation Graph                  │
 │ (Assignments, Concatenations, Returns) │
 └───────────────────┬────────────────────┘
                     │
                     ▼
 ┌────────────────────────────────────────┐
 │ Dangerous Execution Sinks              │
 │ (PreparedStatement, ProcessBuilder,    │
 │  Runtime.exec, ObjectInputStream)      │
 └────────────────────────────────────────┘
```

---

## 1. Taint Sources
Codexa flags user-controlled ingress points across frameworks:
- **Spring Boot**: `@RequestParam`, `@PathVariable`, `@RequestBody`, `@RequestHeader`, `@ModelAttribute`.
- **Node.js Express / Next.js**: `req.params`, `req.query`, `req.body`, `searchParams.get()`.
- **Python Flask / FastAPI**: `request.args`, `request.form`, `request.json()`, route arguments.

---

## 2. Propagation Tracking
When a tainted variable is used in assignments or string operations:
```java
@GetMapping("/search")
public List<User> search(@RequestParam String query) {
    String formattedQuery = "SELECT * FROM users WHERE name = '" + query + "'"; // Taint propagated
    return jdbcTemplate.queryForList(formattedQuery); // Sink reached!
}
```

Codexa's `TaintAnalysisEngine` models this data dependency:
1. `L1`: Taint Source: parameter `query` marked with `@RequestParam`.
2. `L2`: Taint Propagate: variable `formattedQuery` derived from tainted expression containing `query`.
3. `L3`: Taint Sink Reached: `queryForList` called with `formattedQuery`.

---

## 3. False Positive Suppression
If sanitization routines or parameterized bindings are encountered between source and sink, the taint flag is cleared, preventing false alarm noise.
