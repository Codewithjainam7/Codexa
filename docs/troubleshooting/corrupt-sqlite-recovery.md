# Troubleshooting Guide: SQLite WAL Lock & Corruption Recovery

## Symptoms
- HTTP 500 responses with `org.sqlite.SQLiteException: [SQLITE_BUSY] The database file is locked`.
- Log entries showing `disk I/O error` or `file is not a database`.

## Emergency Recovery Steps

### 1. Check for Stray Lock Files
```bash
ls -l /var/codexa/data/codexa.db*
# You may see codexa.db-wal and codexa.db-shm
```

### 2. Run Integrity Check
```bash
sqlite3 /var/codexa/data/codexa.db "PRAGMA integrity_check;"
```

### 3. Dump & Rebuild from Corrupted File
If integrity check reports structural errors:
```bash
sqlite3 /var/codexa/data/codexa.db ".dump" | sqlite3 /var/codexa/data/codexa_recovered.db
mv /var/codexa/data/codexa.db /var/codexa/data/codexa_corrupt.bak
mv /var/codexa/data/codexa_recovered.db /var/codexa/data/codexa.db
```
