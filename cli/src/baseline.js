/**
 * Codexa Baseline and Delta Analysis Engine for CI/CD Gates.
 */

const fs = require("node:fs");

function computeDelta(currentFindings, baselineSarifPath) {
  if (!baselineSarifPath || !fs.existsSync(baselineSarifPath)) {
    return {
      newFindings: currentFindings,
      fixedFindings: [],
      existingFindings: []
    };
  }

  let baselineFindings = [];
  try {
    const raw = JSON.parse(fs.readFileSync(baselineSarifPath, "utf8"));
    const runs = raw.runs || [];
    for (const run of runs) {
      for (const res of run.results || []) {
        const file = res.locations?.[0]?.physicalLocation?.artifactLocation?.uri || "";
        const line = res.locations?.[0]?.physicalLocation?.region?.startLine || 0;
        baselineFindings.push({ ruleId: res.ruleId, file, line });
      }
    }
  } catch {
    return { newFindings: currentFindings, fixedFindings: [], existingFindings: [] };
  }

  const baselineFingerprints = new Set(baselineFindings.map(b => `${b.ruleId}::${b.file}`));
  const currentFingerprints = new Set(currentFindings.map(c => `${c.ruleId}::${c.file}`));

  const newFindings = currentFindings.filter(c => !baselineFingerprints.has(`${c.ruleId}::${c.file}`));
  const existingFindings = currentFindings.filter(c => baselineFingerprints.has(`${c.ruleId}::${c.file}`));
  const fixedFindings = baselineFindings.filter(b => !currentFingerprints.has(`${b.ruleId}::${b.file}`));

  return {
    newFindings,
    fixedFindings,
    existingFindings
  };
}

module.exports = {
  computeDelta
};
