# GitHub Actions CI/CD Integration

Example workflow snippet for triggering Codexa code audits on pull requests.


### Pull Request Quality Gate Example

```yaml
name: Codexa Audit Gate
on: [pull_request]
jobs:
  audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Codexa Scan
        run: curl -s https://codexa-ye85.onrender.com/api/v1/health
```
