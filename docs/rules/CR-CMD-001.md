# Rule: CR-CMD-001 — OS Command Injection & Dynamic Execution

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-CMD-001` |
| **Category** | `SECURITY` |
| **Severity** | `CRITICAL` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-78: OS Command Injection](https://cwe.mitre.org/data/definitions/78.html) |
| **OWASP Top 10** | [A03:2021 - Injection](https://owasp.org/Top10/2021/A03_2021-Injection/) |

---

## 1. Description

Detects programmatic execution of operating system commands where untrusted input strings are directly formatted or concatenated into shell invocations without array separation or input whitelisting. An attacker who supplies shell metacharacters (`;`, `|`, `&`, `$()`, `\n`) can execute arbitrary host commands with the privileges of the application process.

---

## 2. Detection Patterns & Taint Sinks

- **Java**:
  - `Runtime.getRuntime().exec("sh -c " + userInput)`
  - `new ProcessBuilder("sh", "-c", userInput)`
- **Python**:
  - `os.system("ping " + host)`
  - `subprocess.Popen(..., shell=True)`
- **Node.js**:
  - `child_process.exec("git clone " + repoUrl)`

---

## 3. Vulnerable Code Example (Java)

```java
public void pingHost(String target) throws IOException {
    // VULNERABLE: Direct string passed into system shell
    Process process = Runtime.getRuntime().exec("ping -c 1 " + target);
}
```

---

## 4. Remediated Code Example (Java)

```java
public void pingHost(String target) throws IOException {
    // SECURE 1: Strict input validation regex
    if (!target.matches("^[a-zA-Z0-9.-]+$")) {
        throw new IllegalArgumentException("Invalid hostname format");
    }

    // SECURE 2: Argument array prevents shell metacharacter expansion
    ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", target);
    pb.redirectErrorStream(true);
    Process process = pb.start();
}
```

---

## 5. Remediation Best Practices

1. **Avoid OS shells entirely**: Prefer native language library APIs (e.g. `java.net.InetAddress.getByName()` instead of running `ping`).
2. **Use argument arrays**: Always invoke binaries with discrete argument tokens rather than a single interpolated shell string.
3. **Never invoke with `shell=True` / `sh -c`**: Bypasses OS argument separation and triggers shell command parsing.
