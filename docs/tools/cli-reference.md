# Codexa CLI & LSP Toolchain Reference Manual

This document provides a comprehensive technical reference for the **Codexa Command-Line Interface (`codexa`)**, Terminal UI (TUI) engine, and Language Server Protocol (LSP) daemon.

---

## 1. Architectural Philosophy

While Codexa provides an enterprise web dashboard and REST API, developers need frictionless security feedback directly inside their terminal and editor workflows. The Codexa CLI fulfills this need by providing:

1. **Sub-100ms Latency**: Zero initialization overhead using pure Node.js native standard libraries.
2. **Zero Dependency Footprint**: Does not require heavy node modules or network connectivity for core scanning.
3. **CI/CD Quality Gate Support**: Direct integration into Git pre-commit hooks, GitHub Actions, and GitLab CI via `--fail-on`.
4. **Editor Agnostic LSP**: Standard JSON-RPC 2.0 communication over stdio for VS Code, Cursor, Neovim, and IntelliJ.

---

## 2. CLI Command Specification

### `codexa scan [path] [options]`
Scans the target directory or file for static security vulnerabilities and code quality flaws.

| Flag | Type | Default | Description |
|:---|:---:|:---:|:---|
| `--format <fmt>` | `string` | `tui` | Output rendering: `tui`, `table`, `json`, or `sarif`. |
| `--fail-on <level>`| `string` | None | Terminates with exit code `1` if findings meet or exceed severity (`critical`, `high`, `medium`, `low`). |
| `--output <file>` | `string` | None | Directs raw JSON or SARIF report to the specified file path. |

### `codexa sbom [path]`
Extracts dependencies from `pom.xml`, `package.json`, and `requirements.txt`, cross-referencing them against known supply chain CVE advisories.

### `codexa doctor`
Runs health and readiness checks across the local developer workstation:
- Node.js runtime version verification (>= 18.0.0).
- Java 21 LTS detection and virtual thread readiness.
- Git CLI availability.
- Local AI GPU daemon check (Ollama endpoint at `http://localhost:11434`).
- Codexa Backend API probe (`http://localhost:8080`).

### `codexa rules list`
Prints the static analysis rule catalog including rule IDs (`CR-SQL-001`), CWE mappings, severities, and target file extensions.

### `codexa lsp`
Starts the Language Server Protocol daemon on standard I/O for editor integration.

---

## 3. Pre-Commit Hook Integration

To prevent insecure code from entering version control, add the following to `.git/hooks/pre-commit` or `.husky/pre-commit`:

```bash
#!/bin/sh
echo "Running Codexa Security Audit..."
node cli/bin/codexa.js scan . --fail-on high
```

If any `CRITICAL` or `HIGH` severity vulnerabilities exist, the commit is blocked automatically.
