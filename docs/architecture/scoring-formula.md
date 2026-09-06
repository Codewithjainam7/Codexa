# Codexa Production Readiness & Scoring Engine Architecture

The Codexa Scoring Engine evaluates codebases across 5 rigorous mathematical axes, balancing security compliance, structural quality, operational readiness, maintainability, and architectural debt.

---

## 1. Mathematical Score Composition

The overall **Production Readiness Score (0–100)** is computed as a weighted harmonic composite of three primary pillars:

$$\text{Overall Score} = 0.60 \times \text{SecurityScore} + 0.25 \times \text{QualityScore} + 0.15 \times \text{OperationsScore}$$

### Component Deduction Matrix

1. **Security Score (Base 100)**
   - `CRITICAL` finding: **-30.0 points** (Enforces automatic `NOT_READY` verdict cap)
   - `HIGH` finding: **-15.0 points** (Triggers `hasHighAuthSecrets` review rule)
   - `MEDIUM` finding: **-8.0 points**
   - `LOW` finding: **-2.0 points**

2. **Code Quality Score (Base 100)**
   - `HIGH` defect (Severe code smell / cyclomatic complexity): **-20.0 points**
   - `MEDIUM` defect (Deep nesting / long methods / dead code): **-10.0 points**
   - `LOW` defect (Style inconsistencies): **-3.0 points**

3. **Operations & Hardening Score (Base 100)**
   - `HIGH` risk (Missing timeouts / insecure defaults / unhandled exceptions): **-15.0 points**
   - `MEDIUM` risk (Suboptimal logging / missing health checks): **-8.0 points**
   - `LOW` risk (Minor config deviations): **-2.0 points**

---

## 2. Maintainability Index Formula

The **Maintainability Index** (0–100) measures long-term engineering velocity and maintenance overhead:

$$\text{MaintainabilityPenalty} = 0.70 \times \text{QualityPenalty} + 0.40 \times \text{OperationsPenalty} + 1.20 \times \text{TotalFindingCount}$$

$$\text{MaintainabilityScore} = \max(0, \min(100, 100 - \text{MaintainabilityPenalty}))$$

---

## 3. Architectural Health & Debt Index

The **Architectural Score** (0–100) isolates systemic architectural violations, including:
- Layer bleeding (`CR-ARCH-001`)
- Cyclomatic complexity bottlenecks (`CR-COMPLEX-001`)
- Deep control flow nesting (`CR-NEST-001`)
- Code duplication clusters (`CR-DUP-001`)

$$\text{StructuralDebt} = \sum_{\text{arch rules}} 12.0$$

$$\text{ArchitecturalScore} = \max(0, \min(100, 100 - \text{StructuralDebt} - 0.40 \times \text{QualityPenalty}))$$

---

## 4. Production Verdict Decision Table

| Condition | Verdict | Meaning |
| :--- | :--- | :--- |
| Any confirmed `CRITICAL` finding | `NOT_READY` | Blocked from production deployment. Immediate remediation required. |
| High severity auth, injection, or secrets finding | `NEEDS_URGENT_FIXES` or `NOT_READY` | Critical surface exposure. Must patch prior to release. |
| Overall Score $\ge 90.0$ | `REVIEW_COMPLETE` | Production-ready with low residual risk. |
| Overall Score $75.0 - 89.9$ | `GENERALLY_PROMISING` | Suitable for staging/UAT with planned minor fixes. |
| Overall Score $50.0 - 74.9$ | `NEEDS_URGENT_FIXES` | Substantial risk requiring code review attention. |
| Overall Score $< 50.0$ | `NOT_READY` | High risk fail state. |
