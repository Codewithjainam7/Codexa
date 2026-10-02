# Grafana Observability Dashboards & Alert Rules Template

## 1. Overview
Codexa provides pre-built Grafana dashboard JSON models for monitoring pipeline throughput, memory consumption, and vulnerability detection trends.

---

## 2. Alert Rules Definition

```yaml
groups:
  - name: codexa-alerts
    rules:
      - alert: CodexaHighJobErrorRate
        expr: rate(codexa_jobs_failed_total[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Analysis job failure rate exceeds 5% over 5 minutes"

      - alert: CodexaQueueBacklog
        expr: codexa_jobs_queued > 25
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Analysis queue backlog exceeds 25 jobs"
```
