# CR-RACE-001: Time-of-Check to Time-of-Use (TOCTOU) Race Condition

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-RACE-001` |
| **Severity** | `MEDIUM` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-367: Time-of-check Time-of-use (TOCTOU) Race Condition](https://cwe.mitre.org/data/definitions/367.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | Java, Python, JavaScript, TypeScript |

---

## Vulnerability Overview

A TOCTOU race condition happens when a software system checks the state of a resource (e.g. `if (!file.exists())`), and then assumes that state remains unchanged until it performs an action (e.g. `file.createNewFile()`). In multithreaded environments or on shared file systems, a concurrent thread or symlink swap can alter the file between the check and the use.

---

## Secure Remediation

Use atomic filesystem operations with exclusive creation flags:

```java
// Java: Atomic file creation
Path path = Paths.get(filename);
try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
    out.write(data);
} catch (FileAlreadyExistsException e) {
    // Handle collision deterministically
}
```
