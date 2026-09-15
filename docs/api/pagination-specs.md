# Codexa API Pagination Specifications

## Overview
All endpoints returning collections of resources (analysis jobs, detected findings, rule catalogs, and audit events) implement consistent, predictable pagination mechanisms.

## Strategy: Keyset (Cursor) vs Offset

Codexa supports both offset-based and cursor-based pagination depending on dataset scale:

| Endpoint | Supported Modes | Default Mode | Max Page Size |
| :--- | :--- | :--- | :--- |
| `/api/v1/analysis/jobs` | Offset (`page`, `size`) & Cursor (`afterCursor`) | Cursor | 100 |
| `/api/v1/analysis/{id}/findings` | Cursor (`afterCursor`, `beforeCursor`) | Cursor | 250 |
| `/api/v1/rules` | Offset (`page`, `size`) | Offset | 100 |

## Offset Pagination Parameters
- `page`: 0-indexed integer (default: `0`).
- `size`: Number of records per page (default: `20`, max: `100`).
- `sort`: Comma-separated sort attributes, e.g. `createdAt,desc` or `severity,asc`.

### Example Request
```http
GET /api/v1/analysis/jobs?page=2&size=25&sort=completedAt,desc HTTP/1.1
Host: api.codexa.dev
Authorization: Bearer <TOKEN>
```

### Example Response Envelope
```json
{
  "content": [...],
  "page": {
    "size": 25,
    "totalElements": 218,
    "totalPages": 9,
    "number": 2
  },
  "links": {
    "first": "/api/v1/analysis/jobs?page=0&size=25",
    "prev": "/api/v1/analysis/jobs?page=1&size=25",
    "self": "/api/v1/analysis/jobs?page=2&size=25",
    "next": "/api/v1/analysis/jobs?page=3&size=25",
    "last": "/api/v1/analysis/jobs?page=8&size=25"
  }
}
```

## Cursor Pagination (Recommended for Streaming Findings)
For large-scale findings exceeding 10,000 items, cursor pagination guarantees $O(1)$ database seeking without degradation:

```http
GET /api/v1/analysis/job-9912/findings?limit=50&afterCursor=ZmluZGluZy05OTMy HTTP/1.1
```
