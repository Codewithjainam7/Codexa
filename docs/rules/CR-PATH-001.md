# Rule: CR-PATH-001 — Path Traversal & Zip Slip Vulnerability

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-PATH-001` |
| **Category** | `SECURITY` |
| **Severity** | `CRITICAL` |
| **Confidence** | `HIGH` |
| **CWE** | [CWE-22: Path Traversal](https://cwe.mitre.org/data/definitions/22.html) |
| **OWASP Top 10** | [A01:2021 - Broken Access Control](https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/) |

---

## 1. Description

Detects filesystem operations that resolve paths using unvalidated user input containing sequence patterns such as `../` or absolute directory roots. In archive extraction (Zip Slip), crafted zip entries with relative paths can overwrite arbitrary files outside the target extraction directory, including configuration files, system binaries, or executable classes.

---

## 2. Detection Patterns & Taint Sinks

- `new File(targetDir, entry.getName())` without normalization and containment checks.
- `Paths.get(basePath, userSuppliedFilename)`
- Python: `open(os.path.join(upload_dir, filename))`
- Node.js: `fs.readFileSync(path.join(baseDir, req.query.file))`

---

## 3. Vulnerable Code Example (Java Zip Slip)

```java
public void extract(ZipInputStream zis, Path targetDir) throws IOException {
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
        // VULNERABLE: entry.getName() can contain "../../etc/passwd"
        File targetFile = new File(targetDir.toFile(), entry.getName());
        Files.copy(zis, targetFile.toPath());
    }
}
```

---

## 4. Remediated Code Example (Java)

```java
public void extract(ZipInputStream zis, Path targetDir) throws IOException {
    Path normalizedTarget = targetDir.normalize().toAbsolutePath();
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
        Path resolved = normalizedTarget.resolve(entry.getName()).normalize().toAbsolutePath();
        // SECURE: Strict path boundary containment check
        if (!resolved.startsWith(normalizedTarget)) {
            throw new SecurityException("Zip Slip path traversal attempt: " + entry.getName());
        }
        if (entry.isDirectory()) {
            Files.createDirectories(resolved);
        } else {
            Files.createDirectories(resolved.getParent());
            Files.copy(zis, resolved, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
```

---

## 5. Defense-in-Depth Guidelines

1. Always normalize and convert paths to absolute before comparison: `targetPath.normalize().toAbsolutePath()`.
2. Verify containment using `resolvedPath.startsWith(baseDir)`.
3. Reject null bytes (`\0`) and invalid filesystem characters before path construction.
