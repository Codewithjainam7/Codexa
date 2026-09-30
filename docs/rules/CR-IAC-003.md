# CR-IAC-003: Kubernetes Privileged Container Escalation

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-IAC-003` |
| **Category** | `SECURITY` |
| **Default Severity** | `CRITICAL` |
| **CWE Mapping** | [CWE-250: Execution with Unnecessary Privileges](https://cwe.mitre.org/data/definitions/250.html) |
| **OWASP Category** | A05:2021 – Security Misconfiguration |
| **CIS Benchmark** | CIS Kubernetes Benchmark v1.8 (Section 5.2.1) |
| **Scanner Target** | `*.yaml`, `*.yml`, Helm templates, Kubernetes manifests |

---

## 1. Vulnerability Summary
Setting `privileged: true` in a Kubernetes container's `securityContext` disables all container isolation mechanisms. The container is granted access to all host devices, bypasses AppArmor/SELinux profiles, and gains the full Linux capabilities set of the host node. An attacker executing inside a privileged pod has an immediate, trivial path to complete host node compromise, kubelet credential theft, and lateral movement across the entire Kubernetes cluster.

---

## 2. Insecure Manifest Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-processor
spec:
  template:
    spec:
      containers:
        - name: app
          image: internal/payment:1.4.0
          securityContext:
            # VIOLATION: Privileged mode disables container sandbox
            privileged: true
            allowPrivilegeEscalation: true
```

---

## 3. Secure Hardening Pattern

Containers should run with zero elevated privileges, read-only root filesystems, and explicit capability drops:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-processor
spec:
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 10001
        runAsGroup: 10001
        fsGroup: 10001
      containers:
        - name: app
          image: internal/payment:1.4.0
          securityContext:
            privileged: false
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop:
                - ALL
```

---

## 4. Verification Checklist
- [ ] No pod or container manifests declare `privileged: true`.
- [ ] `allowPrivilegeEscalation` is explicitly set to `false`.
- [ ] Kubernetes Admission Controllers (OPA Gatekeeper, Kyverno, or Pod Security Standards `restricted` profile) enforce privilege denial at deployment time.
