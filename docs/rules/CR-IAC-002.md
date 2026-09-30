# CR-IAC-002: Dockerfile Untagged Base Image (:latest Tag)

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-IAC-002` |
| **Category** | `OPERATIONS` / `SECURITY` |
| **Default Severity** | `MEDIUM` |
| **CWE Mapping** | [CWE-1104: Use of Unmaintained or Non-Deterministic Third Party Component](https://cwe.mitre.org/data/definitions/1104.html) |
| **OWASP Category** | A06:2021 – Vulnerable and Outdated Components |
| **Scanner Target** | `Dockerfile`, `Containerfile` |

---

## 1. Vulnerability Summary
Using the `:latest` tag or omitting image tags in Dockerfiles makes container builds non-deterministic. Every invocation of `docker build` may pull down a completely different base image version, introducing:
1. Untested breaking changes and dependency incompatibilities in upstream libraries.
2. Unvetted security vulnerabilities or supply chain compromises introduced into the upstream tag without developer review.
3. Inability to reproduce a historical production build during incident response or rollbacks.

---

## 2. Insecure Code Example

```dockerfile
# VIOLATION: Implicit :latest tag allows uncontrolled upstream shifts
FROM openjdk

WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Or explicitly using latest:
```dockerfile
# VIOLATION: Explicit :latest tag
FROM python:latest
```

---

## 3. Secure Remediation Pattern

Always pin specific major, minor, and patch version tags, or ideally pin the cryptographic SHA-256 digest of the verified base image:

```dockerfile
# REMEDIATION: Pinned semantic version and distribution
FROM eclipse-temurin:21.0.2_13-jre-jammy

WORKDIR /app
COPY target/*.jar app.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "app.jar"]
```

For maximum zero-trust supply chain assurance:
```dockerfile
# REMEDIATION: Cryptographically pinned SHA256 digest
FROM eclipse-temurin@sha256:4a0280eb46c6536551b920bf051c7aa7c3c52a466107380962b9a764d2626e85
```

---

## 4. Verification Checklist
- [ ] No `FROM` directives in any `Dockerfile` omit tags or use `:latest`.
- [ ] Automated dependency updates (e.g. Dependabot, Renovate) open PRs for base image digest bumps with automated test execution.
- [ ] CI pipeline fails on non-deterministic base image declarations.
