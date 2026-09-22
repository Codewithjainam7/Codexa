# Rule Specification: CR-CONFIG-001 (Insecure Configuration & Disabled TLS Validation)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-CONFIG-001` |
| **Category** | Security |
| **Default Severity** | `CRITICAL` |
| **CWE Mapping** | [CWE-295: Improper Certificate Validation](https://cwe.mitre.org/data/definitions/295.html) |
| **OWASP Top 10** | A05:2021 — Security Misconfiguration |
| **Target Scope** | Java AST (JSSE, OkHttp, Apache HttpClient), Python (Requests), Node.js (Axios) |

---

## 1. Vulnerability Overview

Disabling Transport Layer Security (TLS) certificate verification or hostname verification allows Man-in-the-Middle (MitM) attackers on local or transit networks to intercept, decrypt, and manipulate confidential API communications, database connections, and authentication tokens without generating client-side warnings.

Common anti-patterns include:
1. Creating "TrustAll" `X509TrustManager` instances with empty `checkServerTrusted()` and `checkClientTrusted()` implementations.
2. Configuring `HostnameVerifier` instances returning `true` unconditionally.
3. Setting `NODE_TLS_REJECT_UNAUTHORIZED='0'` in Node.js or `verify=False` in Python `requests`.

---

## 2. Detection Logic & AST Patterns

Codexa checks:
- **Java**:
  - Class declarations implementing `X509TrustManager` with empty method bodies in `checkServerTrusted` and `getAcceptedIssuers() { return null; }`.
  - Invocations of `.setHostnameVerifier((s, sslSession) -> true)` or `NoopHostnameVerifier.INSTANCE`.
  - SSLContext initialization with null or custom empty TrustManagers (`sslContext.init(null, trustAllCerts, new SecureRandom())`).
- **Python**: Invocations of `requests.get(..., verify=False)` or `verify=0`.
- **Node.js**: Environment or agent settings where `rejectUnauthorized: false`.

---

## 3. Vulnerable Code Example

```java
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class InsecureHttpClient {

    // VULNERABLE: Bypasses all TLS certificate chain verification (MitM vulnerability)
    public static SSLContext createInsecureContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        return sc;
    }
}
```

---

## 4. Secure Remediation

Always enforce default system trust stores or provide explicitly pinned certificate authorities (CA):

```java
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;

public class SecureHttpClient {

    // SECURE: Enforces standard JVM CA validation with default TrustManager
    public static SSLContext createSecureContext() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null); // Loads standard JVM cacerts bundle

        SSLContext sc = SSLContext.getInstance("TLSv1.3");
        sc.init(null, tmf.getTrustManagers(), null);
        return sc;
    }
}
```

### Self-Signed Internal Services:
If communicating with private internal microservices, import the internal root CA certificate into a custom Java `KeyStore` rather than globally disabling TLS validation.
