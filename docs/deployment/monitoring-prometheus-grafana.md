# Codexa Observability: Prometheus, Grafana & Micrometer Metrics Reference

This guide details the complete observability architecture, Micrometer metric taxonomy, Prometheus scrape configurations, Grafana dashboard panels, and alerting thresholds for monitoring **Codexa** in production.

---

## 1. Observability Architecture Overview

Codexa integrates Spring Boot Actuator with Micrometer to export multidimensional time-series metrics formatted for Prometheus scrapers:

```
[ Codexa Backend Pods (Java 21 / Spring Boot 3.3) ]
  ├── /actuator/prometheus (Port 8080)
  ├── Micrometer Core Metrics (JVM, GC, HikariCP, HTTP)
  └── Codexa Domain Metrics (Pipeline Stages, Rules, AI Fallback)
                     │
                     ▼ (Scrape Interval: 15s)
          [ Prometheus Server / VictoriaMetrics ]
                     │
                     ├──────────────────────────────────┐
                     ▼                                  ▼
          [ Grafana Dashboard ]                [ Alertmanager ]
    (Executive, Runtime, Security)         (Slack / PagerDuty / Opsgenie)
```

---

## 2. Micrometer Metrics Taxonomy

### A. Codexa Core Domain Metrics
| Metric Name | Type | Tags / Dimensions | Description |
|:---|:---:|:---|:---|
| `codexa.analysis.jobs.total` | Counter | `status={COMPLETED, FAILED}`, `type={ZIP, GITHUB}` | Total analysis jobs submitted. |
| `codexa.analysis.duration.seconds` | Timer | `type={ZIP, GITHUB}` | Latency distribution of end-to-end analysis jobs. |
| `codexa.stage.duration.seconds` | Timer | `stage={AST_PARSING, RULES, AI, SCORING}` | Execution duration breakdown per pipeline stage. |
| `codexa.findings.detected.total` | Counter | `category`, `severity`, `ruleId` | Total security and quality violations discovered. |
| `codexa.staging.bytes.extracted` | DistributionSummary | `type={zip}` | Uncompressed file sizes staged to disk. |
| `codexa.ai.remediation.calls` | Counter | `provider={openrouter, offline_deterministic}`, `status` | AI inference vs deterministic offline fallback executions. |
| `codexa.ai.circuitbreaker.state` | Gauge | `state={0:CLOSED, 1:HALF_OPEN, 2:OPEN}` | Real-time state of the AI model cascade circuit breaker. |
| `codexa.ai.cache.hits.total` | Counter | `level={l1_memory, l2_sqlite}` | Cache hit count on content-addressable AST queries. |

### B. JVM, System & Database Pool Metrics
| Metric Name | Type | Description |
|:---|:---:|:---|
| `jvm.memory.used` | Gauge | Heap and non-heap memory consumption (`area={heap, nonheap}`). |
| `jvm.gc.pause` | Timer | Garbage collection pause duration and frequency. |
| `jvm.threads.live` | Gauge | Active virtual and platform threads. |
| `process.cpu.usage` | Gauge | Percentage CPU utilization of the Codexa JVM process ($0.0 - 1.0$). |
| `hikaricp.connections.active` | Gauge | Active database connections currently acquired by worker threads. |
| `hikaricp.connections.pending` | Gauge | Threads awaiting an available database connection. |
| `http.server.requests` | Timer | Latency and throughput of REST API endpoints by status code. |

---

## 3. Prometheus Scrape Configuration & Kubernetes ServiceMonitor

### Standard Prometheus Configuration (`prometheus.yml`)
```yaml
scrape_configs:
  - job_name: 'codexa-production'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    scrape_timeout: 10s
    scheme: http
    static_configs:
      - targets: ['codexa-backend.codexa.svc.cluster.local:8080']
        labels:
          environment: 'production'
          service: 'codexa-core'
```

### Kubernetes Operator `ServiceMonitor` (Prometheus Operator)
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: codexa-backend-monitor
  namespace: codexa
  labels:
    release: prometheus-stack
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: codexa-backend
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 15s
      scrapeTimeout: 10s
```

---

## 4. Production Alerting Rules (`alertmanager.rules.yaml`)

```yaml
groups:
  - name: codexa-critical-alerts
    rules:
      # Alert: High Analysis Pipeline Failure Rate
      - alert: CodexaHighJobFailureRate
        expr: (sum(rate(codexa_analysis_jobs_total{status="FAILED"}[5m])) / sum(rate(codexa_analysis_jobs_total[5m]))) * 100 > 5
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Codexa job failure rate exceeded 5%"
          description: "Over 5% of analysis jobs failed in the last 5 minutes. Check application logs for extraction errors or parser timeouts."

      # Alert: Elevated P99 Analysis Latency
      - alert: CodexaElevatedP99Latency
        expr: histogram_quantile(0.99, sum(rate(codexa_analysis_duration_seconds_bucket[10m])) by (le)) > 45
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Codexa P99 analysis duration > 45s"
          description: "Static scanning jobs are taking longer than 45 seconds to complete. Check worker concurrency and I/O wait."

      # Alert: JVM Memory Saturation
      - alert: CodexaJvmHeapSaturation
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 85
        for: 5m
        labels:
          severity: high
        annotations:
          summary: "Codexa JVM Heap utilization > 85%"
          description: "JVM Heap usage is continuously exceeding 85% for 5 minutes. Memory leak or large repo heap pressure detected."

      # Alert: AI Circuit Breaker Tripped
      - alert: CodexaAiCircuitBreakerOpen
        expr: codexa_ai_circuitbreaker_state == 2
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Codexa AI Remediation Gateway tripped to OPEN"
          description: "OpenRouter inference API is failing or rate-limited. Engine has automatically degraded to deterministic offline templates."
```

---

## 5. Grafana Dashboard Layout & Visualizations

The official Codexa production Grafana dashboard (`Codexa-Enterprise-Overview.json`) organizes metrics into four specialized panels:

### Row 1: Executive & Operational Health
- **Active Scans Gauge**: Real-time counter of concurrently running analysis pipelines.
- **24h Scans Completed Stat**: Total successful scans across the rolling 24-hour window.
- **Average Security Score Gauge**: Color-coded metric (0-100) reflecting overall codebase security posture.
- **Critical Vulnerability Spike Stat**: Count of detected `CRITICAL` findings in the last 1 hour.

### Row 2: Pipeline Throughput & Latency Distribution
- **Analysis Latency Heatmap**: Visualizes scan duration buckets ($< 1\text{ s}$, $1-5\text{ s}$, $5-15\text{ s}$, $> 30\text{ s}$).
- **Stage Breakdown Stacked Graph**: Time spent in AST Parsing vs Static Rules vs AI Remediation vs Scoring.

### Row 3: JVM Runtime & Resource Saturation
- **Heap Memory Pools Time-Series**: G1 Eden, Survivor, and Old Generation memory profiles.
- **HikariCP Connection Pool Saturation**: Active vs Idle vs Pending database connection threads.
- **Garbage Collection Pause Times**: Duration (ms) and frequency of G1/ZGC young and full collection pauses.

### Row 4: AI Gateway & Cache Efficiency
- **Cache Hit Ratio Gauge**: $\frac{\text{Cache Hits}}{\text{Total AI Requests}} \times 100\%$, tracking AST content-addressable cache efficiency.
- **Provider Cascade Breakdown**: Donut chart displaying proportion of Claude 3.5 Sonnet vs GPT-4o vs DeepSeek vs Offline Fallback fixes.
