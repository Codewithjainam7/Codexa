# Test Scenario: Python Insecure Deserialization

## Purpose
Validates Codexa rule `CR-SEC-007` against unsafe deserialization methods in Python.

## Test Cases
1. `pickle.loads(user_input)` -> **VIOLATION (CRITICAL, CWE-502)**.
2. `yaml.load(payload, Loader=yaml.Loader)` -> **VIOLATION (HIGH)**.
3. `yaml.safe_load(payload)` -> **PASSED**.
4. `json.loads(payload)` -> **PASSED**.
