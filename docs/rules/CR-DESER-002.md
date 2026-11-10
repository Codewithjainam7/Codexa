# CR-DESER-002: Polyglot Insecure Deserialization (Python & Node.js)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-DESER-002` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-502: Deserialization of Untrusted Data](https://cwe.mitre.org/data/definitions/502.html) |
| **OWASP Top 10** | A08:2021 – Software and Data Integrity Failures |
| **Languages** | Python, JavaScript, TypeScript, Node.js |

---

## Vulnerability Overview

Deserializing untrusted data with formats that support arbitrary object graphs and code invocation primitives (such as Python `pickle`, `jsonpickle`, unsafe PyYAML `yaml.load`, or Node.js `node-serialize`) grants attackers direct Remote Code Execution (RCE) via standard gadget chains.

---

## Secure Remediation

```python
# Secure Python: Use safe_load for YAML and json for state
import yaml
data = yaml.safe_load(untrusted_content)
```
