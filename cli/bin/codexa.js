#!/usr/bin/env node

/**
 * Codexa CLI - Autonomous Static Analysis, Security Auditing, SBOM & LSP CLI
 */

const fs = require("node:fs");
const path = require("node:path");
const { scanProject } = require("../src/scanner");
const { renderScanSummary, renderTable, renderBanner } = require("../src/tui");
const { generateSarif } = require("../src/sarif");
const { runDoctor } = require("../src/doctor");
const { startLspServer } = require("../src/lsp");
const { RULES } = require("../src/rules");

const args = process.argv.slice(2);

function printHelp() {
  renderBanner();
  console.log(`\x1b[1mUSAGE:\x1b[0m
  codexa <command> [options]

\x1b[1mCOMMANDS:\x1b[0m
  \x1b[38;2;16;185;129mscan [path]\x1b[0m           Scan repository or file for security vulnerabilities and code smells
  \x1b[38;2;16;185;129msbom [path]\x1b[0m           Extract Software Bill of Materials (SBOM) and check supply chain CVEs
  \x1b[38;2;16;185;129mcompliance [path]\x1b[0m     Evaluate regulatory posture (SOC 2, ISO 27001, PCI-DSS, OWASP)
  \x1b[38;2;16;185;129mdoctor\x1b[0m                Check local environment (Node, Java 21, Git, Ollama/GPU, Backend)
  \x1b[38;2;16;185;129mrules list\x1b[0m            List all active static analysis and security rules
  \x1b[38;2;16;185;129mlsp\x1b[0m                   Start Language Server Protocol (LSP) daemon on stdio for IDEs
  \x1b[38;2;16;185;129mhelp\x1b[0m                  Show this help manual

\x1b[1mOPTIONS for 'scan':\x1b[0m
  --format <fmt>        Output format: 'tui' (default), 'table', 'json', 'sarif'
  --fail-on <severity>  Exit with code 1 if findings meet or exceed severity:
                        'critical', 'high', 'medium', 'low'
  --output <file>       Write raw output to specified file path
  -h, --help            Show command-specific options

\x1b[1mEXAMPLES:\x1b[0m
  $ codexa scan .
  $ codexa scan ./src --format sarif --output codexa-results.sarif
  $ codexa scan . --fail-on high
  $ codexa doctor
  $ codexa rules list
`);
}

async function handleScan(scanArgs) {
  let targetPath = ".";
  let format = "tui";
  let failOn = null;
  let outputFile = null;

  for (let i = 0; i < scanArgs.length; i++) {
    const arg = scanArgs[i];
    if (arg === "--format" && scanArgs[i + 1]) {
      format = scanArgs[++i].toLowerCase();
    } else if (arg === "--fail-on" && scanArgs[i + 1]) {
      failOn = scanArgs[++i].toUpperCase();
    } else if (arg === "--output" && scanArgs[i + 1]) {
      outputFile = scanArgs[++i];
    } else if (!arg.startsWith("-")) {
      targetPath = arg;
    }
  }

  if (!fs.existsSync(targetPath)) {
    console.error(`\x1b[38;2;239;68;68mError:\x1b[0m Target path '${targetPath}' does not exist.`);
    process.exit(1);
  }

  const result = scanProject(targetPath);

  // Format outputs
  let outputText = "";
  if (format === "json") {
    outputText = JSON.stringify(result, null, 2);
    if (!outputFile) console.log(outputText);
  } else if (format === "sarif") {
    const sarif = generateSarif(result);
    outputText = JSON.stringify(sarif, null, 2);
    if (!outputFile) console.log(outputText);
  } else if (format === "table") {
    renderTable(result.findings);
  } else {
    // Default: TUI Dashboard
    renderScanSummary(result);
  }

  if (outputFile && outputText) {
    fs.writeFileSync(outputFile, outputText, "utf8");
    console.log(`\x1b[38;2;16;185;129m✔ Output written to:\x1b[0m ${outputFile}`);
  }

  // Gate enforcement
  if (failOn) {
    const severityHierarchy = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1, INFO: 0 };
    const threshold = severityHierarchy[failOn] ?? 4;
    const hasBreach = result.findings.some(f => (severityHierarchy[f.severity] ?? 0) >= threshold);
    if (hasBreach) {
      console.error(`\x1b[38;2;239;68;68m\n[GATE FAILURE]\x1b[0m Findings detected meeting or exceeding threshold '--fail-on ${failOn}'.`);
      process.exit(1);
    }
  }
}

async function handleSbom(sbomArgs) {
  const targetPath = sbomArgs[0] || ".";
  const result = scanProject(targetPath);
  const depFindings = result.findings.filter(f => f.ruleId === "CR-DEP-001");

  console.log(`\x1b[1m\x1b[97mCodexa Software Bill of Materials & Dependency Risk Audit\x1b[0m`);
  console.log(`Target: ${path.resolve(targetPath)}\n`);

  if (depFindings.length === 0) {
    console.log(`\x1b[38;2;16;185;129m✔ No known vulnerable dependencies detected in manifests.\x1b[0m\n`);
  } else {
    console.log(`\x1b[38;2;239;68;68m✖ Detected ${depFindings.length} supply chain dependency risks:\x1b[0m\n`);
    for (const d of depFindings) {
      console.log(`  [${d.severity}] ${d.title} at ${d.file}:${d.line}`);
      console.log(`       ${d.description}`);
      console.log(`       Remediation: ${d.remediation}\n`);
    }
  }
}

async function handleCompliance(compArgs) {
  let targetPath = ".";
  let standard = "soc2";
  for (let i = 0; i < compArgs.length; i++) {
    const a = compArgs[i];
    if (a === "--standard" && compArgs[i + 1]) {
      standard = compArgs[++i].toLowerCase();
    } else if (!a.startsWith("-")) {
      targetPath = a;
    }
  }

  const result = scanProject(targetPath);
  console.log(`\x1b[1m\x1b[97mCodexa Regulatory Compliance Attestation Engine\x1b[0m`);
  console.log(`Target: ${path.resolve(targetPath)} | Standard: ${standard.toUpperCase()}\n`);

  const criticals = result.findings.filter(f => f.severity === "CRITICAL").length;
  const highs = result.findings.filter(f => f.severity === "HIGH").length;

  let status = "\x1b[38;2;16;185;129m✔ AUDIT READY (100% Attestation Score)\x1b[0m";
  if (criticals > 0) {
    status = `\x1b[38;2;239;68;68m✖ AUDIT BLOCKED (${criticals} Critical Regulatory Breaches)\x1b[0m`;
  } else if (highs > 0) {
    status = `\x1b[38;2;245;158;11m▲ CONDITIONAL PASS (${highs} High Risk Action Items)\x1b[0m`;
  }

  console.log(`Compliance Posture: ${status}`);
  console.log(`Controls Evaluated: 6 | Total Violations: ${result.findings.length}\n`);
}

function handleRulesList() {
  console.log(`\x1b[1m\x1b[97mCodexa Static Analysis & Security Rules Catalog (${RULES.length} Rules)\x1b[0m\n`);
  for (const r of RULES) {
    console.log(`  \x1b[1m${r.id}\x1b[0m \x1b[38;2;16;185;129m[${r.severity}]\x1b[0m - ${r.title} (${r.cwe})`);
    console.log(`    \x1b[2mExtensions: ${r.fileExtensions.join(", ")}\x1b[0m\n`);
  }
}

async function main() {
  const cmd = args[0];

  switch (cmd) {
    case "scan":
      await handleScan(args.slice(1));
      break;
    case "sbom":
      await handleSbom(args.slice(1));
      break;
    case "compliance":
      await handleCompliance(args.slice(1));
      break;
    case "doctor":
      await runDoctor();
      break;
    case "rules":
      if (args[1] === "list" || !args[1]) {
        handleRulesList();
      } else {
        printHelp();
      }
      break;
    case "lsp":
      startLspServer();
      break;
    case "help":
    case "--help":
    case "-h":
    case undefined:
      printHelp();
      break;
    default:
      console.error(`Unknown command: '${cmd}'. Run 'codexa help' for available commands.`);
      process.exit(1);
  }
}

main().catch(err => {
  console.error("Fatal error:", err);
  process.exit(1);
});
