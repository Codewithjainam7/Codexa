# Codexa Batch Analysis & Multi-Repository API Specification

This document details the batch analysis API specification for enterprise customers submitting multiple repositories or microservices for parallel automated auditing.

---

## 1. Endpoint Overview

- **Endpoint:** `POST /api/v1/analyses/batch`
- **Content-Type:** `application/json`
- **Response:** `202 Accepted`

Allows security teams to enqueue up to **25 repositories** simultaneously in a single API call.

---

## 2. Request Body Schema

```json
{
  "batchName": "Q3-Microservices-Audit",
  "repositories": [
    {
      "repoUrl": "https://github.com/org/auth-service",
      "branch": "main"
    },
    {
      "repoUrl": "https://github.com/org/payment-service",
      "branch": "production"
    },
    {
      "repoUrl": "https://github.com/org/user-dashboard",
      "branch": "main"
    }
  ],
  "notificationWebhook": "https://security.corp.com/webhooks/codexa"
}
```

---

## 3. Response Schema

```json
{
  "batchId": "c9a1e84b-7f22-4a0b-8d19-4f7621c9012a",
  "status": "QUEUED",
  "totalJobs": 3,
  "jobs": [
    {
      "jobId": "8f1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
      "repoUrl": "https://github.com/org/auth-service",
      "status": "EXTRACTING"
    },
    {
      "jobId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "repoUrl": "https://github.com/org/payment-service",
      "status": "EXTRACTING"
    },
    {
      "jobId": "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "repoUrl": "https://github.com/org/user-dashboard",
      "status": "EXTRACTING"
    }
  ],
  "createdAt": "2026-09-23T00:00:00Z"
}
```

---

## 4. Batch Polling Endpoint

- **Endpoint:** `GET /api/v1/analyses/batch/{batchId}`
- Returns aggregate completion status, overall portfolio score (0–100), and individual job verdicts.
