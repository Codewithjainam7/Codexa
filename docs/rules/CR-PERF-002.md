# Rule: CR-PERF-002 - Unclosed I/O Stream & Resource Leak

## Metadata
- **Severity**: MEDIUM
- **Category**: Performance / Reliability
- **CWE**: CWE-775 (Missing Release of Resource after Effective Lifetime)
- **Languages**: Java, Python, Go, C#

## Vulnerability Description
Failing to close OS file descriptors, network sockets, or database connections in `finally` blocks or try-with-resources constructs leads to OS handle leaks and server unresponsiveness.

## Vulnerable Example (Java)
```java
// VIOLATION: InputStream not closed on exception
FileInputStream fis = new FileInputStream(file);
byte[] data = fis.readAllBytes();
// If readAllBytes throws, fis remains open
fis.close();
```

## Remediated Example (Java)
```java
// SAFE: Use try-with-resources statement
try (FileInputStream fis = new FileInputStream(file)) {
    byte[] data = fis.readAllBytes();
    return data;
}
```
