# CR-SEC-005: Insecure Object Deserialization

### Overview
Detects deserialization of untrusted byte streams using native Java deserialization mechanisms that are vulnerable to arbitrary Remote Code Execution (RCE) via gadget chains.

### Classification
- **Category:** SECURITY
- **Severity:** CRITICAL
- **OWASP Top 10:** A08:2021-Software and Data Integrity Failures
- **CWE:** CWE-502 (Deserialization of Untrusted Data)

### AST Pattern Detection Details
The rule inspects:
1. `MethodCallExpr` calling `readObject()` or `readUnshared()` on `ObjectInputStream`.
2. `MethodCallExpr` calling `enableDefaultTyping()` on Jackson `ObjectMapper`.
3. `ObjectCreationExpr` instantiating `XMLDecoder`.

### Remediation
Avoid Java native serialization. Migrate to safe data interchange formats such as JSON with strict schema validation, or enforce a strict `ValidatingObjectInputStream` allow-list:
```java
ValidatingObjectInputStream ois = new ValidatingObjectInputStream(in);
ois.accept(SafeDomainClass.class);
```
