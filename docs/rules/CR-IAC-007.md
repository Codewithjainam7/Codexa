# CR-IAC-007: Overly Permissive IAM Wildcard Policy

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-IAC-007` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-732: Incorrect Permission Assignment for Critical Resource](https://cwe.mitre.org/data/definitions/732.html) |
| **OWASP Top 10** | A01:2021 – Broken Access Control |
| **Languages** | Terraform, CloudFormation, JSON, YAML |

---

## Vulnerability Overview

Cloud IAM policies defining `Action: "*"` or `Resource: "*"` grant broad administrative capabilities to identities or services that only require a minimal subset of API calls. If the associated service account, lambda function, or container is compromised, the attacker inherits full administrative power over the cloud organization.

---

## Vulnerable Code Patterns

```hcl
# Vulnerable Terraform:
resource "aws_iam_policy" "wildcard_policy" {
  name = "app-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "*"
      Resource = "*"
    }]
  })
}
```

---

## Secure Remediation

Explicitly enumerate required API actions and limit scope to target resource ARNs:

```hcl
resource "aws_iam_policy" "least_privilege" {
  name = "app-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:GetObject",
        "s3:PutObject"
      ]
      Resource = "${aws_s3_bucket.app_data.arn}/*"
    }]
  })
}
```
