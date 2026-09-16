# Test Scenario: Terraform S3 Public Access Block

## Purpose
Ensures infrastructure-as-code definitions for AWS S3 buckets enforce public access block configurations.

## Test Cases
1. `aws_s3_bucket` without associated `aws_s3_bucket_public_access_block` -> **VIOLATION (HIGH)**.
2. S3 bucket with all `block_public_*` flags set to `true` -> **PASSED**.
