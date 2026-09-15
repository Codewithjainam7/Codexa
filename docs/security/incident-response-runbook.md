# Codexa Security Incident Response & Triage Runbook

## Severity Classification & SLA

| Severity | Definition | Response SLA | Mitigation SLA |
| :--- | :--- | :--- | :--- |
| **P1 - Critical** | Remote code execution, database compromise, auth bypass | 15 minutes | 4 hours |
| **P2 - High** | SSRF, privilege escalation, partial secret exposure | 1 hour | 24 hours |
| **P3 - Medium** | Denial of service on non-critical endpoints, minor CSRF | 4 hours | 7 days |
| **P4 - Low** | Informational disclosures, stylistic security warnings | 24 hours | 30 days |

## Triage Checklist
1. **Isolate**: Revoke compromised API tokens via `/api/v1/auth/tokens/revoke-all`.
2. **Snapshot**: Capture memory dump and forensic audit logs before terminating running containers.
3. **Notify**: Send incident dispatch notification to SecOps Slack/PagerDuty channel.
4. **Patch & Verify**: Apply hotfix branch, execute full regression test suite, and deploy canary.
5. **Post-Mortem**: Publish transparent Root Cause Analysis (RCA) within 72 hours.
