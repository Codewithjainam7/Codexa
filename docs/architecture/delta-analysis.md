# Codexa Delta Analysis & Incremental PR Scanning

This architecture specification details Codexa's delta analysis capabilities, explaining how Git commit range diffs, modified-file filtering, and incremental AST comparison enable sub-second pull request checks in CI/CD pipelines.

---

## 1. Delta Analysis Objectives

Scanning massive enterprise repositories (e.g. 100,000 files) on every commit or pull request introduces unnecessary compute latency. A typical PR modifies only 3 to 15 files.

Delta analysis restricts AST parsing strictly to:
1. Files modified, added, or renamed between `target_branch` (e.g. `main`) and `feature_branch`.
2. Cross-file symbols and interfaces directly referenced by modified files.

---

## 2. Git Diff Extraction Pipeline

During CI/CD invocations (e.g. GitHub Actions PR trigger):
```bash
# Extract list of changed source files relative to target branch
git diff --name-only origin/main...HEAD > changed_files.txt
```

Codexa's ingestion engine accepts a file whitelist parameter:
```json
{
  "repoUrl": "https://github.com/org/repo",
  "commitSha": "a1b2c3d4",
  "baseCommitSha": "f9e8d7c6",
  "changedFilesOnly": true
}
```

---

## 3. Incremental Score Recalculation

Instead of scanning unchanged files from scratch:
1. Base scores are loaded from the baseline scan of the target branch (`main`).
2. Only modified files are parsed into ASTs and evaluated against the 32+ rules.
3. Newly introduced findings or resolved findings adjust the baseline score incrementally:
   $$\text{DeltaSecurityPenalty} = \text{NewCriticalCount} \times 30.0 + \text{NewHighCount} \times 15.0 - \text{ResolvedPenalty}$$
4. A PR gating check passes or fails within **&lt; 2 seconds**.
