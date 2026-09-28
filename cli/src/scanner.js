/**
 * Codexa High-Throughput CLI File Scanner
 * Inspects files recursively against static analysis rules and software manifests.
 */

const fs = require("node:fs");
const path = require("node:path");
const { getRulesForExtension } = require("./rules");

const IGNORED_DIRS = new Set([
  ".git",
  ".github",
  "node_modules",
  "target",
  "dist",
  "build",
  ".staging",
  ".idea",
  ".vscode",
  ".next",
  "vendor",
  "bin",
  "obj"
]);

const IGNORED_FILES = new Set([
  "package-lock.json",
  "yarn.lock",
  "pnpm-lock.yaml"
]);

// Known dependency vulnerabilities for quick offline SBOM scanning
const ADVISORIES = [
  { match: /log4j-core.*2\.(?:[0-9]|1[0-4]|1[5-6])\./, id: "CVE-2021-44228", severity: "CRITICAL", desc: "Log4j JNDI LDAP Remote Code Execution" },
  { match: /spring-beans.*5\.(?:2\.[0-9]|3\.[0-9]|3\.1[0-7])\./, id: "CVE-2022-22965", severity: "CRITICAL", desc: "Spring4Shell ClassLoader DataBinder RCE" },
  { match: /snakeyaml.*1\.(?:[0-9]|[1-2][0-9]|3[0-2])\./, id: "CVE-2022-1471", severity: "CRITICAL", desc: "SnakeYAML Constructor Remote Code Execution" },
  { match: /lodash.*(?:3\.|4\.(?:[0-9]|1[0-6])\.)/, id: "CVE-2021-23337", severity: "HIGH", desc: "Lodash Prototype Pollution via template function" }
];

function scanFile(filePath, rootDir) {
  const ext = path.extname(filePath);
  const relativePath = path.relative(rootDir, filePath).replace(/\\/g, "/");
  const fileName = path.basename(filePath);
  const findings = [];

  let content;
  try {
    content = fs.readFileSync(filePath, "utf8");
  } catch (e) {
    return { findings, lineCount: 0 };
  }

  const lines = content.split(/\r?\n/);
  const rules = getRulesForExtension(ext);

  // 1. Static Rule Pattern Evaluation
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const lineNum = i + 1;

    for (const rule of rules) {
      const match = rule.check(line, lineNum);
      if (match) {
        findings.push({
          ruleId: rule.id,
          title: rule.title,
          category: rule.category,
          severity: rule.severity,
          cwe: rule.cwe,
          file: relativePath,
          line: lineNum,
          snippet: line.trim().slice(0, 120),
          description: match.description,
          remediation: match.remediation
        });
      }
    }
  }

  // 2. Manifest Dependency Audit (pom.xml, package.json, requirements.txt)
  if (fileName === "pom.xml" || fileName === "package.json" || fileName === "requirements.txt") {
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const lineNum = i + 1;
      for (const adv of ADVISORIES) {
        if (adv.match.test(line)) {
          findings.push({
            ruleId: "CR-DEP-001",
            title: `Vulnerable Dependency: ${adv.id}`,
            category: "SECURITY",
            severity: adv.severity,
            cwe: "CWE-1395",
            file: relativePath,
            line: lineNum,
            snippet: line.trim().slice(0, 120),
            description: `${adv.desc} (${adv.id}) detected in manifest.`,
            remediation: "Upgrade the vulnerable library version to the latest patched release."
          });
        }
      }
    }
  }

  return { findings, lineCount: lines.length };
}

function walkDirectory(dir, fileList = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (!IGNORED_DIRS.has(entry.name)) {
        walkDirectory(fullPath, fileList);
      }
    } else if (entry.isFile()) {
      if (!IGNORED_FILES.has(entry.name)) {
        fileList.push(fullPath);
      }
    }
  }
  return fileList;
}

function calculateReadinessScore(findings) {
  let critical = 0;
  let high = 0;
  let medium = 0;
  let low = 0;
  let info = 0;

  for (const f of findings) {
    switch (f.severity) {
      case "CRITICAL": critical++; break;
      case "HIGH": high++; break;
      case "MEDIUM": medium++; break;
      case "LOW": low++; break;
      default: info++; break;
    }
  }

  const penalty = (critical * 25) + (high * 15) + (medium * 8) + (low * 3);
  const score = Math.max(0, Math.min(100, Math.round(100 - penalty)));

  let grade = "F";
  if (score >= 95) grade = "A+";
  else if (score >= 90) grade = "A";
  else if (score >= 80) grade = "B";
  else if (score >= 70) grade = "C";
  else if (score >= 60) grade = "D";

  let status = "PRODUCTION_READY";
  if (critical > 0 || high > 3 || score < 65) {
    status = "BLOCKED";
  } else if (score < 85 || high > 0) {
    status = "REQUIRES_REVIEW";
  }

  return {
    score,
    grade,
    status,
    breakdown: { critical, high, medium, low, info, total: findings.length }
  };
}

function scanProject(targetPath) {
  const resolvedTarget = path.resolve(targetPath);
  const startTime = Date.now();

  const isDirectory = fs.statSync(resolvedTarget).isDirectory();
  const filesToScan = isDirectory ? walkDirectory(resolvedTarget) : [resolvedTarget];
  const rootDir = isDirectory ? resolvedTarget : path.dirname(resolvedTarget);

  const allFindings = [];
  let totalLines = 0;

  for (const file of filesToScan) {
    const { findings, lineCount } = scanFile(file, rootDir);
    totalLines += lineCount;
    allFindings.push(...findings);
  }

  const durationMs = Date.now() - startTime;
  const metrics = calculateReadinessScore(allFindings);

  return {
    target: resolvedTarget,
    filesScanned: filesToScan.length,
    linesScanned: totalLines,
    durationMs,
    metrics,
    findings: allFindings
  };
}

module.exports = {
  scanProject,
  scanFile,
  calculateReadinessScore
};
