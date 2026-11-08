/**
 * Codexa CI/CD Exit Code Policy & Threshold Filter.
 */

function evaluatePolicy(findings, score, options = {}) {
  const maxCritical = options.maxCritical !== undefined ? Number(options.maxCritical) : 0;
  const maxHigh = options.maxHigh !== undefined ? Number(options.maxHigh) : Infinity;
  const minScore = options.minScore !== undefined ? Number(options.minScore) : 0;

  let criticalCount = 0;
  let highCount = 0;

  for (const f of findings) {
    if (f.severity === "CRITICAL") criticalCount++;
    if (f.severity === "HIGH") highCount++;
  }

  const failures = [];

  if (criticalCount > maxCritical) {
    failures.push(`Critical findings count (${criticalCount}) exceeds permitted threshold (${maxCritical})`);
  }
  if (highCount > maxHigh) {
    failures.push(`High findings count (${highCount}) exceeds permitted threshold (${maxHigh})`);
  }
  if (score < minScore) {
    failures.push(`Production Readiness Score (${score}) is below minimum required (${minScore})`);
  }

  return {
    passed: failures.length === 0,
    exitCode: failures.length === 0 ? 0 : 1,
    failures
  };
}

module.exports = {
  evaluatePolicy
};
