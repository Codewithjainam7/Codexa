# Codexa Production Readiness & Scoring Engine Architecture

The Codexa Scoring Engine evaluates codebases across 5 rigorous mathematical axes, balancing security compliance, structural quality, operational readiness, maintainability, and architectural debt.

---

## 1. Mathematical Score Composition

The overall **Production Readiness Score (0–100)** is computed as a weighted composite of three primary pillars:

$$\text{Overall Score} = 0.60 \times \text{SecurityScore} + 0.25 \times \text{QualityScore} + 0.15 \times \text{OperationsScore}$$

When confirmed critical security vulnerabilities exist, code quality is barred from compensating for security failure:

$$\text{Overall Score}_{\text{Critical}} = 0.60 \times \text{SecurityScore} + 0.15 \times \text{OperationsScore}$$

### Component Deduction Matrix

1. **Security Score (Base 100)**
   - `CRITICAL` finding: **-30.0 points** (Enforces automatic `NOT_READY` verdict cap)
   - `HIGH` finding: **-15.0 points** (Triggers `hasHighAuthOrInjectionOrSecrets` review gate)
   - `MEDIUM` finding: **-8.0 points**
   - `LOW` finding: **-2.0 points**

$$\text{SecurityScore} = \max(0.0, 100.0 - \text{SecurityPenalty})$$

2. **Code Quality Score with Diminishing Debt Curves (Base 100)**
   - `HIGH` defect: **-12.0 points**
   - `MEDIUM` defect: **-3.0 points**
   - `LOW` defect: **-0.5 points**

To prevent minor style inconsistencies across massive monorepos from erroneously driving scores to zero, an asymptotic diminishing-returns curve is applied:

$$\text{EffectiveQualityPenalty} = \begin{cases} 
\text{QualityPenalty} & \text{if } \text{QualityPenalty} \le 30.0 \\ 
30.0 + (\text{QualityPenalty} - 30.0) \times 0.25 & \text{if } \text{QualityPenalty} > 30.0 
\end{cases}$$

$$\text{QualityScore} = \max(0.0, \min(100.0, 100.0 - \text{EffectiveQualityPenalty}))$$

3. **Operations & Hardening Score (Base 100)**
   - `HIGH` risk: **-12.0 points**
   - `MEDIUM` risk: **-4.0 points**
   - `LOW` risk: **-1.0 point**

$$\text{EffectiveOpsPenalty} = \begin{cases} 
\text{OperationsPenalty} & \text{if } \text{OperationsPenalty} \le 30.0 \\ 
30.0 + (\text{OperationsPenalty} - 30.0) \times 0.30 & \text{if } \text{OperationsPenalty} > 30.0 
\end{cases}$$

---

## 2. Maintainability Index Formula

The **Maintainability Index** (0–100) reflects refactoring velocity and defect probability:

$$\text{MaintainabilityPenalty} = (100.0 - \text{QualityScore}) \times 0.50 + (100.0 - \text{OperationsScore}) \times 0.30 + \min(25.0, |\text{Findings}| \times 0.20)$$

$$\text{MaintainabilityScore} = \max(0.0, \min(100.0, 100.0 - \text{MaintainabilityPenalty}))$$

---

## 3. Architectural Health & Debt Index

The **Architectural Score** (0–100) measures structural maintainability based on complexity, nesting, and duplication violations:

$$\text{StructuralDebt} = \sum_{f \in \text{ArchFindings}} 1.5$$

$$\text{ArchitecturalScore} = \max(0.0, \min(100.0, 100.0 - \min(40.0, \text{StructuralDebt}) - (100.0 - \text{QualityScore}) \times 0.25))$$

---

## 4. Production Verdict Decision Matrix

| Condition | Verdict | Readiness Impact |
| :--- | :--- | :--- |
| `hasConfirmedCritical == true` | `NOT_READY` | Critical security flaw detected (SQLi, RCE, Secrets). Release blocked. |
| `hasHighAuthSecrets == true && Overall < 50` | `NOT_READY` | Severe authorization bypass or credential leak. |
| `hasHighAuthSecrets == true && Overall >= 50` | `NEEDS_URGENT_FIXES` | High-priority security remediation required before deploy. |
| `Overall >= 90.0` | `REVIEW_COMPLETE` | Production ready. Code adheres to enterprise hardening standards. |
| `Overall >= 75.0` | `GENERALLY_PROMISING` | Staging ready. Minor quality or operational debt to address. |
| `Overall >= 50.0` | `NEEDS_URGENT_FIXES` | Significant defects or architectural debt requiring refactoring. |
| `Overall < 50.0` | `NOT_READY` | High risk across multiple dimensions. Not suitable for deployment. |
