# Codexa SARIF v2.1.0 Specification & Schema Mapping

## Overview
Static Analysis Results Interchange Format (SARIF) is the OASIS open standard for static analysis tools. Codexa natively emits SARIF v2.1.0 compliant output consumable by GitHub Code Scanning, SonarQube, and VS Code.

## Schema Mapping Table

| Codexa Finding Property | SARIF JSON Path | Example Value |
| :--- | :--- | :--- |
| `ruleId` | `runs[].results[].ruleId` | `"CR-SEC-001"` |
| `severity` (`CRITICAL`) | `runs[].results[].level` | `"error"` |
| `severity` (`LOW`) | `runs[].results[].level` | `"note"` |
| `filePath` | `runs[].results[].locations[].physicalLocation.artifactLocation.uri` | `"src/main/Auth.java"` |
| `startLine` | `runs[].results[].locations[].physicalLocation.region.startLine` | `42` |
| `message` | `runs[].results[].message.text` | `"Potential SQL Injection in raw query"` |
| `remediation` | `runs[].results[].fixes[].description.text` | `"Use parameterized PreparedStatement"` |

## Output Verification
All generated SARIF files are validated on build against the official schema at `https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json`.
