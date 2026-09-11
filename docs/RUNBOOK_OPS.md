# Incident Response & Operations Runbook

Operational procedures for cluster administrators running Codexa in production.

## 1. High Memory Utilization Alert (P1)
- **Symptom**: JVM heap memory usage exceeds 85% for > 3 minutes.
- **Cause**: Parsing extremely large repositories (> 50,000 files) simultaneously.
- **Remediation**:
  1. Inspect `/api/v1/jobs` for active concurrent ingestion jobs.
  2. Throttle ingestion workers using `codexa.pipeline.max-concurrent-scans=2`.
  3. Increase JVM heap max: `-Xmx4g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=45`.

## 2. Ingestion Disk Staging Full Alert (P2)
- **Symptom**: Staging directory disk space < 10%.
- **Remediation**:
  1. Trigger immediate cleanup of finished jobs:
     ```bash
     curl -X POST http://localhost:8080/api/v1/admin/cleanup-staging
     ```
  2. Verify retention period policy in `application.properties`:
     `codexa.storage.temp-retention-hours=24`.
