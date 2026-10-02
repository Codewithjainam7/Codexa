# Production Readiness Scoring Formula Calibration

## 1. Overview
Codexa evaluates code readiness using a deterministic, explainable mathematical model combining four core quality dimensions:

$$\text{Readiness Index} = 0.60 \times S + 0.25 \times Q + 0.15 \times O$$

Where:
* $S$ = Security Score ($0 \dots 100$)
* $Q$ = Code Quality & Maintainability Score ($0 \dots 100$)
* $O$ = Operations & Deployment Hygiene Score ($0 \dots 100$)

---

## 2. Severity Penalty Deductions
* **CRITICAL**: -25 points per violation
* **HIGH**: -10 points per violation
* **MEDIUM**: -4 points per violation
* **LOW**: -1 point per violation

---

## 3. Production Readiness Verdicts
* **REVIEW_COMPLETE** (Score $\ge 75$): Production ready; zero critical blockers.
* **NEEDS_REVIEW** ($50 \le \text{Score} < 75$): Moderate technical debt or non-critical security flaws.
* **BLOCKED** (Score $< 50$ or any unresolved CRITICAL finding): Deployment gate fails.
