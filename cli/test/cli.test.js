const test = require("node:test");
const assert = require("node:assert");
const fs = require("node:fs");
const path = require("node:path");
const os = require("node:os");
const { execSync } = require("node:child_process");

const { scanProject, calculateReadinessScore, scanFile } = require("../src/scanner");
const { generateSarif } = require("../src/sarif");
const { RULES } = require("../src/rules");

test("Rules Catalog: should define valid rules with CWE and severity", () => {
  assert(RULES.length >= 8, "Expected at least 8 static analysis rules");
  for (const rule of RULES) {
    assert(rule.id.startsWith("CR-"), `Rule ID should start with CR-: ${rule.id}`);
    assert(["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"].includes(rule.severity), `Invalid severity: ${rule.severity}`);
    assert(rule.cwe.startsWith("CWE-"), `Invalid CWE: ${rule.cwe}`);
    assert(typeof rule.check === "function", `Rule check should be a function: ${rule.id}`);
  }
});

test("Scanner: should identify SQL injection in code snippet", () => {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-test-"));
  const vulnerableFile = path.join(tmpDir, "UserRepository.java");
  fs.writeFileSync(
    vulnerableFile,
    `public class UserRepository {
       public void findUser(String name) {
         String query = "SELECT * FROM users WHERE username = '" + name + "'";
         db.executeQuery(query);
       }
     }`
  );

  const result = scanProject(tmpDir);
  assert(result.findings.length > 0, "Expected at least one finding for SQL injection");
  const sqlFinding = result.findings.find(f => f.ruleId === "CR-SQL-001");
  assert(sqlFinding, "Expected finding with ruleId CR-SQL-001");
  assert.strictEqual(sqlFinding.severity, "CRITICAL");
  assert(sqlFinding.line === 3 || sqlFinding.line === 4);

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Scanner: should detect hardcoded AWS credentials", () => {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-test-"));
  const secretFile = path.join(tmpDir, "config.js");
  fs.writeFileSync(
    secretFile,
    `const awsKey = "AKIAIOSFODNN7TESTKEY1";\nconst host = "localhost";`
  );

  const result = scanProject(tmpDir);
  const secretFinding = result.findings.find(f => f.ruleId === "CR-SECRET-001");
  assert(secretFinding, "Expected finding with ruleId CR-SECRET-001");
  assert.strictEqual(secretFinding.severity, "CRITICAL");

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Scoring Engine: should calculate accurate production readiness penalty", () => {
  // 1. Clean code
  const cleanScore = calculateReadinessScore([]);
  assert.strictEqual(cleanScore.score, 100);
  assert.strictEqual(cleanScore.grade, "A+");
  assert.strictEqual(cleanScore.status, "PRODUCTION_READY");

  // 2. Single Critical Finding (-25 pts)
  const criticalScore = calculateReadinessScore([
    { severity: "CRITICAL" }
  ]);
  assert.strictEqual(criticalScore.score, 75);
  assert.strictEqual(criticalScore.grade, "C");
  assert.strictEqual(criticalScore.status, "BLOCKED");

  // 3. Multi-finding heavy penalty
  const severeScore = calculateReadinessScore([
    { severity: "CRITICAL" },
    { severity: "CRITICAL" },
    { severity: "HIGH" },
    { severity: "MEDIUM" }
  ]);
  // 100 - (50 + 15 + 8) = 27
  assert.strictEqual(severeScore.score, 27);
  assert.strictEqual(severeScore.grade, "F");
  assert.strictEqual(severeScore.status, "BLOCKED");
});

test("SARIF Exporter: should serialize scan results to OASIS SARIF v2.1.0", () => {
  const mockResult = {
    target: "/mock/project",
    findings: [
      {
        ruleId: "CR-SQL-001",
        title: "SQL Injection",
        category: "SECURITY",
        severity: "CRITICAL",
        cwe: "CWE-89",
        file: "User.java",
        line: 12,
        snippet: "SELECT * FROM users + id",
        description: "Dynamic SQL injection",
        remediation: "Use PreparedStatement"
      }
    ]
  };

  const sarif = generateSarif(mockResult);
  assert.strictEqual(sarif.version, "2.1.0");
  assert.strictEqual(sarif.runs.length, 1);
  assert.strictEqual(sarif.runs[0].tool.driver.name, "Codexa Static Analysis CLI");
  assert.strictEqual(sarif.runs[0].results.length, 1);
  assert.strictEqual(sarif.runs[0].results[0].ruleId, "CR-SQL-001");
  assert.strictEqual(sarif.runs[0].results[0].level, "error");
});

test("CLI Binary: should execute help and rules list commands", () => {
  const cliPath = path.resolve(__dirname, "../bin/codexa.js");

  const helpOutput = execSync(`node "${cliPath}" help`, { encoding: "utf8" });
  assert(helpOutput.includes("USAGE:"), "Help should display usage");
  assert(helpOutput.includes("scan [path]"), "Help should list scan command");

  const rulesOutput = execSync(`node "${cliPath}" rules list`, { encoding: "utf8" });
  assert(rulesOutput.includes("CR-SQL-001"), "Rules list should display CR-SQL-001");
});
