# CR-IAC-001: Dockerfile Container Running as Root User

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-IAC-001` |
| **Category** | `OPERATIONS` / `SECURITY` |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-250: Execution with Unnecessary Privileges](https://cwe.mitre.org/data/definitions/250.html) |
| **OWASP Category** | A05:2021 – Security Misconfiguration |
| **Scanner Target** | `Dockerfile`, `Containerfile` |

---

## 1. Vulnerability Summary
By default, Docker containers execute processes as the `root` user (UID 0) inside the container namespace unless an explicit `USER` directive is specified. If an application running inside the container suffers from a vulnerability (such as Command Injection `CWE-78`, Path Traversal `CWE-22`, or Remote Code Execution), an attacker can leverage host kernel vulnerabilities, container runtime bugs (e.g., CVE-2024-21626, runc breakout), or mounted host volumes (such as `/var/run/docker.sock`) to escape to the underlying host node with full administrative privileges.

---

## 2. Insecure Code Example

```dockerfile
FROM node:20-alpine

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 3000

# VIOLATION: No USER directive declared. Node.js process runs as root (UID 0).
CMD ["node", "server.js"]
```

---

## 3. Secure Remediation Pattern

Always create an unprivileged user and group or utilize the distribution's built-in non-root account (e.g., `node`, `appuser`, `nonroot` in Google Distroless).

```dockerfile
FROM node:20-alpine

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .

# REMEDIATION: Switch to non-root user before entrypoint execution
USER node

EXPOSE 3000
CMD ["node", "server.js"]
```

For custom enterprise distributions (e.g., Alpine / Debian):
```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup
```

---

## 4. Verification Checklist
- [ ] Every Dockerfile specifies an unprivileged `USER <uid>[:<gid>]` directive.
- [ ] Container filesystem permissions restrict write access outside `/tmp` or dedicated volume mounts.
- [ ] Kubernetes deployments enforce `securityContext.runAsNonRoot: true`.
