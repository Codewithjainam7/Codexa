# Codexa Batch Code Review API Specification

## Overview
The Batch Analysis API enables enterprise security operations to queue multi-repository or multi-branch code reviews within a unified parent execution context.

## Batch Job Life Cycle
1. **Creation**: Client submits a manifest of Git repositories or uploaded archive hashes.
2. **Scheduling**: Codexa distributes individual repository scans across the virtual-thread worker pool.
3. **Aggregation**: Findings from all child jobs are normalized, deduplicated, and scored.
4. **Completion**: A consolidated batch report and SARIF bundle are generated.

## Endpoint: Initiate Batch Review
```http
POST /api/v1/analysis/batch HTTP/1.1
Content-Type: application/json
Authorization: Bearer <TOKEN>

{
  "batchName": "Q3-2026-Enterprise-Audit",
  "priority": "HIGH",
  "notificationWebhook": "https://secops.example.com/webhooks/codexa",
  "targets": [
    {
      "repositoryUrl": "https://github.com/org/payment-service.git",
      "branch": "main",
      "ruleProfile": "STRICT_SECURITY"
    },
    {
      "repositoryUrl": "https://github.com/org/auth-service.git",
      "branch": "develop",
      "ruleProfile": "OWASP_TOP10"
    }
  ]
}
```

### Response (202 Accepted)
```json
{
  "batchId": "batch-f98a2e1",
  "status": "QUEUED",
  "totalJobs": 2,
  "submittedAt": "2026-09-15T17:50:00Z",
  "links": {
    "statusUrl": "/api/v1/analysis/batch/batch-f98a2e1",
    "cancelUrl": "/api/v1/analysis/batch/batch-f98a2e1/cancel"
  }
}
```
