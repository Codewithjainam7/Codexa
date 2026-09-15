# Codexa Zero-Trust Security Architecture for Code Ingestion

## Threat Assumption
Codexa treats all uploaded source files, Git repositories, and metadata as inherently hostile and untrusted. The analysis engine never executes source code during analysis—all scanning is strictly static AST parsing.

## Defense-in-Depth Pillars

1. **Non-Executable Environment**: Analysis runners operate with the `noexec` mount option on temporary upload volumes.
2. **Zip-Bomb & Zip-Slip Neutralization**: Archive paths are validated before byte extraction. Max compression ratios (100:1) and maximum unpacked sizes are strictly bounded.
3. **No Dynamic Reflection/Eval**: Script engines (e.g. Nashorn, Groovy, JS eval) are completely absent from the runtime classpath.
4. **Least Privilege Process**: Backend runs as unprivileged user `codexa` (UID 10001, GID 10001) with root capabilities dropped (`cap_drop: ALL`).
