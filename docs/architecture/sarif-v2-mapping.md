# Codexa SARIF v2.1.0 Specification & Schema Mapping Reference

This specification defines how **Codexa** findings, severities, rule taxonomy descriptors, and source locations map to the **OASIS Static Analysis Results Interchange Format (SARIF) v2.1.0** standard (JSON Schema: `https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json`).

---

## 1. SARIF Document Root Hierarchy

Codexa generates SARIF output adhering strictly to Section 3 of the OASIS standard:

```
{
  "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "Codexa",
          "version": "1.3.0",
          "informationUri": "https://codexa.dev",
          "rules": [ ... ],
          "taxonomies": [ ... ]
        }
      },
      "results": [ ... ]
    }
  ]
}
```

---

## 2. Driver Rules & Taxonomy Metadata Mapping

Each unique rule discovered during analysis is registered in the `runs[0].tool.driver.rules` array to provide IDEs and dashboards with rich documentation and classification tags.

### Rule Object Schema Mapping
| Codexa Rule Property | SARIF `reportingDescriptor` JSON Path | Description |
|:---|:---|:---|
| `getRuleId()` | `rules[].id` | Alphanumeric rule ID (e.g. `CR-SQL-001`). |
| `getName()` | `rules[].name` | Identifier name without spaces (e.g. `SqlInjection`). |
| `getName()` | `rules[].shortDescription.text` | Brief human-readable rule summary. |
| `getDescription()` | `rules[].fullDescription.text` | Detailed technical explanation and vulnerability rationale. |
| `getSeverity()` | `rules[].defaultConfiguration.level` | Base alert level (`error`, `warning`, `note`). |
| `getOwaspMapping()` | `rules[].relationships[].target.id` | Reference to external taxonomies (CWE or OWASP Top 10). |
| `getCategory()` | `rules[].properties.category` | Custom property bag: `SECURITY`, `QUALITY`, `PERFORMANCE`, etc. |

### Taxonomies Mapping (`SarifTaxonomyDescriptor`)
Codexa declares external taxonomy definitions in `runs[0].taxonomies`:
1. **MITRE CWE**: Name `"CWE"`, version `"4.13"`, informationUri `"https://cwe.mitre.org/"`, organization `"MITRE"`.
2. **OWASP Top 10**: Name `"OWASP Top 10"`, version `"2021"`, informationUri `"https://owasp.org/Top10/"`, organization `"OWASP"`.

```json
{
  "id": "CR-SQL-001",
  "name": "SqlInjection",
  "shortDescription": {
    "text": "SQL Injection in Concatenated Query"
  },
  "fullDescription": {
    "text": "Detected raw string concatenation or dynamic parameter interpolation inside an executable SQL query."
  },
  "defaultConfiguration": {
    "level": "error"
  },
  "relationships": [
    {
      "target": {
        "id": "CWE-89",
        "index": 0,
        "toolComponent": {
          "name": "CWE"
        }
      },
      "kinds": ["superset"]
    }
  ],
  "properties": {
    "tags": ["security", "injection", "cwe-89", "owasp-a03"],
    "precision": "very-high"
  }
}
```

---

## 3. Finding to Result Mapping (`results[]`)

Every `FindingEntity` generated during pipeline evaluation maps to a `result` object in `runs[0].results`:

### Detailed Property Mapping Table
| Codexa Finding Field | SARIF JSON Path | Transformation / Logic | Example Value |
|:---|:---|:---|:---|
| `ruleId` | `results[].ruleId` | Direct assignment | `"CR-SQL-001"` |
| `severity` | `results[].level` | Severity mapping function | `"error"` |
| `message` | `results[].message.text` | Primary finding description | `"Raw SQL query concatenation detected."` |
| `filePath` | `results[].locations[].physicalLocation.artifactLocation.uri` | Forward-slash normalized relative URI | `"src/main/OrderDao.java"` |
| `startLine` | `results[].locations[].physicalLocation.region.startLine` | 1-indexed line number | `42` |
| `endLine` | `results[].locations[].physicalLocation.region.endLine` | 1-indexed line number | `46` |
| `snippet` | `results[].locations[].physicalLocation.region.snippet.text` | Source excerpt around finding | `"String q = \"SELECT * FROM users WHERE id='\" + id;"` |
| `deduplicationHash` | `results[].partialFingerprints.primaryLocationLineHash` | SHA-256 finding fingerprint | `"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"` |
| `remediation` | `results[].fixes[].description.text` | Actionable remediation advice | `"Replace raw statement with PreparedStatement parameter binding."` |

---

## 4. Severity Level Conversion Matrix

SARIF restricts `level` values to four discrete states: `error`, `warning`, `note`, and `none`. Codexa maps internal five-tier severities deterministically:

| Codexa Severity | SARIF `level` | GitHub Security Dashboard Display | CI Gate Impact |
|:---|:---|:---|:---|
| **CRITICAL** | `error` | High Severity Alert (Red Badge) | Fails build if gate threshold $\ge \text{HIGH}$ |
| **HIGH** | `error` | Error Alert (Red Badge) | Fails build if gate threshold $\ge \text{HIGH}$ |
| **MEDIUM** | `warning` | Warning Alert (Yellow Badge) | Fails build if gate threshold $\ge \text{MEDIUM}$ |
| **LOW** | `note` | Informational Notice (Blue Badge) | Non-blocking |
| **INFO** | `note` | Informational Notice (Blue Badge) | Non-blocking |

---

## 5. URI Normalization & Path Canonization

To guarantee seamless integration across GitHub Actions, Azure Pipelines, and local VS Code instances, Codexa enforces strict URI canonicalization on `artifactLocation.uri`:

1. **Staging Prefix Stripping**: Temporary extraction prefixes (e.g. `/tmp/codexa/staging/9b1deb4d/`) are stripped. The emitted URI is strictly relative to the repository workspace root.
2. **Path Separator Uniformity**: Windows backslashes (`\`) are converted to standard RFC 3986 forward slashes (`/`).
3. **Leading Slash Avoidance**: URIs never begin with a leading `/` (e.g., `src/main/Auth.java`, not `/src/main/Auth.java`), complying with GitHub Code Scanning ingestion guidelines.

---

## 6. Complete SARIF Output Example

```json
{
  "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "Codexa",
          "version": "1.3.0",
          "informationUri": "https://codexa.dev",
          "rules": [
            {
              "id": "CR-SQL-001",
              "name": "SqlInjection",
              "shortDescription": {
                "text": "SQL Injection in Concatenated Query"
              },
              "defaultConfiguration": {
                "level": "error"
              }
            }
          ]
        }
      },
      "results": [
        {
          "ruleId": "CR-SQL-001",
          "level": "error",
          "message": {
            "text": "Potential SQL Injection via unparameterized string concatenation."
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": {
                  "uri": "src/main/java/com/example/UserRepository.java",
                  "uriBaseId": "%SRCROOT%"
                },
                "region": {
                  "startLine": 45,
                  "endLine": 45,
                  "snippet": {
                    "text": "String query = \"SELECT * FROM accounts WHERE id = '\" + accountId + \"'\";"
                  }
                }
              }
            }
          ],
          "partialFingerprints": {
            "primaryLocationLineHash": "4a5c88b22e70c8a6791b920d3648e89547d0de0b0ecbe992cbb9352e8d35d259"
          },
          "fixes": [
            {
              "description": {
                "text": "Use PreparedStatement query parameter binding instead of string concatenation."
              }
            }
          ]
        }
      ]
    }
  ]
}
```
