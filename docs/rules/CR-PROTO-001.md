# CR-PROTO-001: Server-Side Prototype Pollution via Dynamic Key Assignment

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-PROTO-001` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-1321: Improperly Controlled Modification of Dynamically-Determined Object Attributes](https://cwe.mitre.org/data/definitions/1321.html) |
| **OWASP Top 10** | A03:2021 – Injection |
| **Languages** | JavaScript, TypeScript, Node.js |

---

## Vulnerability Overview

In JavaScript, objects inherit properties from `Object.prototype`. If an application recursively merges user-controllable JSON objects without sanitizing reserved property keys (`__proto__`, `constructor`, `prototype`), an attacker can inject malicious properties globally onto all objects. 

---

## Secure Remediation

```javascript
// Secure merge implementation:
function safeMerge(target, source) {
  for (const key of Object.keys(source)) {
    if (key === '__proto__' || key === 'constructor' || key === 'prototype') {
      continue; // Block prototype pollution vectors
    }
    if (typeof source[key] === 'object' && source[key] !== null) {
      target[key] = safeMerge(target[key] || {}, source[key]);
    } else {
      target[key] = source[key];
    }
  }
  return target;
}
```
