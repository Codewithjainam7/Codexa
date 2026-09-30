# Codexa IntelliJ IDEA Plugin Guide

## 1. Overview
The Codexa IntelliJ IDEA Plugin embeds deterministic Java AST code analysis, regulatory compliance checks, and prompt injection defense into JetBrains IDEs (IntelliJ IDEA Ultimate & Community, PyCharm, and WebStorm).

---

## 2. Plugin Descriptor & Structure
The plugin descriptor resides in [`extensions/intellij/`](file:///F:/Codexa/extensions/intellij/):
```
extensions/intellij/
├── resources/
│   └── META-INF/
│       └── plugin.xml    # JetBrains plugin configuration descriptor
└── README.md             # Developer and usage instructions
```

---

## 3. Contributed Actions & Menus
The plugin registers a top-level action group under the **Analyze** menu in IntelliJ:
* **`Analyze > Codexa Security > Run Codexa AST Security Audit`**: Evaluates all Java, Python, and configuration files in the project.
* **`Analyze > Codexa Security > Open Security Playground`**: Opens the interactive sandbox directly in the IDE browser tool window.

---

## 4. Connection to Cloud & Local Engines
By default, the plugin connects to the live production deployment at `https://codexa-ye85.onrender.com`. For air-gapped enterprise environments, it can be pointed to an internal on-premises Codexa instance.
