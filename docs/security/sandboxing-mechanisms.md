# Codexa Process Sandboxing, Container Hardening & Execution Isolation

This specification defines the process sandboxing mechanisms, container security policies, Linux kernel security profiles, and filesystem isolation controls used by **Codexa** to safely process hostile or untrusted source code repositories.

---

## 1. Threat Model & Sandboxing Objectives

In multi-tenant SaaS environments or automated CI/CD gating pipelines, repositories submitted for analysis may contain malicious payloads:
- Exploit scripts masquerading as build configurations.
- Path traversal archives (Zip Slip) attempting to overwrite system binaries.
- Malicious binaries designed to initiate reverse shells or access host sockets.
- Algorithmic complexity attacks (Zip Bombs, ReDoS) designed to starve host resources.

### Sandboxing Objectives:
1. **Absolute Non-Execution Guarantee**: Source files are parsed purely as text/syntax tokens; neither the JVM nor the operating system ever executes uploaded code.
2. **Container Privilege Containment**: Even in the theoretical event of a zero-day exploit in the JVM or an underlying C-library, the process cannot escalate privileges, write to root filesystems, or escape the container.
3. **Resource Starvation Immunity**: Deterministic cgroup boundaries prevent noisy neighbors from impacting adjacent tenant workloads.

---

## 2. Multi-Layer Sandboxing Architecture

```
+-----------------------------------------------------------------------------------+
|                            Host Operating System Kernel                           |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               Layer 1: Seccomp-BPF System Call Filtering                          |
|   - Blocks 80+ dangerous system calls: ptrace, reboot, chroot, unshare            |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               Layer 2: Linux cgroups v2 Resource Governance                       |
|   - Hard memory cap (2GB/4GB), CPU quota clamping, OOM score tuning               |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               Layer 3: Mount Namespaces & Non-Executable Volumes                  |
|   - Read-only root filesystem (read_only: true)                                   |
|   - Ephemeral staging volume mounted with `noexec,nosuid,nodev` flags             |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               Layer 4: Least-Privilege Process Execution                          |
|   - Runs as unprivileged UID 10001 (codexa:codexa)                                |
|   - All Linux kernel capabilities dropped (cap_drop: ALL)                         |
|   - No-new-privileges flag enforced (no setuid/setgid execution)                  |
+-----------------------------------------------------------------------------------+
```

---

## 3. Filesystem Isolation & `noexec` Staging

All code extraction occurs strictly inside an ephemeral scratch directory mounted with Linux security flags:

```bash
mount -t tmpfs -o rw,noexec,nosuid,nodev,size=1024m tmpfs /tmp/codexa/staging
```

### Mount Flag Security Guarantees:
- **`noexec`**: The Linux kernel unconditionally refuses to execute any binary located on this volume. If a malicious ZIP contains an ELF binary or shell script, executing `execve()` returns `EACCES (Permission denied)`.
- **`nosuid`**: Prevents set-user-identifier or set-group-identifier bits from taking effect, neutralizing local privilege escalation exploits.
- **`nodev`**: Disallows the creation or interpretation of character or block special devices (neutralizing unauthorized hardware access).

---

## 4. Seccomp-BPF System Call Whitelist Profile

Codexa operates with a customized Seccomp profile (`codexa-seccomp.json`) that strictly blocks hazardous system calls:

```json
{
  "defaultAction": "SCMP_ACT_ALLOW",
  "architectures": [
    "SCMP_ARCH_X86_64",
    "SCMP_ARCH_AARCH64"
  ],
  "syscalls": [
    {
      "names": [
        "ptrace",
        "process_vm_readv",
        "process_vm_writev"
      ],
      "action": "SCMP_ACT_ERRNO",
      "comment": "Block process memory inspection and debugging"
    },
    {
      "names": [
        "chroot",
        "pivot_root",
        "unshare",
        "setns"
      ],
      "action": "SCMP_ACT_ERRNO",
      "comment": "Block namespace manipulation and container breakout"
    },
    {
      "names": [
        "reboot",
        "kexec_load",
        "kexec_file_load",
        "init_module",
        "delete_module"
      ],
      "action": "SCMP_ACT_ERRNO",
      "comment": "Block kernel reboot and module loading"
    }
  ]
}
```

---

## 5. Kubernetes Hardened Workload Configuration

For production Kubernetes deployments, pods are configured with the strictest Pod Security Standard (`restricted`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: codexa-backend
spec:
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 10001
        runAsGroup: 10001
        fsGroup: 10001
        seccompProfile:
          type: RuntimeDefault
      containers:
        - name: codexa-backend
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop:
                - ALL
            seccompProfile:
              type: RuntimeDefault
          resources:
            requests:
              cpu: 500m
              memory: 1024Mi
            limits:
              cpu: 2000m
              memory: 4096Mi
          volumeMounts:
            - name: staging-volume
              mountPath: /tmp/codexa/staging
      volumes:
        - name: staging-volume
          emptyDir:
            medium: Memory
            sizeLimit: 10Gi
```

---

## 6. JVM Sandboxing & AST Memory Safety

Within the Java 21 runtime, additional software boundaries enforce memory safety:
- **No Native Compilers on PATH**: Docker images omit `gcc`, `make`, `python`, `node`, `javac`, ensuring no shell injection could ever invoke a native compiler.
- **Bounded AST Graph Sizes**: JavaParser node visitors enforce recursion depth limits ($< 250$ nested AST nodes) to prevent stack overflow attacks on deeply nested pathological input.
- **64KB Buffer Limits**: Streaming decompressors strictly limit buffer sizes, preventing heap memory exhaustion attacks.
