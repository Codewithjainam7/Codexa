# Test Scenario: Node.js Prototype Pollution

## Purpose
Validates detection of unsafe recursive object merge functions susceptible to prototype pollution (CWE-1321).

## Test Cases
1. Recursive merge without `__proto__` or `constructor` sanitization -> **VIOLATION (HIGH)**.
2. `Object.assign({}, ...)` with sanitized keys -> **PASSED**.
3. `Map` data structure usage for user-controlled keys -> **PASSED**.
