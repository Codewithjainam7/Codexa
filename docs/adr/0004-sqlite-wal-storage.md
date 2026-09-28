# ADR-0004: Embedded SQLite with Write-Ahead Logging (WAL) and PostgreSQL Dual-Engine

## Status
**Accepted** (2026-09-04)

## Context & Problem Statement
Codexa is distributed both as a self-contained developer CLI / single-binary desktop utility and as a scalable cloud enterprise service. Requiring an external database service (e.g., MySQL, Oracle, MongoDB) for local developer usage introduces severe friction, docker-compose prerequisites, and port conflicts.

However, standard embedded SQLite in traditional rollback journal mode suffers from global database locking: any active write transaction completely blocks all concurrent readers, causing `SQLITE_BUSY` errors during parallel REST API queries.

## Decision Drivers
- **Zero-Configuration Developer Experience**: Standalone binary must run out-of-the-box (`java -jar codexa.jar`) without provisioning database servers.
- **Concurrent Read/Write Performance**: Real-time progress updates must write to the database while the frontend polls findings without deadlocks.
- **Enterprise Scalability**: Seamless migration path to managed PostgreSQL in Kubernetes multi-replica clusters.

## Considered Options
1. **H2 In-Memory / File Database**: Fast in dev, but prone to file lock corruptions on abnormal JVM shutdown and divergent SQL syntax from PostgreSQL.
2. **Mandatory External PostgreSQL**: Optimal for enterprise clusters, but excessive friction for local single-developer CLI scans.
3. **SQLite with Write-Ahead Logging (WAL) Mode + PostgreSQL Dual-Mode**: Embedded SQLite for single-node / local deployments and PostgreSQL via Spring Data JPA for clustered environments.

## Decision Outcome
Chosen option: **Dual-Mode Persistence Architecture: SQLite with WAL as Default, PostgreSQL for Enterprise Profiles**.

### SQLite WAL Mode Configuration:
```sql
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA busy_timeout = 5000;
PRAGMA foreign_keys = ON;
```

### Key Technical Properties:
- **Write-Ahead Logging (WAL)**: Readers never block writers, and writers never block readers. Concurrently running analysis scans can write findings while UI queries read historical jobs.
- **Spring Profile Separation**:
  - `dev` / `default`: Embedded SQLite / H2 for zero-setup execution.
  - `prod`: PostgreSQL 16+ enabled via `SPRING_PROFILES_ACTIVE=prod` with Flyway automated migrations.

## Consequences
- **Positive**: Single JAR execution without external dependencies.
- **Positive**: Zero `SQLITE_BUSY` contention errors under standard developer workloads.
- **Positive**: Identical JPA entities and Flyway migration scripts work across both SQLite and PostgreSQL.
- **Negative / Limitation**: SQLite WAL is not suitable for multi-replica Kubernetes clusters sharing a network filesystem (requires PostgreSQL for multi-node deployments).
