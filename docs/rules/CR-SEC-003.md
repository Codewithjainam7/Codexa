# CR-SEC-003: Path Traversal & Arbitrary File Access

### Overview
Flags unvalidated file path manipulation where user-supplied parameters or concatenated strings are passed directly to filesystem constructors without canonicalization or directory containment checks.

### Classification
- **Category:** SECURITY
- **Severity:** HIGH
- **OWASP Top 10:** A01:2021-Broken Access Control
- **CWE:** CWE-22 (Improper Limitation of a Pathname to a Restricted Directory)

### AST Pattern Detection Details
The rule inspects:
1. `ObjectCreationExpr` creating `File`, `FileInputStream`, `FileOutputStream`, `FileReader`, `FileWriter`, or `RandomAccessFile` where an argument contains dynamic binary string concatenation (`+`).
2. `MethodCallExpr` targeting `Paths.get()`, `Path.of()`, `resolve()`, or `resolveSibling()` using dynamic string concatenation.

### Remediation
Enforce canonical path normalization and verify that the destination path strictly resides within the approved base directory:
```java
Path destination = baseDir.resolve(fileName).normalize();
if (!destination.startsWith(baseDir)) {
    throw new SecurityException("Directory traversal attempt detected: " + fileName);
}
```
