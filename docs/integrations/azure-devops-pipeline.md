# Codexa Azure DevOps Pipelines Integration Guide

This guide details how to integrate Codexa static analysis and production readiness gates into Microsoft Azure DevOps CI/CD pipelines (`azure-pipelines.yml`).

---

## 1. Pipeline Integration Overview

By integrating Codexa into Azure Pipelines:
1. Every commit and Pull Request triggers an automated static audit via Codexa API or container.
2. The readiness score is evaluated: if `verdict == NOT_READY`, the build pipeline breaks automatically.
3. The SARIF report is uploaded and displayed natively in Azure DevOps under the **Scans** and **Artifacts** tabs.

---

## 2. Complete `azure-pipelines.yml` Example

```yaml
trigger:
  branches:
    include:
      - main
      - develop

pr:
  branches:
    include:
      - main

pool:
  vmImage: 'ubuntu-latest'

steps:
- task: UseDotNet@2
  displayName: 'Prepare Environment'

- script: |
    echo "Submitting repository to Codexa Security Platform..."
    RESPONSE=$(curl -s -X POST https://codexa-ye85.onrender.com/api/v1/analyses/github \
      -H "Content-Type: application/json" \
      -d "{\"repoUrl\": \"$(Build.Repository.Uri)\"}")
    
    JOB_ID=$(echo $RESPONSE | jq -r '.id')
    echo "Codexa Analysis Job Enqueued: $JOB_ID"
    
    # Poll for completion
    STATUS="EXTRACTING"
    while [ "$STATUS" != "COMPLETED" ] && [ "$STATUS" != "FAILED" ]; do
      sleep 3
      STATUS=$(curl -s https://codexa-ye85.onrender.com/api/v1/analyses/$JOB_ID | jq -r '.status')
      echo "Current Status: $STATUS"
    done
    
    # Evaluate Verdict
    VERDICT=$(curl -s https://codexa-ye85.onrender.com/api/v1/analyses/$JOB_ID | jq -r '.verdict')
    SCORE=$(curl -s https://codexa-ye85.onrender.com/api/v1/analyses/$JOB_ID | jq -r '.overallScore')
    echo "Codexa Readiness Score: $SCORE/100, Verdict: $VERDICT"
    
    # Export SARIF
    curl -s https://codexa-ye85.onrender.com/api/v1/analyses/$JOB_ID/sarif > $(Build.ArtifactStagingDirectory)/codexa.sarif
    
    if [ "$VERDICT" == "NOT_READY" ]; then
      echo "##vso[task.logissue type=error]Codexa Production Readiness Gate Failed! Critical security vulnerabilities detected."
      exit 1
    fi
  displayName: 'Execute Codexa Static Audit'

- task: PublishBuildArtifacts@1
  inputs:
    PathtoPublish: '$(Build.ArtifactStagingDirectory)/codexa.sarif'
    ArtifactName: 'CodeAnalysisLogs'
  displayName: 'Publish Codexa SARIF Artifact'
```
