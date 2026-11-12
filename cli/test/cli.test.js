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
  assert(rulesOutput.includes("CR-XXE-001"), "Rules list should display CR-XXE-001");
  assert(rulesOutput.includes("CR-SSTI-001"), "Rules list should display CR-SSTI-001");
});

test("Scanner: should detect XXE, SSTI, JWT None, ReDoS and Server Action vulnerabilities", () => {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-rules-test-"));

  // 1. XXE test file
  fs.writeFileSync(path.join(tmpDir, "Parser.java"), `DocumentBuilderFactory.newInstance();\n`);

  // 2. SSTI test file
  fs.writeFileSync(path.join(tmpDir, "view.py"), `jinja2.Template("Hello " + req.name)\n`);

  // 3. JWT None test file
  fs.writeFileSync(path.join(tmpDir, "auth.js"), `jwt.decode(token, { verify: false });\n`);

  // 4. ReDoS test file
  fs.writeFileSync(path.join(tmpDir, "validate.js"), `const r = new RegExp("(a+)+");\n`);

  // 5. Server Action test file
  fs.writeFileSync(path.join(tmpDir, "action.ts"), `'use server';\nexport async function mutate() {}\n`);

  const result = scanProject(tmpDir);
  const foundRuleIds = result.findings.map(f => f.ruleId);

  assert(foundRuleIds.includes("CR-XXE-001"), "Expected CR-XXE-001 detection");
  assert(foundRuleIds.includes("CR-SSTI-001"), "Expected CR-SSTI-001 detection");
  assert(foundRuleIds.includes("CR-JWT-001"), "Expected CR-JWT-001 detection");
  assert(foundRuleIds.includes("CR-REDOS-001"), "Expected CR-REDOS-001 detection");
  assert(foundRuleIds.includes("CR-RSC-001"), "Expected CR-RSC-001 detection");

  // Verify Halstead metrics were computed
  const singleFileResult = scanFile(path.join(tmpDir, "Parser.java"), tmpDir);
  assert(singleFileResult.halstead, "Expected Halstead metrics on scanFile");
  assert(singleFileResult.halstead.maintainabilityIndex > 0, "Expected non-zero Maintainability Index");

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Entropy Engine: should calculate accurate Shannon entropy and identify secrets", () => {
  const { calculateShannonEntropy, isHighEntropyToken } = require("../src/entropy");

  // Low entropy repetitive string
  const lowEntropy = calculateShannonEntropy("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
  assert.strictEqual(lowEntropy, 0);

  // Standard identifier
  const identifierEntropy = calculateShannonEntropy("getUserProfileByAccountIdentifier");
  assert(identifierEntropy < 4.2);

  // High entropy cryptographic API key
  const secretKey = "4f8a9b2c3d1e0f7a6b5c4d3e2f1a0b9c";
  assert(calculateShannonEntropy(secretKey) > 3.2);
  assert.strictEqual(isHighEntropyToken(secretKey), true);
});

test("Cache Engine: should store and retrieve content hashes", () => {
  const { computeHash, loadCache, saveCache } = require("../src/cache");
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-cache-test-"));

  const hash1 = computeHash("console.log('hello world');");
  const hash2 = computeHash("console.log('hello world');");
  assert.strictEqual(hash1, hash2);

  saveCache(tmpDir, { "test.js": { hash: hash1, findings: [] } });
  const loaded = loadCache(tmpDir);
  assert.strictEqual(loaded["test.js"].hash, hash1);

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Baseline Delta Engine: should compute new and fixed findings against baseline", () => {
  const { computeDelta } = require("../src/baseline");
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-baseline-test-"));
  const sarifFile = path.join(tmpDir, "baseline.sarif");

  fs.writeFileSync(sarifFile, JSON.stringify({
    runs: [{
      results: [
        { ruleId: "CR-SQL-001", locations: [{ physicalLocation: { artifactLocation: { uri: "User.java" }, region: { startLine: 10 } } }] }
      ]
    }]
  }));

  const currentFindings = [
    { ruleId: "CR-SQL-001", file: "User.java", line: 10 },
    { ruleId: "CR-XXE-001", file: "Parser.java", line: 5 } // New regression!
  ];

  const delta = computeDelta(currentFindings, sarifFile);
  assert.strictEqual(delta.newFindings.length, 1);
  assert.strictEqual(delta.newFindings[0].ruleId, "CR-XXE-001");
  assert.strictEqual(delta.existingFindings.length, 1);

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Autofix Engine: should apply deterministic code patches in place", () => {
  const { applyAutofix } = require("../src/autofix");
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-autofix-test-"));
  const targetFile = path.join(tmpDir, "token.js");

  fs.writeFileSync(targetFile, `const token = Math.random();\n`);
  const fixed = applyAutofix(targetFile, { ruleId: "CR-RAND-001", line: 1 });
  assert.strictEqual(fixed, true);

  const updatedContent = fs.readFileSync(targetFile, "utf8");
  assert(updatedContent.includes("crypto.randomBytes"));

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("Policy Engine: should enforce CI/CD threshold rules and exit codes", () => {
  const { evaluatePolicy } = require("../src/policy");

  const findings = [
    { severity: "CRITICAL" },
    { severity: "HIGH" }
  ];

  // Default: fails on > 0 critical
  const policy1 = evaluatePolicy(findings, 70);
  assert.strictEqual(policy1.passed, false);
  assert.strictEqual(policy1.exitCode, 1);

  // Relaxed policy: max 1 critical allowed, min score 65
  const policy2 = evaluatePolicy(findings, 70, { maxCritical: 1, minScore: 65 });
  assert.strictEqual(policy2.passed, true);
  assert.strictEqual(policy2.exitCode, 0);
});

test("HTML Exporter: should generate valid self-contained HTML audit report", () => {
  const { generateHtmlReport } = require("../src/htmlReport");
  const html = generateHtmlReport({
    target: "my-service",
    score: 85,
    grade: "B",
    findings: [
      { severity: "HIGH", ruleId: "CR-SEC-001", title: "Hardcoded secret", file: "app.js", line: 12, snippet: "apiKey='xyz'", remediation: "Use env var" }
    ]
  });

  assert(html.includes("<!DOCTYPE html>"));
  assert(html.includes("Codexa Static Security Audit Report"));
  assert(html.includes("CR-SEC-001"));
  assert(html.includes("85</span>/100"));
});

test("Blame Engine: should annotate findings with git blame metadata", () => {
  const { BlameEngine } = require("../src/blame");
  const blameEngine = new BlameEngine(path.resolve(__dirname, ".."));
  const findings = [
    { ruleId: "CR-SEC-001", file: "package.json", line: 1 }
  ];
  const annotated = blameEngine.annotateFindings(findings);
  assert.strictEqual(annotated.length, 1);
  assert(annotated[0].provenance !== undefined);
  assert(typeof annotated[0].provenance.author === "string");
});

test("TUI Renderer: should render interactive triage without throwing", () => {
  const { renderInteractiveTriage } = require("../src/tui");
  const findings = [
    { ruleId: "CR-SEC-001", severity: "HIGH", title: "Test finding", file: "main.js", line: 5, snippet: "token = 123", remediation: "fix it" }
  ];
  // Verify it executes cleanly
  assert.doesNotThrow(() => {
    renderInteractiveTriage(findings, "HIGH");
  });
});

test("Suppression Engine: should filter out in-source inline suppressed findings", () => {
  const { SuppressionEngine } = require("../src/suppression");
  const engine = new SuppressionEngine(__dirname);

  const fileContent = `
    // codexa-ignore CR-SQL-001: Verified parameterized query in wrapper
    const query = "SELECT * FROM users WHERE id = " + id;
  `;

  const findings = [
    { ruleId: "CR-SQL-001", file: "test.js", line: 3 }
  ];

  const { activeFindings, suppressedFindings } = engine.processSuppressions(findings, {
    "test.js": fileContent
  });

  assert.strictEqual(activeFindings.length, 0);
  assert.strictEqual(suppressedFindings.length, 1);
  assert.strictEqual(suppressedFindings[0].suppressions[0].kind, "inSource");
  assert(suppressedFindings[0].suppressions[0].justification.includes("Verified parameterized"));
});

test("License Auditor: should classify permissive vs strong copyleft licenses", () => {
  const { LicenseAuditor } = require("../src/licenseAuditor");
  const auditor = new LicenseAuditor();

  const mit = auditor.classifyLicense("MIT");
  assert.strictEqual(mit.type, "PERMISSIVE");
  assert.strictEqual(mit.risk, "LOW");

  const gpl = auditor.classifyLicense("GPL-3.0");
  assert.strictEqual(gpl.type, "STRONG_COPYLEFT");
  assert.strictEqual(gpl.risk, "CRITICAL");
  assert.strictEqual(gpl.copyleft, true);

  const lgpl = auditor.classifyLicense("LGPL-3.0");
  assert.strictEqual(lgpl.type, "WEAK_COPYLEFT");
  assert.strictEqual(lgpl.risk, "MEDIUM");
});

test("Worker Pool Scanner: should scan files concurrently across worker lanes", async () => {
  const { WorkerPoolScanner } = require("../src/workerPool");
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "codexa-workerpool-"));

  const f1 = path.join(tmpDir, "UserRepository.java");
  const f2 = path.join(tmpDir, "config.js");
  fs.writeFileSync(f1, `String query = "SELECT * FROM users WHERE id = '" + id + "'";\n`);
  fs.writeFileSync(f2, `const awsKey = "AKIAIOSFODNN7TESTKEY1";\n`);

  const scanner = new WorkerPoolScanner(2);
  let progressCalled = 0;
  const result = await scanner.scanBatch([f1, f2], () => {
    progressCalled++;
  });

  assert.strictEqual(result.filesScanned, 2);
  assert.strictEqual(progressCalled, 2);
  assert(result.findings.length >= 2);
  assert(result.concurrency === 2);

  fs.rmSync(tmpDir, { recursive: true, force: true });
});

test("CycloneDX SBOM Generator: should emit valid CycloneDX v1.5 JSON spec", () => {
  const { CycloneDxSbomGenerator } = require("../src/sbomGenerator");
  const generator = new CycloneDxSbomGenerator(__dirname);

  const sbom = generator.generateSbom(
    { name: "codexa-core", version: "3.5.0" },
    [
      { name: "express", version: "4.19.2", license: "MIT", dev: false },
      { name: "mocha", version: "10.4.0", dev: true }
    ],
    [
      { ruleId: "CR-DEP-001", cve: "CVE-2024-1234", severity: "HIGH", title: "Test Advisory", purl: "pkg:npm/express@4.19.2" }
    ]
  );

  assert.strictEqual(sbom.bomFormat, "CycloneDX");
  assert.strictEqual(sbom.specVersion, "1.5");
  assert(sbom.serialNumber.startsWith("urn:uuid:"));
  assert.strictEqual(sbom.components.length, 2);
  assert.strictEqual(sbom.components[0].purl, "pkg:npm/express@4.19.2");
  assert.strictEqual(sbom.vulnerabilities.length, 1);
  assert.strictEqual(sbom.vulnerabilities[0].id, "CVE-2024-1234");
});

test("CSV Exporter: should serialize findings to RFC 4180 CSV and sanitize formulas", () => {
  const { generateCsvReport, sanitizeCsvCell } = require("../src/csvExporter");
  
  // Formula injection check
  assert.strictEqual(sanitizeCsvCell("=CMD('calc')"), "'=CMD('calc')");
  assert.strictEqual(sanitizeCsvCell("+123"), "'+123");

  const findings = [
    {
      ruleId: "CR-SQL-001",
      severity: "CRITICAL",
      title: "SQL Injection",
      file: "User.java",
      line: 42,
      cwe: "CWE-89",
      remediation: "Use PreparedStatement"
    }
  ];

  const csv = generateCsvReport(findings);
  assert(csv.includes("Rule ID,Severity,Title,File,Line,CWE,Remediation"));
  assert(csv.includes("CR-SQL-001,CRITICAL,SQL Injection,User.java,42,CWE-89,Use PreparedStatement"));
});

test("Markdown Exporter: should generate valid pull request review comment markdown", () => {
  const { generateMarkdownReport } = require("../src/markdownReport");

  const result = {
    target: "my-app",
    metrics: { score: 92, grade: "A", status: "PRODUCTION_READY", breakdown: { critical: 0, high: 1, medium: 1, low: 0, total: 2 } },
    findings: [
      { ruleId: "CR-OAUTH-001", severity: "HIGH", title: "OAuth Open Redirect", file: "auth.js", line: 15, remediation: "Enforce PKCE" }
    ]
  };

  const md = generateMarkdownReport(result);
  assert(md.includes("## 🛡️ Codexa Security & Production Readiness Audit"));
  assert(md.includes("🟢 **PRODUCTION READY**"));
  assert(md.includes("CR-OAUTH-001"));
  assert(md.includes("Enforce PKCE"));
  assert(md.includes("<details>"));
});
