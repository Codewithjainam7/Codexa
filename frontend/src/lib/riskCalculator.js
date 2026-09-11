/**
 * Weighted risk score calculator based on finding severities.
 */
const SEVERITY_WEIGHTS = {
  CRITICAL: 25,
  HIGH: 10,
  MEDIUM: 3,
  LOW: 1,
  INFO: 0,
};

export function calculateRiskScore(findings = []) {
  if (!findings || findings.length === 0) return 0;
  const rawScore = findings.reduce((acc, f) => {
    const sev = (f.severity || 'LOW').toUpperCase();
    return acc + (SEVERITY_WEIGHTS[sev] || 1);
  }, 0);
  return Math.min(100, Math.round(rawScore));
}

export function getRiskLevel(score) {
  if (score >= 75) return { label: 'CRITICAL', color: 'rose' };
  if (score >= 40) return { label: 'HIGH', color: 'orange' };
  if (score >= 15) return { label: 'MEDIUM', color: 'amber' };
  return { label: 'LOW', color: 'emerald' };
}
