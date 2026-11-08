# CR-MEMORY-001: Unclosed Stream & Native Resource Leak

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-MEMORY-001` |
| **Severity** | `MEDIUM` |
| **Category** | `QUALITY` |
| **CWE ID** | [CWE-775: Missing Release of File Descriptor or Resource after Effective Lifetime](https://cwe.mitre.org/data/definitions/775.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | Java, Python, Go, TypeScript |

---

## Vulnerability Overview

When allocating OS resources (file handles, sockets, database cursors, native direct buffers), failure to guarantee release across all exception paths results in file descriptor depletion (`EMFILE: too many open files`) and eventual JVM or container failure.

---

## Secure Remediation

```java
// Java: try-with-resources manages lifecycle deterministically
try (FileInputStream fis = new FileInputStream(file);
     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
    String line;
    while ((line = reader.readLine()) != null) {
        process(line);
    }
}
```
