# Codexa Structured Audit Logging & SIEM Compliance Standards

This specification defines the security audit logging taxonomy, Elastic Common Schema (ECS) mapping, anti-tamper mechanisms, and SIEM ingestion protocols enforced by **Codexa**.

---

## 1. Compliance Alignment & Regulatory Mandates

Codexa's audit logging framework fulfills compliance requirements across enterprise governance standards:
- **SOC 2 Type II (Trust Services Criteria CC6.8 & CC7.2)**: Logging authorized and unauthorized configuration modifications, security alerts, and administrative actions.
- **PCI-DSS v4.0 Requirement 10**: Log and monitor all access to system components and cardholder data environments.
- **ISO/IEC 27001:2022 Control 8.15**: Logging of events, generation of evidence, and periodic review of activity logs.
- **GDPR Article 30**: Records of processing activities and data access tracing.

---

## 2. Event Taxonomy & Classification Matrix

Every security-relevant operation within Codexa produces an immutable audit record:

| Event Action | Event Category | Severity | Trigger Description |
|:---|:---|:---:|:---|
| `auth.token.authenticated` | `iam` | `INFO` | Client authenticated successfully via PAT or JWT. |
| `auth.token.rejected` | `iam` | `WARN` | Authentication failed due to expired, invalid, or revoked token. |
| `auth.token.revoked` | `iam` | `WARN` | Security administrator revoked an active API token. |
| `analysis.job.submitted` | `audit` | `INFO` | User or CI/CD runner submitted code for static scanning. |
| `analysis.job.completed` | `audit` | `INFO` | Scan completed with score, verdict, and duration metrics. |
| `security.ssrf.blocked` | `threat` | `HIGH` | Outbound HTTP request aborted due to private IP or metadata probe. |
| `security.zip_slip.blocked` | `threat` | `CRITICAL` | Directory traversal attempt detected during archive decompression. |
| `security.zip_bomb.blocked` | `threat` | `HIGH` | High-ratio archive decompression aborted to prevent DoS. |
| `admin.rule.toggled` | `configuration`| `WARN` | Administrator enabled or disabled an analysis rule in the catalog. |
| `admin.storage.pruned` | `maintenance` | `INFO` | Scheduled or manual database retention sweep purged historical records. |

---

## 3. Elastic Common Schema (ECS) JSON Log Format

All audit events are formatted as single-line structured JSON adhering to the Elastic Common Schema (ECS v8.x):

```json
{
  "@timestamp": "2026-09-28T15:10:45.812Z",
  "ecs.version": "8.11.0",
  "log.level": "WARN",
  "event.category": ["threat", "network"],
  "event.action": "security.ssrf.blocked",
  "event.outcome": "failure",
  "event.reason": "TARGET_RESOLVED_TO_AWS_METADATA_IP",
  "user.id": "cdx_usr_9912",
  "user.roles": ["developer", "ci-runner"],
  "client.ip": "198.51.100.42",
  "client.user_agent": "Codexa-CLI/1.3.0",
  "http.request.method": "POST",
  "url.path": "/api/v1/analyses/github",
  "codexa.job_id": "ddaf0239-eee4-4032-80a0-60c9e4fe7078",
  "codexa.attempted_target": "http://169.254.169.254/latest/meta-data/",
  "service.name": "codexa-backend",
  "service.version": "1.3.0",
  "service.environment": "production",
  "host.hostname": "codexa-backend-7d9b4c6-v9x12"
}
```

---

## 4. In-Flight Secret & PII Scrubbing (CWE-532 Mitigation)

Before any audit record is written to disk or forwarded over the network, it passes through `SensitiveLoggingRule` sanitizers to prevent credential leakage into log management systems:

1. **Token Masking**: All bearer tokens and API keys are masked to prefix and suffix characters:
   ```
   cdx_pat_7e9a2b5f...3b4c5d6e (Middle 48 hex characters replaced)
   ```
2. **Password & Credential Sanitization**: Key-value pairs containing `password`, `secret`, `access_key`, `credential` have values replaced with `[REDACTED]`.
3. **Shannon Entropy Filter**: Any unstructured message string containing tokens with Shannon entropy $H > 4.5$ is automatically sanitized before output.

---

## 5. Mapped Diagnostic Context (MDC) Tracing

To correlate log events across multi-stage pipeline workers, asynchronous ForkJoinPool tasks, and REST controllers, Codexa injects distributed tracing keys into the SLF4J `MDC`:

```java
try (MDC.MDCCloseable c1 = MDC.putCloseable("jobId", jobId.toString());
     MDC.MDCCloseable c2 = MDC.putCloseable("clientIp", request.getRemoteAddr());
     MDC.MDCCloseable c3 = MDC.putCloseable("userId", principal.getId())) {
    
    log.info("Analysis pipeline started for repository");
    orchestrator.execute(context);
}
```

Every child thread inherits the MDC context via thread-local propagation.

---

## 6. Anti-Tamper & SIEM Ingestion Architecture

```
[ Codexa Pods (Structured JSON to stdout) ]
                    │
                    ▼
[ Kubernetes Container Runtime Engine (cri-o / containerd) ]
                    │
                    ▼
[ Fluentbit / Vector DaemonSet Node Log Collector ]
                    │
         ┌──────────┴──────────┐
         ▼                     ▼
[ Immutable WORM S3 Bucket ]   [ SIEM Analytics Platform ]
(AWS S3 Object Lock, 365 days) (Splunk / Datadog / Elasticsearch)
```

- **Write-Once-Read-Many (WORM)**: In cloud environments, audit logs are streamed to Amazon S3 buckets configured with **S3 Object Lock in Compliance Mode** (retention period: 365 days). Once written, logs cannot be modified, deleted, or overwritten by any user, including root AWS accounts.
- **Log Rotation Policy**: On-disk rolling files are rotated daily with maximum file size 100 MB and total storage cap 10 GB.
