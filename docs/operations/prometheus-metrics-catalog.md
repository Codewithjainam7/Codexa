# Prometheus Metrics Catalog & Micrometer Instrumentation

## 1. Overview
Codexa exposes real-time runtime, JVM, and static analysis metrics via Micrometer and the Spring Boot Actuator endpoint at `/actuator/prometheus`.

---

## 2. Core Metrics Catalog

| Metric Name | Type | Description |
| :--- | :--- | :--- |
| `codexa.analysis.duration.seconds` | Summary | Latency distribution of complete analysis jobs |
| `codexa.findings.total` | Counter | Total findings flagged, tagged by `category` and `severity` |
| `codexa.jobs.active` | Gauge | Number of currently executing analysis pipelines |
| `jvm.threads.virtual.count` | Gauge | Active Java 21 virtual threads |
| `sqlite.pool.connections.active` | Gauge | Active SQLite database connections |
