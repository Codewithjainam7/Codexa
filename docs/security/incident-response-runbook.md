# Codexa Security Incident Response & Triage Runbook

This runbook defines the operational protocol, severity triage matrix, containment procedures, and forensic response checklists for security incidents impacting the **Codexa** platform, its ingestion pipeline, or associated container infrastructure.

---

## 1. Incident Severity Classification & Escalation Matrix

Codexa categorizes incidents based on Common Vulnerability Scoring System (CVSS v3.1) metrics and operational impact:

| Severity Level | CVSS v3.1 | Description & Impact | Acknowledgment SLA | Remediation SLA | Incident Commander |
|:---|:---:|:---|:---:|:---:|:---|
| **P1 - Critical** | $9.0 - 10.0$ | Arbitrary Remote Code Execution (RCE), unauthenticated database breach, root container escape, authentication bypass. | **15 minutes** | **4 hours** | Head of Engineering / Lead SecOps |
| **P2 - High** | $7.0 - 8.9$ | SSRF attempting cloud metadata egress, Zip Slip path traversal attempt, authenticated privilege escalation, exposed API master keys. | **30 minutes** | **12 hours** | Senior Security Engineer |
| **P3 - Medium** | $4.0 - 6.9$ | Rate-limiting bypass, localized Denial of Service (DoS) in memory parser, CSRF on non-critical endpoints, outdated vulnerable sub-dependency without active exploit. | **2 hours** | **72 hours** | On-Call Backend Engineer |
| **P4 - Low** | $0.1 - 3.9$ | Minor informational header leak, verbose error trace on 500 response, stylistic or documentation security flaw. | **8 hours** | **14 days** | Engineering Team Member |

---

## 2. Five-Phase Incident Response Lifecycle

```
+-----------------------------------------------------------------------------------+
|                            Phase 1: Detection & Triage                            |
|   - Prometheus / Grafana alert trigger (e.g. error spike, SSRF rejection rate)   |
|   - Customer or bug bounty security report via security@codexa.dev                |
|   - Classify severity (P1-P4) and assign Incident Commander                       |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                           Phase 2: Immediate Containment                          |
|   - Isolate affected container / pod via network policy                           |
|   - Revoke compromised API keys and invalidate active JWT sessions                |
|   - Capture memory dump (Heap dump) & rolling logs before container restart       |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                        Phase 3: Eradication & Root Cause                          |
|   - Identify vulnerability mechanism (e.g., bypass in path normalization)          |
|   - Create failing regression unit test reproducing exact exploit payload         |
|   - Implement deterministic source fix in git hotfix branch                       |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                           Phase 4: Recovery & Canary Deploy                       |
|   - Execute full automated test suite (135+ tests passing)                        |
|   - Deploy patch to canary / staging environment; verify fix                      |
|   - Blue/Green zero-downtime rolling update to production cluster                 |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                        Phase 5: Post-Mortem & Retrospective                       |
|   - Conduct blameless root cause analysis (RCA) within 72 hours                   |
|   - Publish advisory / CVE disclosure (if applicable)                             |
|   - Automate regression prevention rule into Codexa static engine                 |
+-----------------------------------------------------------------------------------+
```

---

## 3. Incident Scenarios & Standard Operating Procedures (SOP)

### SOP-01: Malicious Archive Exploitation Attempt (Zip Slip / Zip Bomb)
1. **Detection**: `SecureZipExtractor` logs `Zip Slip directory traversal detected in entry: ../../` or `Zip bomb detected: exceeded file count limit`.
2. **Containment**:
   - The extraction transaction is aborted automatically by `ApiException`.
   - Ensure the staged directory was purged by `StagingManagerService.cleanDirectory()`.
3. **Forensic Inspection**:
   ```bash
   # Check worker logs for client IP and job metadata
   grep -E "ZIP_SLIP_DETECTED|ZIP_BOMB_DETECTED" /var/log/codexa/application.log
   ```
4. **Action**: If repeated from single source IP, add IP to edge firewall / cloud WAF blocklist.

---

### SOP-02: SSRF Remote Target Probe Attempt
1. **Detection**: `SsrfProtectionService` throws `SSRF_ATTEMPT_DETECTED` on remote Git download endpoint `/api/v1/analyses/github`.
2. **Verification**:
   - Inspect resolved target IP in audit log:
     ```json
     {"event":"SSRF_BLOCKED","jobId":"...","attemptedHost":"169.254.169.254","blockedReason":"LINK_LOCAL_METADATA"}
     ```
3. **Action**: Confirm outbound connection was terminated before TCP handshake. Verify cloud instance IAM roles are tightly scoped (IMDSv2 hop limit = 1).

---

### SOP-03: Compromised API Token Emergency Revocation
If an API secret or administrative bearer token is inadvertently committed to a public repository:
1. **Emergency Revocation**:
   ```bash
   # Revoke specific token immediately
   curl -X POST https://codexa.internal/api/v1/auth/tokens/revoke \
        -H "Authorization: Bearer <ADMIN_MASTER_KEY>" \
        -d '{"tokenId": "tok_live_7x89q..."}'
   ```
2. **Rotate Master Encryption Key**: If database credentials or master token secrets are implicated:
   - Update `CODEXA_MASTER_ENCRYPTION_KEY` in Kubernetes Secret.
   - Trigger rolling deployment: `kubectl rollout restart deployment/codexa-backend`.

---

## 4. Forensic Evidence Collection & Heap Dumps

Before terminating or restarting a compromised or anomalous container pod, capture runtime diagnostic evidence:

```bash
# 1. Capture JVM Heap Dump
jcmd <PID> GC.heap_dump /tmp/codexa_incident_<DATE>.hprof

# 2. Capture Thread Dump
jcmd <PID> Thread.print > /tmp/codexa_threads_<DATE>.txt

# 3. Export Container Journal / Logs
journalctl -u codexa-backend --since "2 hours ago" > /tmp/incident_logs.txt

# 4. Copy artifacts to secure offline S3/GCS bucket
aws s3 cp /tmp/codexa_incident_*.hprof s3://secops-forensics-vault/incidents/
```

---

## 5. Post-Mortem & Blameless RCA Template

Within 72 hours of resolving any P1 or P2 incident, the Incident Commander must author an Incident Post-Mortem containing:
1. **Executive Summary**: Brief narrative describing what happened, business impact, and resolution.
2. **Detailed Timeline (UTC)**: Chronological event trace from vulnerability introduction, detection, alert, triage, containment, to fix deployment.
3. **Root Cause Analysis (5 Whys)**: Deep technical exploration identifying underlying causes rather than individual actions.
4. **Corrective & Preventive Action Items (CAPA)**: JIRA tracking tickets with designated assignees and due dates for architectural improvements, additional static rules, and integration tests.
