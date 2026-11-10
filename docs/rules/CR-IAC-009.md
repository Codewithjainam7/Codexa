# CR-IAC-009: Dockerfile SUID Escalation & Sudo Package Installation

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-IAC-009` |
| **Category** | Security |
| **Severity** | High |
| **Confidence** | High |
| **CWE** | [CWE-250: Execution with Unnecessary Privileges](https://cwe.mitre.org/data/definitions/250.html) |
| **OWASP Top 10** | A05:2021 - Security Misconfiguration |

---

## Description
Container images should adhere strictly to the principle of least privilege. Installing the `sudo` binary or assigning SetUID (`chmod u+s` / `4755`) to binaries inside a container filesystem creates prime vectors for local privilege escalation (LPE). If an unprivileged application process is exploited (e.g., via RCE or path traversal), an attacker can leverage SUID binaries or sudoers misconfigurations to gain instant root access inside the container and potentially attempt container breakout.

---

## Non-Compliant Code Example
```dockerfile
FROM ubuntu:22.04

RUN apt-get update && apt-get install -y sudo curl
RUN chmod 4755 /bin/busybox
RUN echo "appuser ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
```

---

## Compliant Solution
```dockerfile
FROM ubuntu:22.04

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Strip all unnecessary SUID / SGID bits
RUN find / -perm /6000 -type f -exec chmod a-s {} + 2>/dev/null || true

# Run as non-root
USER 10001:10001
```
