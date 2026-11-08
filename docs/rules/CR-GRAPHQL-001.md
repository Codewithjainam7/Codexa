# CR-GRAPHQL-001: GraphQL Query Depth Limiting & Production Introspection Exposure

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-GRAPHQL-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-770: Allocation of Resources Without Limits](https://cwe.mitre.org/data/definitions/770.html) / [CWE-200: Exposure of Sensitive Information](https://cwe.mitre.org/data/definitions/200.html) |
| **OWASP Top 10** | A04:2021 – Insecure Design |
| **Languages** | JavaScript, TypeScript, Java, Python |

---

## Vulnerability Overview

1. **Unbounded Query Depth**: In relational GraphQL schemas, cyclical relationships allow attackers to craft nested queries of arbitrary depth (e.g. 100+ levels deep), triggering thousands of database round-trips and exhausting server memory.
2. **Schema Introspection Exposure**: Enabling introspection in production allows attackers to issue `__schema` queries and reverse-engineer entire internal microservices, private models, and hidden administrative mutations.

---

## Secure Remediation

```javascript
// Secure Apollo Server configuration:
import depthLimit from 'graphql-depth-limit';

const server = new ApolloServer({
  schema,
  validationRules: [depthLimit(7)],
  introspection: process.env.NODE_ENV !== 'production',
});
```
