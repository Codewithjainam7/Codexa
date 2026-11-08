# Vulnerability Blast Radius Engine

Codexa calculates the **Blast Radius** of security vulnerabilities by traversing the call graph backwards from the sink to all exposed entrypoints.

---

## Metric Formula
$$\text{Blast Radius} = \min\left(1.0, 0.4 + (N_{\text{endpoints}} \times 0.15) + \frac{1.0}{\max(1, H_{\text{min}})}\right)$$

Where:
- $N_{\text{endpoints}}$: Number of public HTTP or RPC routes reaching the vulnerable sink.
- $H_{\text{min}}$: Shortest hop distance between untrusted input and vulnerable execution.
