# CR-IAC-008: Public Cloud Storage Bucket Exposure

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-IAC-008` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-284: Improper Access Control](https://cwe.mitre.org/data/definitions/284.html) |
| **OWASP Top 10** | A05:2021 – Security Misconfiguration |
| **Languages** | Terraform, CloudFormation, JSON, YAML |

---

## Vulnerability Overview

Cloud storage buckets (AWS S3, Google Cloud Storage, Azure Blob Storage) left open with public read ACLs or with S3 Block Public Access controls disabled are a leading cause of enterprise data breaches. Unauthenticated external actors can list and dump company databases, backups, PII, and sensitive assets.

---

## Vulnerable Code Patterns

```hcl
# Vulnerable S3 Bucket:
resource "aws_s3_bucket" "data" {
  bucket = "company-internal-records"
  acl    = "public-read" # Critical Exposure!
}
```

---

## Secure Remediation

Block all public access and access files via signed URLs or IAM roles:

```hcl
resource "aws_s3_bucket" "data" {
  bucket = "company-internal-records"
}

resource "aws_s3_bucket_public_access_block" "data_block" {
  bucket = aws_s3_bucket.data.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
```
