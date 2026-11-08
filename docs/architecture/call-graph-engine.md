# Interprocedural Call Graph Engine

Codexa constructs typed, directed **Call Graphs** across all parsed source files.

```
 [ Controller Handler ] ──> [ Domain Service ] ──> [ Persistence Repository ]
                                    │
                                    └──> [ External Gateway / RPC ]
```

## Key Capabilities
1. **Reachability Analysis**: Determines whether vulnerable methods or deprecated calls can actually be reached from public HTTP or RPC entrypoints.
2. **Blast Radius Computation**: Calculates the upstream impact of refactoring or patching a shared utility method.
3. **Dead Code Elimination**: Pinpoints isolated subgraphs with zero incoming edges from any controller or public API entrypoint.
