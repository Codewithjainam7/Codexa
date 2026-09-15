# Codexa Incremental Delta Analysis Engine

## Overview
In high-velocity CI/CD environments, scanning an entire multi-million line codebase on every pull request is inefficient. Codexa's Delta Analysis engine inspects only the exact files and lines touched by a commit range.

## Git Diff Parsing Workflow

```
Git Range (e.g. main...feature/auth)
      │
      ▼
LibGit2 / JGit Unified Diff Parser
      │
      ├── Extracts Modified File Paths
      ├── Maps Changed Line Spans (Hunks)
      │
      ▼
Scoped Rule Filter
      ├── Skip unchanged files entirely
      ├── Execute AST rules on modified files
      │
      ▼
Line Intersection Matcher
      └── Discard findings located outside modified line ranges (optional baseline filter)
```

## Benefits
- **Sub-Second Feedback**: PR checks run in under 2 seconds for typical 5-10 file changesets.
- **Noise Reduction**: Prevents legacy technical debt in untouched legacy files from blocking new PR merges.
