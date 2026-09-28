# Troubleshooting & Forensic Recovery: SQLite Database Corruption & WAL Contention

This operational guide provides diagnostic commands, recovery runbooks, and prevention guidelines for resolving SQLite database corruption (`SQLITE_CORRUPT`), locking timeouts (`SQLITE_BUSY`), and Write-Ahead Log (WAL) desynchronization in **Codexa** deployments.

---

## 1. Root Causes & Failure Signatures

SQLite is exceptionally reliable; however, corruption or lock contention can occur under pathological operational conditions:

| Error Code / Log Pattern | Immediate Root Cause | Operational Context |
|:---|:---|:---|
| `[SQLITE_BUSY] The database file is locked` | Write transaction lock timeout exceeded ($> 5,000\text{ ms}$). | Multiple processes attempting simultaneous writes, or long-running transaction holding an exclusive lock. |
| `[SQLITE_CORRUPT] database disk image is malformed` | Torn pages or disk sector corruption. | Hard VM power kill or abrupt container `kill -9` during an active WAL checkpoint. |
| `[SQLITE_CANTOPEN] unable to open database file` | POSIX permissions mismatch or missing parent directory. | Service restarted under wrong UID, or directory permissions altered. |
| `[SQLITE_IOERR] disk I/O error` | Host filesystem exhaustion or transient NFS/SMB lock drop. | Codexa database hosted on network mount (NFS/EFS/CIFS) rather than local POSIX disk. |

> [!CAUTION]
> **Never store SQLite database files on network filesystems (NFS, SMB, AWS EFS).** Network filesystems implement broken or incomplete POSIX file locking semantics, which inevitably leads to database corruption under concurrent access.

---

## 2. Immediate Diagnostic Triage

Before applying destructive recovery commands, run non-destructive diagnostic checks:

```bash
# Step 1: Inspect active SQLite files in data directory
ls -lh /var/codexa/data/codexa.db*

# Output should show:
# -rw-r----- 1 codexa codexa  142M Sep 28 14:00 codexa.db
# -rw-r----- 1 codexa codexa  8.4M Sep 28 14:05 codexa.db-wal  (Write-Ahead Log)
# -rw-r----- 1 codexa codexa   32K Sep 28 14:05 codexa.db-shm  (Shared Memory Index)

# Step 2: Check for active locks using lsof or fuser
sudo fuser /var/codexa/data/codexa.db

# Step 3: Run quick diagnostic check
sqlite3 /var/codexa/data/codexa.db "PRAGMA quick_check;"

# Step 4: Run exhaustive B-tree structural check
sqlite3 /var/codexa/data/codexa.db "PRAGMA integrity_check;"
```

If `PRAGMA integrity_check` outputs `ok`, the database is structurally sound. Proceed to Section 3. If it outputs errors (e.g. `row 44 missing from index`, `Page 1845: btreeInitPage() failed`), proceed to Section 4.

---

## 3. Resolving `SQLITE_BUSY` & WAL Checkpoint Deadlocks

If the database is healthy but suffering from `SQLITE_BUSY` timeouts:

### Step 1: Force WAL Checkpoint
Force SQLite to fold all pending transactions from `codexa.db-wal` back into the main `codexa.db` file:

```bash
sqlite3 /var/codexa/data/codexa.db "PRAGMA wal_checkpoint(TRUNCATE);"
```

Output:
`0|0|0` indicates complete success (0 log pages, 0 pages checkpointed, 0 uncommitted pages).

### Step 2: Verify Busy Timeout in Application Configuration
Ensure Codexa's JDBC connection URL includes adequate busy timeout buffering (minimum 5,000 ms):

```yaml
spring:
  datasource:
    url: jdbc:sqlite:/var/codexa/data/codexa.db?busy_timeout=5000&journal_mode=WAL
```

---

## 4. Emergency Database Salvage & Rebuild Procedures

If `PRAGMA integrity_check` reports structural corruption:

### Method A: Native `.recover` Command (Preferred, Modern SQLite 3.33+)
The `.recover` command recovers structurally damaged databases more effectively than `.dump` because it ignores corrupted index pages and scans raw B-tree leafs:

```bash
# 1. Stop the Codexa service
sudo systemctl stop codexa.service

# 2. Quarantine corrupted files
sudo cp /var/codexa/data/codexa.db /var/codexa/data/codexa.db.corrupt_backup
sudo cp /var/codexa/data/codexa.db-wal /var/codexa/data/codexa.db-wal.corrupt_backup 2>/dev/null || true

# 3. Execute low-level forensic recovery
sqlite3 /var/codexa/data/codexa.db.corrupt_backup ".recover" | sqlite3 /var/codexa/data/codexa_recovered.db

# 4. Verify recovered database
sqlite3 /var/codexa/data/codexa_recovered.db "PRAGMA integrity_check;"

# 5. Swap recovered database into production
sudo mv /var/codexa/data/codexa_recovered.db /var/codexa/data/codexa.db
sudo rm -f /var/codexa/data/codexa.db-wal /var/codexa/data/codexa.db-shm
sudo chown codexa:codexa /var/codexa/data/codexa.db
sudo chmod 640 /var/codexa/data/codexa.db

# 6. Re-enable WAL mode
sqlite3 /var/codexa/data/codexa.db "PRAGMA journal_mode=WAL;"

# 7. Start service
sudo systemctl start codexa.service
```

### Method B: SQL Text Dump & Filter (`.dump`)
If `.recover` is unavailable, use `.dump` with rollback filtering:

```bash
sqlite3 /var/codexa/data/codexa.db.corrupt_backup ".dump" | \
    grep -v '^ROLLBACK' | \
    sqlite3 /var/codexa/data/codexa_dump_restored.db
```

---

## 5. Prevention Best Practices Checklist

- [ ] **Always Use Local Block Storage**: Run SQLite only on local SSDs / NVMe volumes.
- [ ] **Configure WAL Mode**: Ensure `PRAGMA journal_mode=WAL` is active.
- [ ] **Set Proper Busy Timeouts**: Minimum 5,000 ms in JDBC connection strings.
- [ ] **Automate Scheduled Vacuuming**: Execute `PRAGMA incremental_vacuum;` weekly to reclaim fragmented free-list pages.
- [ ] **Migrate to PostgreSQL for Clustering**: For deployments requiring multi-pod horizontal autoscaling, transition to PostgreSQL 16.
