# Codexa Data Retention & Archival Policies

## Overview
To manage disk storage consumption and uphold data privacy compliance, Codexa runs background retention workers that purge ephemeral files according to automated schedules.

## Retention Schedules

| Artifact Category | Storage Location | Retention Duration | Automated Action |
| :--- | :--- | :--- | :--- |
| **Extracted Zip Archives** | `/tmp/codexa/uploads` | 2 hours post-analysis | Immediate recursive deletion |
| **Raw Analysis AST Trees** | In-Memory Heap Cache | 30 minutes idle | Garbage collected |
| **Historical Job Reports** | SQLite / PostgreSQL | 90 days (configurable) | Pruned or archived to Cold Storage |
| **Audit Logs** | System Event Log | 365 days | Compressed & rotated daily |

## Manual Cleanup Trigger
Administrators can initiate on-demand storage pruning via CLI or REST:
```bash
curl -X POST https://codexa.local/api/v1/admin/maintenance/prune?olderThanDays=30 \
     -H "Authorization: Bearer <ADMIN_TOKEN>"
```
