# CR-IAC-010: Terraform RDS Unencrypted Storage & KMS Key Rotation Disabled

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-IAC-010` |
| **Category** | Security |
| **Severity** | High |
| **Confidence** | High |
| **CWE** | [CWE-311: Missing Encryption of Sensitive Data](https://cwe.mitre.org/data/definitions/311.html) |
| **OWASP Top 10** | A02:2021 - Cryptographic Failures |

---

## Description
Production databases storing relational records, customer data, and authentication credentials must have storage-at-rest encryption enabled using customer-managed AWS KMS keys. Furthermore, databases should never be placed in public subnets with `publicly_accessible = true`. In addition, customer-managed KMS encryption keys must enable annual automatic rotation (`enable_key_rotation = true`) according to PCI-DSS, SOC 2, and NIST 800-53 standards.

---

## Non-Compliant Code Example
```hcl
resource "aws_db_instance" "app_db" {
  allocated_storage   = 50
  engine              = "postgres"
  instance_class      = "db.t3.medium"
  name                = "production_db"
  storage_encrypted   = false
  publicly_accessible = true
}

resource "aws_kms_key" "db_key" {
  description         = "KMS Key for RDS"
  enable_key_rotation = false
}
```

---

## Compliant Solution
```hcl
resource "aws_kms_key" "db_key" {
  description             = "KMS Key for RDS"
  deletion_window_in_days = 30
  enable_key_rotation     = true
}

resource "aws_db_instance" "app_db" {
  allocated_storage   = 50
  engine              = "postgres"
  instance_class      = "db.t3.medium"
  name                = "production_db"
  storage_encrypted   = true
  kms_key_id          = aws_kms_key.db_key.arn
  publicly_accessible = false
}
```
