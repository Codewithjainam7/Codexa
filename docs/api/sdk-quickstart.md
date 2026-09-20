# Codexa Multi-Language SDK & Automation Quickstart

This guide provides drop-in automation recipes for triggering and polling Codexa code audits using standard command-line tools, Python, and Node.js.

---

## 1. cURL & Shell Automation Script

```bash
#!/usr/bin/env bash
set -euo pipefail

CODEXA_URL="${CODEXA_URL:-http://localhost:8080}"
REPO_URL="https://github.com/Codewithjainam7/Codexa"

echo "Submitting repository to Codexa: ${REPO_URL}..."
JOB_ID=$(curl -s -X POST "${CODEXA_URL}/api/v1/analyses/github" \
  -H "Content-Type: application/json" \
  -d "{\"repoUrl\":\"${REPO_URL}\"}" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

echo "Analysis Job ID: ${JOB_ID}"
echo "Polling for completion..."

while true; do
  STATUS_JSON=$(curl -s "${CODEXA_URL}/api/v1/analyses/${JOB_ID}")
  STATUS=$(echo "${STATUS_JSON}" | grep -o '"status":"[^"]*' | cut -d'"' -f4)
  PERCENT=$(echo "${STATUS_JSON}" | grep -o '"progressPercent":[0-9]*' | cut -d':' -f2)

  echo "Status: ${STATUS} (${PERCENT}%)..."
  if [ "${STATUS}" = "COMPLETED" ]; then
    SCORE=$(echo "${STATUS_JSON}" | grep -o '"overallScore":[0-9.]*' | cut -d':' -f2)
    VERDICT=$(echo "${STATUS_JSON}" | grep -o '"verdict":"[^"]*' | cut -d'"' -f4)
    echo "========================================="
    echo "Audit Complete!"
    echo "Overall Score: ${SCORE} / 100"
    echo "Verdict: ${VERDICT}"
    echo "========================================="
    break
  elif [ "${STATUS}" = "FAILED" ]; then
    echo "Analysis failed."
    exit 1
  fi
  sleep 3
done
```

---

## 2. Python Automation (Zero Dependencies)

```python
import json
import time
import urllib.request

CODEXA_URL = "http://localhost:8080"
REPO_URL = "https://github.com/Codewithjainam7/Codexa"

# 1. Trigger Analysis
req = urllib.request.Request(
    f"{CODEXA_URL}/api/v1/analyses/github",
    data=json.dumps({"repoUrl": REPO_URL}).encode("utf-8"),
    headers={"Content-Type": "application/json"}
)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode())
    job_id = data["id"]
    print(f"Started analysis job: {job_id}")

# 2. Poll until complete
while True:
    with urllib.request.urlopen(f"{CODEXA_URL}/api/v1/analyses/{job_id}") as resp:
        job = json.loads(resp.read().decode())
        status = job.get("status")
        percent = job.get("progressPercent", 0)
        print(f"Progress: {status} ({percent}%)...")
        
        if status == "COMPLETED":
            print(f"\nFinal Readiness Score: {job.get('overallScore')}/100")
            print(f"Production Verdict: {job.get('verdict')}")
            print(f"Critical Findings: {job['metrics'].get('criticalCount')}")
            break
        elif status == "FAILED":
            raise RuntimeError(f"Audit failed: {job.get('errorMessage')}")
            
    time.sleep(3)
```

---

## 3. Node.js Automation (ESM / Native Fetch)

```javascript
const CODEXA_URL = process.env.CODEXA_URL || 'http://localhost:8080';
const repoUrl = 'https://github.com/Codewithjainam7/Codexa';

async function auditRepository() {
  const submitRes = await fetch(`${CODEXA_URL}/api/v1/analyses/github`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ repoUrl })
  });
  const { id: jobId } = await submitRes.json();
  console.log(`Submitted audit job: ${jobId}`);

  while (true) {
    const res = await fetch(`${CODEXA_URL}/api/v1/analyses/${jobId}`);
    const job = await res.json();
    console.log(`Status: ${job.status} (${job.progressPercent}%)...`);

    if (job.status === 'COMPLETED') {
      console.log(`Score: ${job.overallScore}/100 | Verdict: ${job.verdict}`);
      return job;
    }
    if (job.status === 'FAILED') {
      throw new Error(`Audit failed: ${job.errorMessage}`);
    }
    await new Promise(r => setTimeout(r, 3000));
  }
}

auditRepository().catch(console.error);
```
