# Codexa Multi-Language SDK Quickstart

## Python SDK Quickstart

```python
import codexa

client = codexa.Client(
    base_url="https://codexa.example.com",
    api_key="cdx_pat_xxxxxxxxxxxxxxxxxxxx"
)

# Submit archive for review
job = client.analysis.submit_zip(
    file_path="./backend-service.zip",
    rule_profile="STRICT_SECURITY"
)

print(f"Analysis started: {job.id}")
result = job.wait_for_completion(timeout_seconds=120)
print(f"Quality Score: {result.quality_score}/100")

for finding in result.findings:
    print(f"[{finding.severity}] {finding.file}:{finding.line} - {finding.message}")
```

## Node.js / TypeScript SDK Quickstart

```typescript
import { CodexaClient } from '@codexa/sdk';

const codexa = new CodexaClient({
  baseUrl: 'https://codexa.example.com',
  apiKey: process.env.CODEXA_API_KEY!,
});

async function runReview() {
  const job = await codexa.analysis.createFromGit({
    repositoryUrl: 'https://github.com/org/repo.git',
    branch: 'main',
  });

  const stream = await codexa.analysis.streamEvents(job.id);
  stream.on('finding_detected', (finding) => {
    console.warn(`[${finding.severity}] Violation: ${finding.message}`);
  });
}
```
