# Test Scenario: Kubernetes Privileged Container Audit

## Purpose
Detects Kubernetes Pod specifications enabling root capabilities or privileged container mode.

## Test Cases
1. `securityContext.privileged: true` -> **VIOLATION (CRITICAL)**.
2. `securityContext.allowPrivilegeEscalation: false` -> **PASSED**.
