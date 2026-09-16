# Test Scenario: Rust Unsafe Block Auditing

## Purpose
Audits `unsafe { ... }` blocks in Rust source code, enforcing requirement for `// SAFETY:` explanatory comments.

## Test Cases
1. `unsafe` block without preceding `// SAFETY:` rationale -> **VIOLATION (MEDIUM)**.
2. Documented `unsafe` block with verified pointer invariants -> **PASSED**.
