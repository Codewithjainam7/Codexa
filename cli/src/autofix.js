/**
 * Codexa CLI In-Place Deterministic Autofix Engine.
 */

const fs = require("node:fs");

function applyAutofix(filePath, finding) {
  if (!fs.existsSync(filePath)) return false;
  const content = fs.readFileSync(filePath, "utf8");
  const lines = content.split(/\r?\n/);
  const targetIndex = finding.line - 1;

  if (targetIndex < 0 || targetIndex >= lines.length) return false;

  let line = lines[targetIndex];
  let modified = false;

  // 1. Math.random() in token context
  if (finding.ruleId === "CR-RAND-001" && line.includes("Math.random()")) {
    line = line.replace(/Math\.random\(\)/g, "crypto.randomBytes(16).toString('hex')");
    modified = true;
  }

  // 2. Disabled verify in JWT
  if (finding.ruleId === "CR-JWT-001" && /verify\s*:\s*false/i.test(line)) {
    line = line.replace(/verify\s*:\s*false/ig, "verify: true");
    modified = true;
  }

  // 3. Permissive CORS
  if (finding.ruleId === "CR-CORS-001" && line.includes("'*'")) {
    line = line.replace(/'\*'/g, "process.env.ALLOWED_ORIGIN || 'https://app.codexa.io'");
    modified = true;
  }

  if (modified) {
    lines[targetIndex] = line;
    fs.writeFileSync(filePath, lines.join("\n"), "utf8");
    return true;
  }

  return false;
}

module.exports = {
  applyAutofix
};
