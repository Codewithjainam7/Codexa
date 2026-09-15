# Codexa Database Backup & Disaster Recovery Runbook

## SQLite Backup via Online Vacuum
For standalone deployments running SQLite in WAL mode:

```bash
# Atomic online backup without locking writes
sqlite3 /var/codexa/data/codexa.db ".backup /var/backups/codexa-$(date +%Y%m%d_%H%M%S).db"
```

## PostgreSQL Dump (Enterprise Clustered Mode)

```bash
pg_dump -Fc -h db.internal -U codexa -d codexa_db > /var/backups/codexa_pg_$(date +%Y%m%d).dump
```

## Restoration Procedure
1. Stop backend service: `systemctl stop codexa`.
2. Restore file: `cp /var/backups/codexa-snapshot.db /var/codexa/data/codexa.db`.
3. Verify integrity: `sqlite3 /var/codexa/data/codexa.db "PRAGMA integrity_check;"`.
4. Start backend service: `systemctl start codexa`.
5. Check health: `curl -f http://localhost:8080/actuator/health`.
