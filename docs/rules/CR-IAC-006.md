# CR-IAC-006: Terraform Unencrypted Block or Object Storage

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-IAC-006` |
| **Category** | `SECURITY` / `COMPLIANCE` |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-311: Missing Encryption of Sensitive Data](https://cwe.mitre.org/data/definitions/311.html) |
| **Compliance Mapping** | SOC 2 (CC6.1), PCI-DSS (Req 3.4), HIPAA (§164.312(a)(2)(iv)) |
| **Scanner Target** | `*.tf`, `*.tfvars`, Terraform HCL configurations |

---

## 1. Vulnerability Summary
Provisioning persistent storage resources (such as AWS EBS volumes, S3 buckets, RDS database clusters, or Azure Managed Disks) without enabling server-side encryption at rest exposes sensitive corporate data, credentials, and customer PII to physical media theft, unauthorized snapshot clones, and regulatory compliance audit failure.

---

## 2. Insecure Terraform Example

```hcl
resource "aws_ebs_volume" "data_disk" {
  availability_zone = "us-east-1a"
  size              = 100

  # VIOLATION: Storage encryption is disabled or omitted
  encrypted = false
}
```

Or for AWS S3 without default encryption:
```hcl
resource "aws_s3_bucket" "audit_logs" {
  bucket = "company-production-audit-logs"
  # VIOLATION: Missing server_side_encryption_configuration
}
```

---

## 3. Secure Remediation Pattern

Always enforce encryption at rest with AWS KMS customer-managed keys (CMK) or standard platform AES-256 keys:

```hcl
resource "aws_ebs_volume" "data_disk" {
  availability_zone = "us-east-1a"
  size              = 100

  # REMEDIATION: Enforce KMS encryption at rest
  encrypted   = true
  kms_key_id  = aws_kms_key.app_encryption_key.arn

  tags = {
    Environment = "production"
    Compliance  = "SOC2"
  }
}
```

For S3 bucket server-side encryption:
```hcl
resource "aws_s3_bucket_server_side_encryption_configuration" "s3_enc" {
  bucket = aws_s3_bucket.audit_logs.id

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.app_encryption_key.arn
      sse_algorithm     = "aws:kms"
    }
    bucket_key_enabled = true
  }
}
```

---

## 4. Verification Checklist
- [ ] All persistent storage volumes declare `encrypted = true`.
- [ ] S3 buckets have default KMS encryption configured.
- [ ] KMS key rotation is enabled with minimum 365-day intervals.
