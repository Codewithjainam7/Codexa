# Rule: CR-SEC-007 - Insecure Object Deserialization

## Metadata
- **Severity**: CRITICAL
- **Category**: Security / Injection
- **CWE**: CWE-502 (Deserialization of Untrusted Data)
- **Languages**: Java, Python, C#

## Vulnerability Description
Deserializing untrusted byte streams using native object serialization (e.g. Java `ObjectInputStream.readObject()`, Python `pickle.loads()`, PHP `unserialize()`) allows remote attackers to execute arbitrary code via gadget chains.

## Vulnerable Example (Java)
```java
// VIOLATION: Deserializing untrusted network input
ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
UserSession session = (UserSession) ois.readObject();
```

## Remediated Example (Java)
```java
// SAFE: Use strict JSON/Protobuf schemas with type-filtering
ObjectMapper mapper = JsonMapper.builder().build();
UserSession session = mapper.readValue(jsonString, UserSession.class);
```
