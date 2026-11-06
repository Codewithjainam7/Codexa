# CR-XXE-001: XML External Entity (XXE) Injection

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-XXE-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-611: Improper Restriction of XML External Entity Reference](https://cwe.mitre.org/data/definitions/611.html) |
| **OWASP Top 10** | A05:2021 – Security Misconfiguration |
| **Languages** | Java, Python, JavaScript, TypeScript |

---

## Vulnerability Overview

XML External Entity (XXE) attacks occur when an untrusted XML input containing a reference to an external entity is processed by a weakly configured XML parser. 

Attackers exploit this weakness to:
- Disclose confidential server files (`/etc/passwd`, sensitive configuration files, cloud instance metadata).
- Perform Server-Side Request Forgery (SSRF) against internal private microservices.
- Trigger Denial-of-Service via exponential entity expansion (Billion Laughs / XML Bomb).

---

## Vulnerable Code Patterns

```java
// Vulnerable: Default DocumentBuilderFactory permits DTD and external entities
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
DocumentBuilder db = dbf.newDocumentBuilder();
Document doc = db.parse(request.getInputStream());
```

---

## Secure Remediation

```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
// Disallow DOCTYPE declarations entirely:
dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
// Disable external entities:
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
dbf.setXIncludeAware(false);
dbf.setExpandEntityReferences(false);
```
