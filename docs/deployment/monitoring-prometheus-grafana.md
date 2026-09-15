# Codexa Prometheus & Grafana Monitoring Guide

## Micrometer Actuator Endpoints
Codexa exposes production metrics at `/actuator/prometheus`:

- `jvm_memory_used_bytes{area="heap"}`: Heap consumption.
- `codexa_analysis_duration_seconds`: Histogram of code review latency.
- `codexa_ast_nodes_total`: Count of parsed syntax nodes.
- `codexa_findings_detected_total{severity="..."}`: Violation counters by severity.

## Prometheus Scrape Config

```yaml
scrape_configs:
  - job_name: 'codexa'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['codexa-service:8080']
```

## Key Grafana Alerting Thresholds
- **P99 Analysis Duration > 60s**: Signals potential worker thread starvation or huge unoptimized zip upload.
- **Heap Usage > 85% for 5m**: Triggers alert for heap sizing adjustment.
- **Circuit Breaker State == OPEN**: Alerts SecOps that AI provider fallback is active.
