# CR-REDIS-001: Redis Command Injection & Unsanitized Lua Eval

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-REDIS-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-94: Improper Control of Generation of Code](https://cwe.mitre.org/data/definitions/94.html) |
| **OWASP Top 10** | A03:2021 – Injection |
| **Languages** | Java, Python, JavaScript, TypeScript |

---

## Vulnerability Overview

When executing Redis Lua scripts via `eval` or `evalsha`, embedding unescaped user parameters directly into the script string allows attackers to break out of Lua string literals and execute arbitrary Redis commands inside the server (e.g. wiping the cache, poisoning session keys, or reading arbitrary Redis keys).

---

## Secure Remediation

Always keep the Lua script template static and pass user variables via the `KEYS` and `ARGV` arrays:

```javascript
// Secure Redis Lua invocation:
const script = `
  local current = redis.call('get', KEYS[1])
  if not current then
    return redis.call('set', KEYS[1], ARGV[1])
  end
  return current
`;
await redis.eval(script, 1, userKey, userValue);
```
