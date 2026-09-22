# Rule Specification: CR-DESER-001 (Insecure Object Deserialization)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-DESER-001` |
| **Category** | Security |
| **Default Severity** | `CRITICAL` |
| **CWE Mapping** | [CWE-502: Deserialization of Untrusted Data](https://cwe.mitre.org/data/definitions/502.html) |
| **OWASP Top 10** | A08:2021 — Software and Data Integrity Failures |
| **Target Scope** | Java AST, Python, Node.js, C#, PHP Deserialization APIs |

---

## 1. Vulnerability Overview

Insecure deserialization occurs when untrusted byte streams or serialized object graphs are unpacked into runtime memory without cryptographic verification or strict type filters.

In Java, invoking `ObjectInputStream.readObject()` on arbitrary user payloads triggers gadget chains present in the runtime classpath (e.g. Apache Commons Collections, Spring Beans, Jackson polymorphic gadgets), allowing attackers to achieve arbitrary **Remote Code Execution (RCE)** before application validation logic ever runs.

In Python, `pickle.loads()` executes arbitrary opcode calls (e.g. `__reduce__` triggering `os.system`). In YAML libraries, un-safe loaders (`yaml.load()`) instantiate arbitrary objects.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**: Invocations of `ObjectInputStream.readObject()` or `readUnshared()` without an active `ObjectInputFilter`. Jackson polymorphic typing configured with `enableDefaultTyping()` or `activateDefaultTyping(LaissezFaireSubTypeValidator.instance)`.
- **Python**: Invocations of `pickle.loads(...)`, `yaml.load(..., Loader=yaml.Loader)` instead of `yaml.safe_load()`.
- **PHP**: Invocations of `unserialize(...)` on untrusted input strings.

---

## 3. Vulnerable Code Examples

```java
// VULNERABLE: Direct deserialization of untrusted network stream invites RCE gadget execution
public Object deserializeUser(InputStream networkStream) throws Exception {
    try (ObjectInputStream ois = new ObjectInputStream(networkStream)) {
        return ois.readObject();
    }
}
```

```python
# VULNERABLE: Python pickle allows arbitrary bytecode execution during unpickling
import pickle

def load_session(data):
    return pickle.loads(data)
```

---

## 4. Secure Remediation

### A. Prefer Structured Formats (JSON / Protocol Buffers)
Avoid language-native binary serialization. Use typed, schema-validated JSON with Jackson or Google Protocol Buffers:

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // SECURE: Structured JSON mapping without polymorphic gadget risk
    public UserProfile parseProfile(byte[] jsonBytes) throws Exception {
        return MAPPER.readValue(jsonBytes, UserProfile.class);
    }
}
```

### B. Java Look-Ahead Deserialization Filters
If `ObjectInputStream` is mandatory for legacy compatibility, enforce strict Java 9+ `ObjectInputFilter`:

```java
ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
    "com.example.model.UserProfile;com.example.model.Address;!*"
);
ois.setObjectInputFilter(filter);
```

### C. Safe YAML Loading in Python
```python
import yaml
# SECURE: safe_load disallows arbitrary class instantiation
data = yaml.safe_load(untrusted_yaml_content)
```
