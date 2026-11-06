# CR-REDOS-001: Regular Expression Denial of Service (ReDoS)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-REDOS-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-1333: Inefficient Regular Expression Complexity](https://cwe.mitre.org/data/definitions/1333.html) |
| **OWASP Top 10** | A06:2021 – Vulnerable and Outdated Components |
| **Languages** | Java, Python, JavaScript, TypeScript, Go |

---

## Vulnerability Overview

Regular Expression Denial of Service (ReDoS) is an algorithmic complexity attack. When an NFA-based regex engine matches untrusted input against a pattern containing ambiguous, nested quantifiers, a non-matching or mismatched payload forces exponential backtracking:

$$T(n) \propto O(2^n)$$

A payload of merely 30 to 40 characters can cause the regex engine to execute billions of comparisons, locking up an entire CPU core and starving all concurrent threads.

---

## Vulnerable Code Patterns

```java
// Catastrophic nested repetition
Pattern pattern = Pattern.compile("^(a+)+$");
Matcher matcher = pattern.matcher(untrustedInput); // Freezes on "aaaaaaaaaaaaaaaaaaaaaaaaaaaa!"
```

```javascript
// Overlapping alternation
const emailRegex = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
```

---

## Secure Remediation

1. Eliminate nested quantifiers (`+`, `*`) within repeated groups.
2. Use possessive quantifiers (e.g. `(a++)+` in Java) or lookaheads to prevent backtracking.
3. Validate and enforce a strict upper bound on input length prior to regex evaluation.
