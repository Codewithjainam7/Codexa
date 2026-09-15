# Codexa GDPR & Data Privacy Compliance Guide

## Principles Applied
Codexa is engineered with Privacy by Design (Art. 25 GDPR):

## Core Directives
1. **Data Minimization (Art. 5(1)(c))**: Codexa extracts only Abstract Syntax Trees and syntax tokens. Developer PII (author emails, commit signatures) are stripped unless explicitly retained for blame reporting.
2. **Right to Erasure / Forgotten (Art. 17)**: Deleting a repository from Codexa triggers an atomic cascade:
   - All historical analysis findings are deleted from SQLite/Postgres.
   - Cached AST trees in Caffeine memory are cleared.
   - Temp upload directories are securely zero-filled.
3. **Data Residency**: Self-hosted deployments keep 100% of code, findings, and metadata within customer-controlled network boundaries.
