/**
 * Codexa Monochromatic Luxury Terminal UI (TUI) Renderer
 * Styled with ANSI escape codes matching Codexa's OLED obsidian design system.
 */

const colors = {
  reset: "\x1b[0m",
  bold: "\x1b[1m",
  dim: "\x1b[2m",
  italic: "\x1b[3m",
  underline: "\x1b[4m",
  
  // Foreground
  fgBlack: "\x1b[30m",
  fgWhite: "\x1b[97m",
  fgEmerald: "\x1b[38;2;16;185;129m",
  fgRed: "\x1b[38;2;239;68;68m",
  fgAmber: "\x1b[38;2;245;158;11m",
  fgYellow: "\x1b[38;2;234;179;8m",
  fgBlue: "\x1b[38;2;59;130;246m",
  fgCyan: "\x1b[38;2;6;182;212m",
  fgMuted: "\x1b[38;2;148;163;184m",
  
  // Background
  bgObsidian: "\x1b[48;2;10;10;10m",
  bgRed: "\x1b[48;2;153;27;27m",
  bgAmber: "\x1b[48;2;146;64;14m",
  bgEmerald: "\x1b[48;2;6;78;59m",
  bgMuted: "\x1b[48;2;30;41;59m"
};

function renderBanner() {
  console.log(`${colors.fgWhite}${colors.bold}`);
  console.log("   ██████╗ ██████╗ ██████╗ ███████╗██╗  ██╗ █████╗ ");
  console.log("  ██╔════╝██╔═══██╗██╔══██╗██╔════╝╚██╗██╔╝██╔══██╗");
  console.log("  ██║     ██║   ██║██║  ██║█████╗   ╚███╔╝ ███████║");
  console.log("  ██║     ██║   ██║██║  ██║██╔══╝   ██╔██╗ ██╔══██║");
  console.log("  ╚██████╗╚██████╔╝██████╔╝███████╗██╔╝ ██╗██║  ██║");
  console.log("   ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝");
  console.log(`${colors.reset}${colors.fgMuted}  Autonomous Static Analysis & Security Auditing Engine v1.0.0${colors.reset}\n`);
}

function renderProgressBar(score, width = 28) {
  const filled = Math.round((score / 100) * width);
  const empty = width - filled;
  
  let barColor = colors.fgEmerald;
  if (score < 65) barColor = colors.fgRed;
  else if (score < 85) barColor = colors.fgAmber;
  
  const filledBar = "█".repeat(filled);
  const emptyBar = "░".repeat(empty);
  
  return `${barColor}${filledBar}${colors.dim}${emptyBar}${colors.reset}`;
}

function getSeverityBadge(severity) {
  switch (severity) {
    case "CRITICAL":
      return `${colors.bgRed}${colors.fgWhite}${colors.bold} CRITICAL ${colors.reset}`;
    case "HIGH":
      return `${colors.bgAmber}${colors.fgWhite}${colors.bold}   HIGH   ${colors.reset}`;
    case "MEDIUM":
      return `${colors.bgMuted}${colors.fgYellow}${colors.bold}  MEDIUM  ${colors.reset}`;
    case "LOW":
      return `${colors.bgMuted}${colors.fgBlue}${colors.bold}   LOW    ${colors.reset}`;
    default:
      return `${colors.bgMuted}${colors.fgWhite}${colors.bold}   INFO   ${colors.reset}`;
  }
}

function renderScanSummary(result) {
  renderBanner();

  const { target, filesScanned, linesScanned, durationMs, metrics, findings } = result;
  const { score, grade, status, breakdown } = metrics;

  console.log(`${colors.bold}${colors.fgWhite}Target:${colors.reset} ${target}`);
  console.log(`${colors.dim}Files: ${filesScanned} | Lines: ${linesScanned.toLocaleString()} | Scan Duration: ${durationMs}ms${colors.reset}\n`);

  // Score Dashboard Card
  console.log(`${colors.bold}─── PRODUCTION READINESS SCORE ────────────────────────────${colors.reset}`);
  const scoreDisplay = `${score} / 100 (Grade: ${grade})`;
  let statusBadge = `${colors.fgEmerald}[PRODUCTION READY]${colors.reset}`;
  if (status === "BLOCKED") statusBadge = `${colors.fgRed}${colors.bold}[BLOCKED - GATES FAILED]${colors.reset}`;
  else if (status === "REQUIRES_REVIEW") statusBadge = `${colors.fgAmber}[REQUIRES SECURITY REVIEW]${colors.reset}`;

  console.log(` Score: ${renderProgressBar(score)} ${colors.bold}${scoreDisplay}${colors.reset}  ${statusBadge}`);
  console.log(`${colors.dim}───────────────────────────────────────────────────────────${colors.reset}`);

  // Breakdown Table
  console.log(` ${colors.fgRed}● Critical: ${breakdown.critical}${colors.reset}  ` +
              ` ${colors.fgAmber}▲ High: ${breakdown.high}${colors.reset}  ` +
              ` ${colors.fgYellow}◆ Medium: ${breakdown.medium}${colors.reset}  ` +
              ` ${colors.fgBlue}■ Low: ${breakdown.low}${colors.reset}  ` +
              ` ${colors.fgMuted}Total Issues: ${breakdown.total}${colors.reset}\n`);

  // Top Findings Cards
  if (findings.length > 0) {
    console.log(`${colors.bold}${colors.fgWhite}─── AUDIT FINDINGS (${Math.min(10, findings.length)} of ${findings.length} shown) ─────────────────────${colors.reset}\n`);
    
    // Sort critical first
    const sorted = [...findings].sort((a, b) => {
      const order = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3, INFO: 4 };
      return (order[a.severity] ?? 5) - (order[b.severity] ?? 5);
    });

    const displayFindings = sorted.slice(0, 10);

    for (let i = 0; i < displayFindings.length; i++) {
      const f = displayFindings[i];
      const badge = getSeverityBadge(f.severity);
      
      console.log(` ${badge} ${colors.bold}${f.title}${colors.reset} ${colors.dim}(${f.ruleId} · ${f.cwe})${colors.reset}`);
      console.log(`   ${colors.fgCyan}--> ${f.file}:${f.line}${colors.reset}`);
      console.log(`   ${colors.dim}| ${f.snippet}${colors.reset}`);
      console.log(`   ${colors.fgEmerald}✔ Remediation:${colors.reset} ${f.remediation}`);
      console.log("");
    }

    if (findings.length > 10) {
      console.log(`${colors.dim}  ... and ${findings.length - 10} more findings. Pass --format json or --format sarif for full output.${colors.reset}\n`);
    }
  } else {
    console.log(`${colors.fgEmerald}${colors.bold}✔ No security or code quality vulnerabilities detected! Codebase is production-ready.${colors.reset}\n`);
  }
}

function renderTable(findings) {
  if (findings.length === 0) {
    console.log("No findings detected.");
    return;
  }
  console.log("SEVERITY\tRULE\tFILE:LINE\tTITLE");
  console.log("────────────────────────────────────────────────────────────────────────");
  for (const f of findings) {
    console.log(`${f.severity}\t${f.ruleId}\t${f.file}:${f.line}\t${f.title}`);
  }
}

module.exports = {
  renderBanner,
  renderScanSummary,
  renderTable
};
