# CR-IAC-011: S3 Public Website Hosting & CloudFront Insecure Protocol

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-IAC-011` |
| **Category** | Security |
| **Severity** | High |
| **Confidence** | High |
| **CWE** | [CWE-319: Cleartext Transmission of Sensitive Information](https://cwe.mitre.org/data/definitions/319.html) |
| **OWASP Top 10** | A02:2021 - Cryptographic Failures |

---

## Description
Serving content directly from public S3 websites or configuring Amazon CloudFront CDN distributions with `viewer_protocol_policy = "allow-all"` allows unencrypted cleartext HTTP sessions between end-users and edge caches. This enables man-in-the-middle (MitM) packet interception, credential sniffing, and cache poisoning. Furthermore, public S3 buckets with permissive write or read ACLs risk data leakage and dangling DNS subdomain takeovers.

---

## Non-Compliant Code Example
```hcl
resource "aws_cloudfront_distribution" "cdn" {
  default_cache_behavior {
    viewer_protocol_policy = "allow-all"
    target_origin_id       = "s3-origin"
  }
}

resource "aws_s3_bucket_acl" "public_bucket" {
  bucket = aws_s3_bucket.site.id
  acl    = "public-read"
}
```

---

## Compliant Solution
```hcl
resource "aws_cloudfront_distribution" "cdn" {
  default_cache_behavior {
    viewer_protocol_policy = "redirect-to-https"
    target_origin_id       = "s3-origin"
  }
}

resource "aws_s3_bucket_public_access_block" "block" {
  bucket = aws_s3_bucket.site.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
```
