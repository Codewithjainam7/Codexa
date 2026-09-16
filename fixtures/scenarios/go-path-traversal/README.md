# Test Scenario: Go Path Traversal Prevention

## Purpose
Validates Codexa rule `CR-SEC-004` against unconstrained file path concatenation in Go.

## Test Cases
1. `os.Open(filepath.Join(baseDir, userInput))` without boundary check -> **VIOLATION (HIGH, CWE-22)**.
2. `filepath.Clean(userInput)` with `strings.HasPrefix` validation -> **PASSED**.
3. `os.Root` (Go 1.24+ sandboxed filesystem) -> **PASSED**.
