# CR-NOSQL-001: NoSQL Operator Injection & $where Code Execution

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-NOSQL-001` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-943: Improper Neutralization of Special Elements in Data Query Logic](https://cwe.mitre.org/data/definitions/943.html) |
| **OWASP Top 10** | A03:2021 – Injection |
| **Languages** | JavaScript, TypeScript, Python, Java |

---

## Vulnerability Overview

1. **NoSQL Operator Injection**: When Express or Python parses JSON bodies into native objects, passing `req.body` directly to `User.findOne(req.body)` allows an attacker to submit `{"username": "admin", "password": {"$ne": ""}}`. Since `password != ""` is true, the query succeeds without a password.
2. **MongoDB `$where` Injection**: The `$where` operator evaluates raw JavaScript strings on the MongoDB server. Dynamic concatenation leads to arbitrary JavaScript execution in the database engine.

---

## Secure Remediation

```javascript
// Secure: Typecast inputs to string primitives and avoid passing nested objects:
const username = String(req.body.username || '');
const password = String(req.body.password || '');

const user = await User.findOne({ username });
if (!user || !(await bcrypt.compare(password, user.passwordHash))) {
  throw new Error('Invalid credentials');
}
```
