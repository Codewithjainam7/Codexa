# CR-IAC-005: Terraform Security Group with Unrestricted Ingress (0.0.0.0/0)

| Metadata | Details |
| :--- | :--- |
| **Rule ID** | `CR-IAC-005` |
| **Category** | `SECURITY` |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-284: Improper Access Control](https://cwe.mitre.org/data/definitions/284.html) |
| **OWASP Category** | A01:2021 – Broken Access Control |
| **Cloud Framework** | CIS AWS Foundations Benchmark (Section 4.1, 4.2) |
| **Scanner Target** | `*.tf`, `*.tfvars`, Terraform HCL configurations |

---

## 1. Vulnerability Summary
Configuring cloud security group ingress rules with `cidr_blocks = ["0.0.0.0/0"]` (or IPv6 `::/0`) on sensitive administrative or database ports (such as SSH `22`, RDP `3389`, PostgreSQL `5432`, MySQL `3306`, or Redis `6379`) exposes internal infrastructure directly to the public internet. This leaves systems vulnerable to automated brute-force password spraying, zero-day network daemon exploits, and unauthorized remote access.

---

## 2. Insecure Terraform Example

```hcl
resource "aws_security_group" "db_sg" {
  name        = "production-db-sg"
  description = "Database Security Group"

  ingress {
    description = "PostgreSQL Access"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    # VIOLATION: Unrestricted internet access to internal database port
    cidr_blocks = ["0.0.0.0/0"]
  }
}
```

---

## 3. Secure Remediation Pattern

Restrict ingress strictly to internal VPC CIDR ranges, trusted bastion VPN subnets, or utilize security group references (`security_groups` / `source_security_group_id`):

```hcl
resource "aws_security_group" "db_sg" {
  name        = "production-db-sg"
  description = "Database Security Group restricted to application tier"

  ingress {
    description     = "PostgreSQL access from Application Tier only"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    # REMEDIATION: Reference application server security group instead of public CIDR
    security_groups = [aws_security_group.app_tier.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["10.0.0.0/16"]
  }
}
```

---

## 4. Verification Checklist
- [ ] No Terraform security groups open ports 22, 3389, 5432, 3306, 6379, or 27017 to `0.0.0.0/0`.
- [ ] Public ingress is permitted exclusively on ports 80/443 for public-facing Application Load Balancers.
- [ ] Database instances and private backends reside inside isolated subnets with zero public IP association.
