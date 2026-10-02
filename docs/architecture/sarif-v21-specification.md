# SARIF v2.1.0 Specification & GitHub Code Scanning Integration

## 1. Overview
SARIF (Static Analysis Results Interchange Format / OASIS Standard) provides a unified JSON schema for static analysis tool output. Codexa exports compliant **SARIF v2.1.0** reports for native ingestion into GitHub Advanced Security, GitLab Code Quality, and Azure DevOps.

---

## 2. SARIF Schema Architecture

```json
{
  "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "Codexa",
          "version": "2.4.0",
          "rules": [...]
        }
      },
      "results": [
        {
          "ruleId": "CR-SEC-001",
          "level": "error",
          "message": { "text": "SQL Injection vulnerability detected" },
          "locations": [...]
        }
      ]
    }
  ]
}
```

---

## 3. GitHub Actions Upload Workflow

```yaml
- name: Upload SARIF to GitHub Code Scanning
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: codexa-report.sarif
```
