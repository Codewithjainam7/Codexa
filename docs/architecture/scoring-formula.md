# Codexa Scoring Formula

- Security Score: Base 100 with deduction weights per severity (Critical: -25, High: -15, Medium: -8, Low: -3).
- Quality Score: Base 100 with code smell and complexity deductions.
- Operations Score: Base 100 evaluated against cloud config, Docker, and logging practices.
