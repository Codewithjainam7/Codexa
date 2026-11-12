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

### Attack Impact
- **Remote Code Execution (RCE)**: Modifying gadget properties utilized by child process spawners or template engines.
- **Authentication Bypass**: Polluting properties like `isAdmin` or `role` to true for all runtime object evaluations.
- **Denial of Service (DoS)**: Overwriting core object functions (`toString`, `valueOf`) causing unhandled server exceptions.

---

## Non-Compliant Examples

```javascript
// Vulnerable: Recursive merge without key validation
function mergeDeep(target, source) {
  for (const key of Object.keys(source)) {
    if (typeof source[key] === "object" && source[key] !== null) {
      if (!target[key]) target[key] = {};
      mergeDeep(target[key], source[key]); // Attacker passes { "__proto__": { "polluted": true } }
    } else {
      target[key] = source[key];
    }
  }
  return target;
}
```

---

## Secure Remediation

### 1. Prototype-Safe Recursive Merge
```javascript
function safeMerge(target, source) {
  for (const key of Object.keys(source)) {
    // 1. Strictly block prototype pollution keys
    if (key === '__proto__' || key === 'constructor' || key === 'prototype') {
      continue;
    }
    
    // 2. Safely merge nested objects using null-prototype dictionary
    if (typeof source[key] === 'object' && source[key] !== null && !Array.isArray(source[key])) {
      if (!target[key]) target[key] = Object.create(null);
      safeMerge(target[key], source[key]);
    } else {
      target[key] = source[key];
    }
  }
  return target;
}
```

### 2. Use `Map` or Null-Prototype Objects
For dictionaries with user-controlled keys, always instantiate using `Object.create(null)` or ES6 `Map` so prototype inheritance does not exist:
```javascript
const userCache = Object.create(null);
const sessionStore = new Map();
```
