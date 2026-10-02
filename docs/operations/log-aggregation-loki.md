# Centralized Log Aggregation with Promtail & Grafana Loki

## 1. Overview
Codexa formats all runtime events as structured JSON logs, enabling zero-parse ingestion into **Grafana Loki** via Promtail.

---

## 2. Logback JSON Encoder Configuration

```xml
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"app":"codexa","env":"production"}</customFields>
    </encoder>
</appender>
```

---

## 3. LogQL Query Examples
* **Find Critical Security Violations**:
  `{app="codexa"} |= "CR-SEC" |= "CRITICAL"`
* **Monitor Ingestion Failures**:
  `{app="codexa"} |= "ZipBombDetected" or "RepositoryCloneException"`
