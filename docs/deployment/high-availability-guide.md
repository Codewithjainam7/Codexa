# Codexa High Availability & Multi-Node Clustering Architecture

This document details the high-availability (HA) architecture, stateless horizontal scaling patterns, distributed task coordination, database connection pool tuning, and zero-downtime rolling upgrade strategies for **Codexa**.

---

## 1. High-Availability Architectural Topology

To achieve **99.99% service availability** in enterprise environments, Codexa operates as a completely stateless, horizontally scalable compute cluster backed by managed PostgreSQL and distributed coordination locks:

```
                            [ Cloud Load Balancer / Ingress ]
                         (Round Robin / Least Connections / TLS 1.3)
                                          │
            ┌─────────────────────────────┼─────────────────────────────┐
            ▼                             ▼                             ▼
   [ Codexa Worker Pod 1 ]       [ Codexa Worker Pod 2 ]       [ Codexa Worker Pod N ]
   (Stateless Spring Boot)       (Stateless Spring Boot)       (Stateless Spring Boot)
   ├── Local Memory Cache        ├── Local Memory Cache        ├── Local Memory Cache
   └── Ephemeral Staging         └── Ephemeral Staging         └── Ephemeral Staging
            │                             │                             │
            └─────────────────────────────┼─────────────────────────────┘
                                          │
                  ┌───────────────────────┴───────────────────────┐
                  ▼                                               ▼
     [ Distributed ShedLock ]                      [ PostgreSQL 16 Primary (RDS / Multi-AZ) ]
(Single Scheduled Job Execution)                    ├── Flyway Managed Schema
                                                    ├── Synchronous Standby Replica (Failover < 30s)
                                                    └── HikariCP Tuned Connection Pool
```

### Core HA Invariants:
1. **Zero Session State**: Authentication relies on cryptographically signed, stateless tokens (PATs and JWTs). Any worker pod can handle any incoming API request without session stickiness.
2. **Independent Ephemeral Staging**: Each worker pod maintains its own scratch directory for archive decompression and AST parsing. Staging paths are never shared across a distributed network filesystem (avoiding NFS/EFS lock contention and latency).
3. **Graceful Worker Drain**: Terminating worker instances complete active static scans before closing database connections and shutting down.

---

## 2. Distributed Task Coordination (ShedLock Architecture)

Because multiple worker replicas run concurrently, scheduled background tasks—such as orphaned file sweeps and historical database pruning—must execute **on exactly one node at a time** to prevent race conditions:

### ShedLock Table Definition (`V2__create_shedlock_table.sql`)
```sql
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

### Spring Boot ShedLock Configuration
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime() // Synchronize lock clocks to database server time
                .build()
        );
    }
}
```

### Protected Scheduled Task Example
```java
@Component
public class ScheduledMaintenanceService {

    @Scheduled(cron = "0 0 * * * *") // Hourly sweep
    @SchedulerLock(name = "OrphanStagingSweep", lockAtMostFor = "15m", lockAtLeastFor = "5m")
    public void sweepOrphanedStaging() {
        log.info("Acquired distributed ShedLock: executing staging cleanup...");
        stagingManagerService.sweepOrphanedStagingDirectories();
    }
}
```

---

## 3. Database Connection Pool Optimization (HikariCP)

Codexa utilizes **HikariCP** for high-performance database connection pooling. In multi-node deployments, pool sizes must be tuned to prevent PostgreSQL backend connection exhaustion:

### Optimal Pool Sizing Formula
$$\text{PoolSizePerNode} = \frac{\text{PostgreSQL Max Connections} - \text{Superuser Reserved}}{\text{Worker Node Replicas}}$$

For a 3-replica cluster connected to a PostgreSQL database configured with `max_connections = 100`:
- Reserved for maintenance/pgAdmin: $10$
- Available pool pool budget: $90$ connections
- Max pool per node: $\frac{90}{3} = 30$ connections

### Recommended `application.yml` Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      idle-timeout: 300000        # 5 minutes
      max-lifetime: 1800000       # 30 minutes
      connection-timeout: 20000   # 20 seconds
      leak-detection-threshold: 60000 # Warn if query holds connection > 60s
      pool-name: CodexaHikariPool
```

---

## 4. Graceful Shutdown & Zero-Downtime Rolling Deployments

When deploying updates or scaling in worker nodes, active analysis jobs must complete without being killed mid-scan:

### Spring Boot Graceful Shutdown Configuration
```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 45s
```

### Kubernetes Pod Lifecycle Hooks
```yaml
lifecycle:
  preStop:
    exec:
      # Sleep 5 seconds to allow Ingress / Service endpoints to de-register pod IP
      command: ["/bin/sh", "-c", "sleep 5"]
```

---

## 5. High-Availability Disaster Recovery (DR) Checklist

| Failure Scenario | Automatic Detection & Recovery Mechanism | Target RTO / RPO |
|:---|:---|:---:|
| **Worker Pod Crash** | Kubernetes restart policy restarts container; Kube-proxy removes IP from service endpoints. | $\text{RTO} < 5\text{ s}$<br>$\text{RPO} = 0$ |
| **Primary DB Failover** | AWS Multi-AZ / Patroni promotes standby replica; HikariCP reconnects automatically within 20s. | $\text{RTO} < 30\text{ s}$<br>$\text{RPO} < 1\text{ s}$ |
| **High Scan Concurrency** | Kubernetes HPA detects CPU $> 75\%$ and provisions additional worker replicas dynamically. | $\text{RTO} < 60\text{ s}$<br>$\text{RPO} = 0$ |
| **AI Provider Outage** | AI Circuit Breaker trips to `OPEN` and redirects to offline deterministic template engine. | $\text{RTO} < 0.1\text{ s}$<br>$\text{RPO} = 0$ |
| **Disk Exhaustion on Node** | `StagingManagerService` Sweeper purges orphaned folders; Kubelet evicts pod to clean node if needed. | $\text{RTO} < 10\text{ s}$<br>$\text{RPO} = 0$ |
