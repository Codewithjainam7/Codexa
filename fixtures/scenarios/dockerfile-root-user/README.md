# Test Scenario: Dockerfile Non-Root User Enforcement

## Purpose
Enforces container security best practice requiring explicit `USER` directive rather than running as root.

## Test Cases
1. `Dockerfile` with missing `USER` directive before `ENTRYPOINT` -> **VIOLATION (MEDIUM)**.
2. `Dockerfile` declaring `USER nonroot:nonroot` -> **PASSED**.
