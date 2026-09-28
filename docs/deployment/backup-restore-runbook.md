# Codexa Database Backup, Disaster Recovery & Point-In-Time Restoration Runbook

This runbook defines the disaster recovery (DR) procedures, continuous archiving protocols, automated backup automation scripts, and integrity verification checklists for **Codexa** databases (PostgreSQL 16 and SQLite 3 WAL).

---

## 1. Disaster Recovery Objectives & SLAs

| Metric | Target SLA | Strategy |
|:---|:---:|:---|
| **Recovery Point Objective (RPO)** | **$< 15\text{ minutes}$** | Continuous WAL shipping / 15-minute incremental snapshots |
| **Recovery Time Objective (RTO)** | **$< 30\text{ minutes}$** | Automated container restore via pg_restore or hot SQLite swap |
| **Backup Retention Schedule** | **90 days** | Daily full backups retained 30 days, weekly backups retained 90 days |
| **Disaster Recovery Drill Cadence** | **Quarterly** | End-to-end restore to isolated staging environment |

---

## 2. PostgreSQL Enterprise Backup Architecture (Production)

For multi-node production clusters running PostgreSQL 16:

### A. Automated Daily Logical Dump (`pg_dump` Custom Format)
Custom compressed format (`-Fc`) enables multi-threaded parallel restoration:

```bash
#!/bin/bash
set -euo pipefail

BACKUP_DIR="/var/backups/codexa"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/codexa_db_${DATE}.dump"

mkdir -p "${BACKUP_DIR}"

# Execute parallel compressed pg_dump
PGPASSWORD="${PGPASSWORD}" pg_dump \
    -h "${PGHOST:-localhost}" \
    -p "${PGPORT:-5432}" \
    -U "${PGUSER:-codexa}" \
    -d "${PGDATABASE:-codexa_db}" \
    -Fc \
    -j 4 \
    -v \
    -f "${BACKUP_FILE}"

# Encrypt backup with GPG
gpg --symmetric --batch --yes --passphrase-file /etc/codexa/backup-key.pass "${BACKUP_FILE}"
rm -f "${BACKUP_FILE}"

# Sync encrypted archive to offsite cloud object storage
aws s3 cp "${BACKUP_FILE}.gpg" "s3://codexa-enterprise-backups/postgres/${DATE}/"
```

### B. Point-In-Time Recovery (PITR) via Continuous WAL Archiving
In `postgresql.conf`:
```ini
wal_level = replica
archive_mode = on
archive_command = 'test ! -f /mnt/wal_archive/%f && cp %p /mnt/wal_archive/%f'
archive_timeout = 900 # Flush WAL every 15 minutes
```

---

## 3. SQLite Online Backup Architecture (Standalone / Edge)

For single-node or edge installations using SQLite in WAL mode:

### Atomic Online Hot Backup (Zero Reader/Writer Lock Contention)
Never copy `codexa.db` using standard `cp` while the process is active, as this risks capturing torn pages. Instead, invoke SQLite's online backup API:

```bash
#!/bin/bash
set -euo pipefail

DB_PATH="/var/codexa/data/codexa.db"
BACKUP_PATH="/var/backups/codexa/sqlite_$(date +%Y%m%d_%H%M%S).db"

# 1. Force WAL checkpoint to consolidate uncommitted transactions
sqlite3 "${DB_PATH}" "PRAGMA wal_checkpoint(TRUNCATE);"

# 2. Perform atomic online snapshot
sqlite3 "${DB_PATH}" ".backup '${BACKUP_PATH}'"

# 3. Verify integrity of generated snapshot
sqlite3 "${BACKUP_PATH}" "PRAGMA integrity_check;"

# 4. Compress and archive
zstd -q --rm "${BACKUP_PATH}" -o "${BACKUP_PATH}.zst"
```

---

## 4. Complete Step-by-Step Restoration Runbook

### Scenario A: PostgreSQL Disaster Restoration

```bash
# Step 1: Scale down application replicas to prevent writes
kubectl scale deployment/codexa-backend --replicas=0 -n codexa

# Step 2: Download and decrypt target backup
aws s3 cp s3://codexa-enterprise-backups/postgres/20260928_120000/codexa_db_20260928_120000.dump.gpg .
gpg --decrypt --batch --passphrase-file /etc/codexa/backup-key.pass codexa_db_*.dump.gpg > restore.dump

# Step 3: Recreate clean target database
dropdb -h localhost -U postgres --if-exists codexa_db
createdb -h localhost -U postgres -O codexa codexa_db

# Step 4: Execute multi-threaded restore
pg_restore -h localhost -U codexa -d codexa_db -j 4 -v restore.dump

# Step 5: Verify table counts & latest scan records
psql -h localhost -U codexa -d codexa_db -c "SELECT COUNT(*) FROM analyses;"
psql -h localhost -U codexa -d codexa_db -c "SELECT COUNT(*) FROM findings;"

# Step 6: Scale application replicas back up
kubectl scale deployment/codexa-backend --replicas=3 -n codexa
```

### Scenario B: SQLite Disaster Restoration

```bash
# Step 1: Stop the systemd service
sudo systemctl stop codexa.service

# Step 2: Move corrupted database to quarantine
sudo mv /var/codexa/data/codexa.db /var/codexa/data/codexa.db.corrupt_$(date +%s)
sudo rm -f /var/codexa/data/codexa.db-wal /var/codexa/data/codexa.db-shm

# Step 3: Decompress backup snapshot
zstd -d /var/backups/codexa/sqlite_target.db.zst -o /var/codexa/data/codexa.db
sudo chown codexa:codexa /var/codexa/data/codexa.db
sudo chmod 640 /var/codexa/data/codexa.db

# Step 4: Verify database integrity
sqlite3 /var/codexa/data/codexa.db "PRAGMA integrity_check;"

# Step 5: Start systemd service and check health
sudo systemctl start codexa.service
curl -f http://localhost:8080/actuator/health
```

---

## 5. Post-Restoration Data Integrity Verification Checklist

After any restore procedure, execute the following verification checklist before opening the system to user traffic:

- [ ] **Database Connectivity**: Actuator `/actuator/health` reports status `UP` with valid database ping latency ($< 5\text{ ms}$).
- [ ] **Flyway Migration Status**: Verify no pending migrations via `SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;`.
- [ ] **Finding Consistency**: Execute query ensuring foreign keys match between `findings` and `analyses`:
  ```sql
  SELECT COUNT(*) FROM findings WHERE job_id NOT IN (SELECT id FROM analyses);
  -- Must return 0
  ```
- [ ] **Synthesize Test Scan**: Execute a synthetic scan of a standard test repository to verify end-to-end analysis functionality.
