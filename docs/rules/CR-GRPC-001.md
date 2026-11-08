# CR-GRPC-001: gRPC Plaintext Channel & Unauthenticated Service

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-GRPC-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-319: Cleartext Transmission of Sensitive Information](https://cwe.mitre.org/data/definitions/319.html) |
| **OWASP Top 10** | A02:2021 – Cryptographic Failures |
| **Languages** | Java, Go, Python, TypeScript |

---

## Vulnerability Overview

When microservices communicate via gRPC, developers often default to `.usePlaintext()` or `insecure.NewCredentials()` during initial development. If left in production, HTTP/2 frame data, internal protobuf messages, bearer tokens, and customer data are transmitted in plaintext across network boundaries, exposing systems to eavesdropping, replay attacks, and token interception.

---

## Secure Remediation

```java
// Java: Enforce TLS credentials on Channel
ManagedChannel channel = Grpc.newChannelBuilderForAddress(
    host, port, TlsChannelCredentials.create()
).build();
```

```go
// Go: Require TLS credentials
creds, err := credentials.NewClientTLSFromFile("cert.pem", "")
conn, err := grpc.Dial(address, grpc.WithTransportCredentials(creds))
```
