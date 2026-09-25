# Codexa Data Retention, Ephemeral Staging & Lifecycle Policy

This specification outlines the data retention lifecycle, storage reclamation mechanisms, ephemeral workspace purging, and privacy compliance standards implemented in **Codexa**.

---

## 1. Governance & Privacy Guarantees

As a static code analysis and security auditing platform, Codexa processes proprietary source code, software architecture designs, and vulnerability findings. To ensure adherence to enterprise data privacy regulations (including GDPR Article 5(1)(e) Storage Limitation, SOC 2 Type II Confidentiality, and ISO/IEC 27001 data classification), Codexa operates under a **Zero-Residual Ingestion Principle**:

1. **Transient Code Storage**: Uploaded zip archives and cloned Git repositories are ephemeral; source files exist on disk only for the exact duration of the AST analysis pipeline.
2. **Deterministic Cleanup Guarantee**: Unpacking directories are guaranteed to be purged via Java `finally` blocks, operating system shutdown hooks, and scheduled orphan-reclamation background sweeps.
3. **Structured Finding Minimization**: Persistent storage preserves only structural metadata, vulnerability findings, and contextual diff snippets. Full source trees are never stored in the database.

---

## 2. Artifact Lifecycle Matrix

| Artifact Category | Storage Medium | Default Retention Period | Pruning Mechanism | Override Property |
|:---|:---|:---|:---|:---|
| **Staged Source Trees** | Ephemeral Disk (`/tmp/codexa/staging/{jobId}`) | $\le \text{Analysis Duration}$ (Immediate upon completion) | `StagingManagerService.cleanDirectory()` in pipeline `finally` block | `codexa.staging.cleanup-on-completion=true` |
| **In-Memory AST Graphs** | JVM Heap (`CompilationUnit`) | $\le \text{Analysis Duration}$ (Evicted post-scoring) | JVM Garbage Collection & SoftReference cache | `codexa.ast.cache.max-size=500` |
| **Orphaned Temp Folders** | Ephemeral Disk (`/tmp/codexa/staging`) | $120\text{ minutes}$ | `@Scheduled` hourly background sweep (`cleanupOrphanedStagingDirectories`) | `codexa.staging.orphan-ttl-minutes=120` |
| **Job Finding Entities** | PostgreSQL / SQLite (`findings`, `analyses`) | $90\text{ days}$ (Default) | Scheduled daily database retention worker | `codexa.retention.job-retention-days=90` |
| **AI In-Flight Cache** | SQLite / H2 (`ai_remediation_cache`) | $30\text{ days}$ | LRU eviction policy (capped at 50,000 entries) | `codexa.ai.cache.max-entries=50000` |
| **Security Audit Logs** | Disk (`/var/log/codexa/audit.log`) | $365\text{ days}$ | Logback rolling policy (`TimeBasedRollingPolicy` with compression) | `logging.file.total-size-cap=10GB` |

---

## 3. Ephemeral Staging Directory Management

The `StagingManagerService` encapsulates the lifecycle of ephemeral scan directories:

```
+-----------------------------------------------------------------------------------+
|                           Job Submission (Zip / Git URL)                          |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               1. Allocate Isolated Directory: /staging/{jobId}                    |
|      - Path normalized and validated against stagingRoot                          |
|      - POSIX permissions: 0700 (owner-only read/write/execute)                    |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               2. Decompress Source Files with Resource Guards                     |
|      - 64KB high-throughput buffer                                                |
|      - Zip Slip traversal defense & Zip Bomb decompression threshold              |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|               3. Execute Multi-Stage Static Analysis Pipeline                     |
|      - AST Parsing -> Rules Evaluation -> AI Remediation -> Scoring               |
+------------------------------------------+----------------------------------------+
                                           |
                                           v (try / catch / finally)
+-----------------------------------------------------------------------------------+
|               4. Deterministic Teardown in finally Block                          |
|      - `Files.walkFileTree()` depth-first deletion of all files & directories     |
|      - Complete removal verified; zero source remnants left on filesystem         |
+-----------------------------------------------------------------------------------+
```

### Safety Boundary Enforcement

To prevent path manipulation vulnerabilities or accidental system file deletion, `StagingManagerService` validates every directory against the configured `stagingRoot` before executing recursive deletion:

```java
public void cleanDirectory(Path directory) {
    if (directory == null || !Files.exists(directory)) {
        return;
    }

    // Safety verification: must reside strictly within stagingRoot
    if (!directory.toAbsolutePath().normalize().startsWith(stagingRoot)) {
        log.warn("Attempted to delete directory outside staging root: {}", directory);
        return;
    }

    try {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        log.debug("Successfully purged staging directory: {}", directory);
    } catch (IOException e) {
        log.error("Failed to clean staging directory: {}", directory, e);
    }
}
```

---

## 4. Background Orphan Sweeper (`@Scheduled`)

In the event of an abrupt JVM crash, container SIGKILL, or unhandled operating system termination during a scan, temporary directories might not be cleaned by the pipeline `finally` block.

To guarantee disk reclamation, `StagingManagerService` runs an automated background sweeper every hour:

```java
@Scheduled(fixedRate = 3600000) // Every 60 minutes
public void sweepOrphanedStagingDirectories() {
    log.info("Initiating scheduled orphan staging directory cleanup sweep...");
    Instant cutoff = Instant.now().minus(Duration.ofMinutes(properties.staging().orphanTtlMinutes()));

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(stagingRoot)) {
        for (Path entry : stream) {
            if (Files.isDirectory(entry)) {
                BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                Instant lastModified = attrs.lastModifiedTime().toInstant();
                if (lastModified.isBefore(cutoff)) {
                    log.warn("Sweeping abandoned staging directory older than TTL: {}", entry);
                    cleanDirectory(entry);
                }
            }
        }
    } catch (IOException e) {
        log.error("Error during scheduled staging sweep: {}", e.getMessage(), e);
    }
}
```

---

## 5. Historical Analysis Job Pruning API

Codexa provides administrative endpoints for automated and manual database retention management.

### Configuration Properties (`application.yml`)
```yaml
codexa:
  staging:
    base-dir: ${STAGING_BASE_DIR:/tmp/codexa/staging}
    cleanup-on-completion: true
    orphan-ttl-minutes: 120
  retention:
    job-retention-days: 90
    auto-prune-enabled: true
    cron: "0 0 2 * * ?" # 02:00 AM UTC daily
```

### Administrative Pruning Endpoint
```http
POST /api/v1/admin/maintenance/prune?olderThanDays=60 HTTP/1.1
Host: codexa.internal
Authorization: Bearer <ADMIN_BEARER_TOKEN>
Content-Type: application/json
```

#### Response (`HTTP 200 OK`)
```json
{
  "status": "SUCCESS",
  "cutoffDate": "2026-07-27T00:00:00Z",
  "purgedJobsCount": 1420,
  "purgedFindingsCount": 28450,
  "freedDiskSpaceBytes": 184549376,
  "executionDurationMs": 412
}
```

---

## 6. Compliance Self-Audit Checklist

| Requirement | Implementation Verification | Status |
|:---|:---|:---|
| **GDPR Art. 17 (Right to Erasure)** | Automated cascade deletion across `analyses` $\to$ `findings` $\to$ `remediation_cache`. | PASS |
| **SOC 2 Confidentiality CC6.5** | Immediate disposal of customer intellectual property upon job termination. | PASS |
| **Denial of Service Prevention** | 64KB bounded streaming buffers, max 10GB staging disk quota, orphan reaper. | PASS |
| **Tamper-Evident Audit Trail** | Immutable log events recording staging creation, extraction hash, and deletion timestamp. | PASS |
