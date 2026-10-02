# Business Continuity & Disaster Recovery (BCDR) Runbook

## 1. Overview
This runbook establishes Recovery Point Objectives (RPO) and Recovery Time Objectives (RTO) for Codexa production services.

---

## 2. Service Continuity Targets
* **Recovery Point Objective (RPO)**: <= 1 hour (SQLite WAL snapshots automated hourly).
* **Recovery Time Objective (RTO)**: <= 15 minutes (Container redeployment on secondary cloud region).

---

## 3. Database Restoration Procedure

```bash
# 1. Stop service
docker stop codexa-production

# 2. Restore SQLite database from verified backup
cp /backups/codexa-backup-latest.db /app/data/codexa.db

# 3. Verify SQLite file integrity
sqlite3 /app/data/codexa.db "PRAGMA integrity_check;"

# 4. Restart service
docker start codexa-production
```
