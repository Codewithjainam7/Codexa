# Rule: CR-SSRF-001 — Server-Side Request Forgery (SSRF)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-SSRF-001` |
| **Category** | `SECURITY` |
| **Severity** | `CRITICAL` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-918: Server-Side Request Forgery (SSRF)](https://cwe.mitre.org/data/definitions/918.html) |
| **OWASP Top 10** | [A10:2021 - Server-Side Request Forgery (SSRF)](https://owasp.org/Top10/2021/A10_2021-Server-Side_Request_Forgery_%28SSRF%29/) |

---

## 1. Description

Detects outbound network requests initiated using user-supplied URLs or endpoints without IP address resolution validation and private network filtering. SSRF allows attackers to force the server to issue unauthorized requests to internal services, loopback interfaces (`127.0.0.1`), private subnets (`10.0.0.0/8`, `192.168.0.0/16`, `172.16.0.0/12`), or cloud instance metadata services (`169.254.169.254`).

---

## 2. Detection Patterns & Taint Sinks

- **Java**:
  - `new URL(userUrl).openConnection()`
  - `HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(userUrl))...)`
  - `RestTemplate.getForObject(userUrl, ...)`
  - `WebClient.create().get().uri(userUrl)...`
- **Python**:
  - `requests.get(user_url)` without host verification
- **Node.js**:
  - `fetch(req.body.url)` or `axios.get(req.query.target)`

---

## 3. Vulnerable Code Example (Java)

```java
public byte[] fetchRemoteResource(String webhookUrl) throws IOException {
    // VULNERABLE: Direct HTTP request to user-controlled URL
    URL url = new URL(webhookUrl);
    try (InputStream in = url.openStream()) {
        return in.readAllBytes();
    }
}
```

---

## 4. Remediated Code Example (Java)

```java
public byte[] fetchRemoteResource(String webhookUrl) throws IOException {
    URI uri = URI.create(webhookUrl);
    
    // SECURE 1: Enforce HTTPS protocol
    if (!"https".equalsIgnoreCase(uri.getScheme())) {
        throw new SecurityException("Only secure HTTPS connections permitted");
    }

    // SECURE 2: Resolve IP and block private / loopback / link-local addresses
    InetAddress[] addresses = InetAddress.getAllByName(uri.getHost());
    for (InetAddress addr : addresses) {
        if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() || 
            addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
            throw new SecurityException("SSRF attempt to internal or link-local address: " + addr);
        }
    }

    HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER) // SECURE 3: Prevent redirect to private subnet
            .build();

    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
    // execute validated request
    ...
}
```

---

## 5. Architectural Mitigations

1. **Strict Protocol Whitelist**: Permit only `https://`, explicitly rejecting `file://`, `gopher://`, `ftp://`, and `ldap://`.
2. **Disable Automatic Redirect Following**: Attackers frequently use open redirects on external hosts to bypass initial IP validation.
3. **Dedicated Egress Proxy**: Route outbound requests through an isolated egress gateway with strict VPC firewall rules.
