# Codexa GraphQL Schema Draft Specification

## Motivation
To support complex developer dashboards requiring consolidated data (e.g. repository metadata, recent scans, rule violation breakdowns, and AST performance metrics in a single network round-trip), Codexa drafts an optional GraphQL schema.

## Schema Definition (SDL)

```graphql
enum Severity {
  CRITICAL
  HIGH
  MEDIUM
  LOW
  INFORMATIONAL
}

enum JobStatus {
  QUEUED
  PARSING
  ANALYZING
  AI_SYNTHESIZING
  COMPLETED
  FAILED
}

type Finding {
  id: ID!
  ruleId: String!
  severity: Severity!
  message: String!
  filePath: String!
  startLine: Int!
  endLine: Int!
  remediationSnippet: String
}

type AnalysisJob {
  id: ID!
  repositoryName: String!
  commitSha: String
  status: JobStatus!
  qualityScore: Int!
  findings(severity: Severity, limit: Int = 50): [Finding!]!
  durationMs: Int!
  createdAt: String!
}

type Query {
  job(id: ID!): AnalysisJob
  recentJobs(limit: Int = 10): [AnalysisJob!]!
  activeRules(category: String): [RuleDefinition!]!
}

type RuleDefinition {
  ruleId: String!
  name: String!
  category: String!
  defaultSeverity: Severity!
  cweId: String
}
```
