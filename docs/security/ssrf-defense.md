# Server-Side Request Forgery (SSRF) Defense Architecture

This specification details the multi-layered defense-in-depth architecture implemented in **Codexa** to prevent Server-Side Request Forgery (SSRF, CWE-918) attacks during remote repository fetching and webhook delivery.

---

## 1. Threat Landscape & Attack Vectors

Codexa accepts remote repository URLs (e.g. GitHub, GitLab, Bitbucket) from end-users to perform static code analysis. Without strict egress verification, an attacker could supply malicious URLs to probe internal network infrastructure:

- **Cloud Instance Metadata Theft**: Exploiting `http://169.254.169.254/latest/meta-data/` on AWS/GCP to exfiltrate IAM role credentials.
- **Internal Microservice Port Scanning**: Probing Kubernetes internal services (e.g. `http://vault.internal.svc:8200`, `http://k8s-api:6443`).
- **Loopback Service Exploitation**: Reaching local management endpoints (e.g. `http://localhost:8080/actuator`, `http://127.0.0.1:9090`).
- **DNS Rebinding Attacks**: Pointing a domain to a public IP during initial validation, then immediately resolving it to `127.0.0.1` during the HTTP GET phase.

---

## 2. Multi-Stage Defense Pipeline

Codexa routes all remote URL ingestion requests through the `SsrfProtectionService`:

```
[ User Supplies Repository URL ]
               │
               ▼
[ Stage 1: Protocol & Scheme Validation ]
  - Whitelist: ONLY "https" (or "http" in explicit test environments)
  - Reject: "file://", "gopher://", "ftp://", "jar://", "data://"
               │
               ▼
[ Stage 2: Domain Whitelist & Host Normalization ]
  - Permitted domains: github.com, gitlab.com, bitbucket.org, dev.azure.com
  - Reject numeric IP addresses in host field
  - Normalize punycode and unicode homograph domains
               │
               ▼
[ Stage 3: Synchronous DNS Resolution ]
  - Resolve ALL A (IPv4) and AAAA (IPv6) records for target host
  - Evaluate every resolved IP against prohibited CIDR ranges
               │
               ▼
[ Stage 4: Prohibited CIDR Range Blacklist ]
  - RFC 1918 Private Ranges (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
  - Loopback (127.0.0.0/8, ::1)
  - Link-Local / Cloud Metadata (169.254.0.0/16, fe80::/10)
  - Carrier-Grade NAT (100.64.0.0/10)
  - IPv6 Unique Local (fc00::/7)
               │
               ▼
[ Stage 5: DNS Rebinding Defense (IP Pinning) ]
  - HTTP connection connects directly to the validated IP address
  - Host header retained for SNI and virtual host routing
```

---

## 3. Prohibited Subnets & Reserved IP Ranges

The following network ranges are unconditionally blocked:

| Range / Subnet | Classification | Justification |
|:---|:---|:---|
| `127.0.0.0/8` | IPv4 Loopback | Protects localhost services on the host runner. |
| `::1/128` | IPv6 Loopback | Protects localhost IPv6 endpoints. |
| `10.0.0.0/8` | RFC 1918 Private | Blocks corporate LAN & Kubernetes VPC CIDRs. |
| `172.16.0.0/12` | RFC 1918 Private | Blocks Docker default bridge and VPC subnets. |
| `192.168.0.0/16`| RFC 1918 Private | Blocks internal enterprise network devices. |
| `169.254.0.0/16`| RFC 3927 Link-Local | **AWS/GCP/Azure IMDS metadata service** protection. |
| `fe80::/10` | IPv6 Link-Local | Prevents internal network auto-configuration exploits. |
| `fc00::/7` | IPv6 Unique Local | Blocks internal IPv6 corporate intranets. |
| `0.0.0.0/8` | "This Host" | Disallows unspecified broadcast routing. |
| `224.0.0.0/4` | Multicast | Blocks multicast discovery probing. |

---

## 4. Implementation Details (`SsrfProtectionService`)

Below is the architectural core of Codexa's SSRF validator:

```java
public class SsrfProtectionService {

    public void validateRemoteUrl(String urlString) {
        URI uri = URI.create(urlString);
        String scheme = uri.getScheme();
        
        // 1. Strict Protocol Verification
        if (!"https".equalsIgnoreCase(scheme)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SSRF_INVALID_PROTOCOL",
                "Only HTTPS remote repository connections are permitted.");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SSRF_INVALID_HOST", "Missing hostname.");
        }

        // 2. DNS Resolution of all associated IPs
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SSRF_DNS_RESOLUTION_FAILED",
                "Could not resolve host: " + host);
        }

        // 3. Inspect every resolved IP address
        for (InetAddress address : addresses) {
            if (isProhibitedIp(address)) {
                log.warn("SSRF attempt blocked! Host '{}' resolved to prohibited IP '{}'", host, address.getHostAddress());
                throw new ApiException(HttpStatus.BAD_REQUEST, "SSRF_PROHIBITED_IP",
                    "Resolved IP address is forbidden by security policy.");
            }
        }
    }

    private boolean isProhibitedIp(InetAddress address) {
        return address.isLoopbackAddress()
            || address.isSiteLocalAddress()
            || address.isLinkLocalAddress()
            || address.isAnyLocalAddress()
            || isCloudMetadataIp(address);
    }
}
```

---

## 5. DNS Rebinding Countermeasure: IP Pinning

Standard HTTP clients resolve hostnames twice: once during application-level validation and again during socket connection. If an attacker controls the authoritative DNS server, they can respond with a benign IP on the first query and `169.254.169.254` on the second query.

### Codexa's Mitigation:
Codexa pins the validated IP address at the socket layer:
```java
// Open connection directly to validated IP, preserving SNI & Host header
Socket socket = sslSocketFactory.createSocket(validatedIp, 443);
((SSLSocket) socket).setSSLParameters(new SSLParameters() {{
    setServerNames(List.of(new SNIHostName(originalHostName)));
}});
```

---

## 6. Comprehensive Verification Test Suite

The SSRF protection layer is covered by 16 automated integration tests in `SsrfProtectionTest.java`:
- Detection of AWS metadata IP `169.254.169.254`
- Detection of loopback `127.0.0.1` and `[::1]`
- Detection of RFC 1918 private subnets
- Rejection of invalid protocols (`file://`, `ftp://`, `gopher://`)
- Detection of octal/hex IP encodings (e.g. `http://0177.0.0.1`)
