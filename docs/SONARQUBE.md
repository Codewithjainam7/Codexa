# SonarQube Generic Issue Import Guide

Import Codexa AST static analysis findings directly into SonarQube or SonarCloud.

## SonarQube Generic Issue Schema

Codexa findings map seamlessly to the SonarQube generic issue format:
- `engineId`: `codexa`
- `ruleId`: e.g. `CR-SEC-001`, `CR-QUAL-006`
- `severity`: `BLOCKER`, `CRITICAL`, `MAJOR`, `MINOR`, `INFO`
- `type`: `VULNERABILITY`, `BUG`, `CODE_SMELL`

## Integration in `sonar-project.properties`

```properties
sonar.projectKey=my-enterprise-app
sonar.projectName=Enterprise Application
sonar.sources=src/main
sonar.externalIssuesReportPaths=target/codexa-sonarqube-issues.json
```
