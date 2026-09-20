# Rule: CR-PARAM-001 — Prototype Pollution & Unsafe Parameter Merging

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-PARAM-001` |
| **Category** | `SECURITY` |
| **Severity** | `HIGH` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-1321: Prototype Pollution](https://cwe.mitre.org/data/definitions/1321.html) |
| **OWASP Top 10** | [A03:2021 - Injection](https://owasp.org/Top10/2021/A03_2021-Injection/) |

---

## 1. Description

Detects unsafe merging of user-controlled request parameters (`req.body`, `req.query`, or JSON payloads) into application objects using recursive merge functions (`lodash.merge`, `Object.assign`) or dynamic bracket keys without sanitization. An attacker who supplies properties like `__proto__`, `constructor`, or `prototype` can inject attributes directly into `Object.prototype`, altering application logic, bypassing security gates, or achieving Remote Code Execution (RCE).

---

## 2. Detection Patterns

- `Object.assign(target, req.body)`
- `lodash.merge(config, req.body)` or `_.extend(target, req.query)`
- Bracket assignment loops: `for (let key in req.body) target[key] = req.body[key]`

---

## 3. Vulnerable Code Example (Node.js)

```javascript
app.post("/api/settings", (req, res) => {
    // VULNERABLE: Direct merge allows modifying Object.prototype via __proto__
    const userSettings = Object.assign({}, defaultSettings, req.body);
    saveSettings(userSettings);
    res.json({ status: "saved" });
});
```

---

## 4. Remediated Code Example (Node.js)

```javascript
app.post("/api/settings", (req, res) => {
    // SECURE 1: Explicit property whitelisting
    const allowedKeys = ["theme", "notifications", "language"];
    const safeSettings = Object.create(null); // No prototype link

    for (const key of allowedKeys) {
        if (req.body[key] !== undefined && key !== "__proto__" && key !== "constructor") {
            safeSettings[key] = req.body[key];
        }
    }

    saveSettings(safeSettings);
    res.json({ status: "saved" });
});
```

---

## 5. Defense-in-Depth Mitigations

1. **Schema Validation**: Validate incoming request bodies against strict schemas (Zod, Joi, class-validator) with `stripUnknown: true`.
2. **Prototype-less Dictionaries**: Initialize lookup tables using `Object.create(null)` or `Map`.
3. **Freeze Builtin Prototypes**: Use `Object.freeze(Object.prototype)` in high-security Node.js runtimes.
