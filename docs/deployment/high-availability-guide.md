# Codexa High Availability & Multi-Node Clustering

## Topology
In multi-node deployments, multiple stateless Codexa backend instances share a centralized database and distributed lock manager.

```
                  [ Load Balancer (Round Robin) ]
                                │
               ┌────────────────┴────────────────┐
               ▼                                 ▼
      [ Codexa Node 1 ]                 [ Codexa Node 2 ]
               │                                 │
               └────────────────┬────────────────┘
                                ▼
        [ Managed PostgreSQL & Redis Event Bus ]
```

## Distributed Locking via ShedLock
Periodic maintenance tasks (such as archive cleanup and rule cache refresh) are coordinated using ShedLock over PostgreSQL:

```java
@Scheduled(cron = "0 0 * * * *")
@SchedulerLock(name = "pruneExpiredUploads", lockAtMostFor = "15m", lockAtLeastFor = "5m")
public void pruneExpiredUploads() { ... }
```
