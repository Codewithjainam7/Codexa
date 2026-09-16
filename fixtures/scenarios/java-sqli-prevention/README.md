# Test Scenario: Java SQL Injection Prevention

## Purpose
Validates Codexa rule `CR-SEC-001` against raw SQL concatenation and verifies proper detection of prepared statements.

## Test Cases
1. `DriverManager.getConnection().createStatement().executeQuery(...)` with string concatenation -> **VIOLATION (CRITICAL)**.
2. `PreparedStatement` with parameterized placeholders `?` -> **PASSED**.
3. Spring Data JPA `@Query` with named parameter `:id` -> **PASSED**.
