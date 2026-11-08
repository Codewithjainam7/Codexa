# CR-HELM-001: Plaintext Credentials in Helm values.yaml

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-HELM-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-312: Cleartext Storage of Sensitive Information](https://cwe.mitre.org/data/definitions/312.html) |
| **OWASP Top 10** | A02:2021 – Cryptographic Failures |
| **Languages** | Helm, YAML |

---

## Vulnerability Overview

Helm chart values files (`values.yaml`) frequently hold development configuration. Checking in database passwords, JWT secrets, or cloud keys directly into `values.yaml` commits sensitive credentials into source control, exposing them to anyone with repository read access.

---

## Secure Remediation

Use existing secret references or encrypted secrets with Helm Secrets / SOPS:

```yaml
# Secure: Reference external Secret object
db:
  existingSecret: "production-database-credentials"
  secretKey: "db-password"
```
