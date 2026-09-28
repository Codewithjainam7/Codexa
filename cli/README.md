# Codexa Developer CLI & LSP Daemon (`@codexa/cli`)

The official command-line interface and Language Server Protocol daemon for **Codexa**—enabling instant static analysis, security auditing, software bill of materials (SBOM) extraction, and editor integration with zero external dependencies.

---

## 🚀 Key Highlights

- **⚡ Blazing Fast**: Scans thousands of lines across polyglot repositories in under 50ms using native Node.js.
- **📦 Zero Heavy Dependencies**: Operates entirely using native Node.js built-ins. No massive `node_modules` required.
- **🛡️ 30+ Polyglot Rules**: Audits SQL Injection, OS Command Injection, Hardcoded Secrets, Insecure Deserialization, Path Traversal, and Weak Cryptography.
- **🎨 Monochromatic Luxury TUI**: ASCII score gauges, color-coded severity badges, and production readiness gates (`PRODUCTION_READY`, `REQUIRES_REVIEW`, `BLOCKED`).
- **🌐 Standard Output Formats**: Human-readable TUI, tabulated summary, raw JSON, and OASIS SARIF v2.1.0 for GitHub code scanning integration.
- **🔌 Language Server Protocol (LSP)**: Native JSON-RPC 2.0 daemon for real-time in-editor security diagnostics in VS Code, Cursor, and Neovim.
- **🩺 Integrated System Doctor**: Probes Node.js, Java 21, Git, local Ollama/GPU status, and backend connectivity.

---

## 📥 Installation & Usage

### 1. Run Directly with Node
```bash
# Scan current directory
node cli/bin/codexa.js scan .

# Scan specific directory and output SARIF
node cli/bin/codexa.js scan ./backend --format sarif --output results.sarif

# Enforce CI quality gate (exit code 1 on high/critical)
node cli/bin/codexa.js scan . --fail-on high
```

### 2. Global / Local npm link
```bash
cd cli
npm link
codexa scan .
codexa doctor
codexa rules list
```

---

## 🛠️ Command Reference

| Command | Description |
|:---|:---|
| `codexa scan [path]` | Recursively scans files, computes production readiness score, and renders findings. |
| `codexa sbom [path]` | Extracts Software Bill of Materials (SBOM) and detects supply chain CVE advisories. |
| `codexa doctor` | Probes system prerequisites (Node, Java 21, Git, local AI GPU / Ollama, and backend API). |
| `codexa rules list` | Displays the active static analysis rules catalog with CWE mappings and severity levels. |
| `codexa lsp` | Launches Language Server Protocol daemon over stdio for IDE integration. |

---

## 💻 Editor & IDE Integration (LSP)

To connect Codexa LSP to **VS Code** or **Cursor**, add the following server configuration in your extension or `settings.json`:

```json
{
  "codexa.lsp.serverPath": "node",
  "codexa.lsp.args": ["path/to/codexa/cli/bin/codexa.js", "lsp"]
}
```

The LSP server listens on `stdio` and emits `textDocument/publishDiagnostics` upon file open and save events.
