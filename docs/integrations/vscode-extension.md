# Codexa Visual Studio Code Extension Guide

## 1. Overview
The Codexa VS Code Extension integrates deterministic AST security scanning, OWASP Top 10 compliance checks, and prompt injection guardrails directly into the developer's local IDE workspace.

---

## 2. Extension Architecture
The VS Code extension files reside in [`extensions/vscode/`](file:///F:/Codexa/extensions/vscode/):
```
extensions/vscode/
├── package.json       # VS Code extension manifest & contributed commands
├── src/
│   └── extension.ts   # Main activation and command handler script
└── README.md          # Extension overview and configuration
```

---

## 3. Contributed Commands

| Command | Title | Action |
| :--- | :--- | :--- |
| `codexa.scanWorkspace` | `Codexa: Run Deep AST Security Scan` | Initiates full static analysis across the active repository |
| `codexa.openPlayground` | `Codexa: Open Security Playground Sandbox` | Opens the interactive multi-language sandbox |
| `codexa.viewThreatMap` | `Codexa: View Attack Surface Threat Map` | Visualizes the active project's architectural threat map |

---

## 4. Configuration Settings

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `codexa.serverUrl` | `string` | `https://codexa-ye85.onrender.com` | Backend server URL |
| `codexa.scanOnSave` | `boolean` | `true` | Automatically run AST evaluations on file save |
