/**
 * Codexa CLI Offline Executive HTML Report Exporter.
 */

function generateHtmlReport(scanResult) {
  const { target, findings = [], score = 100, grade = "A+" } = scanResult;

  const critical = findings.filter(f => f.severity === "CRITICAL").length;
  const high = findings.filter(f => f.severity === "HIGH").length;
  const medium = findings.filter(f => f.severity === "MEDIUM").length;

  const findingRows = findings.map(f => `
    <tr class="severity-${f.severity.toLowerCase()}">
      <td><span class="badge ${f.severity.toLowerCase()}">${f.severity}</span></td>
      <td><strong>${escapeHtml(f.ruleId)}</strong><br><small>${escapeHtml(f.title)}</small></td>
      <td><code>${escapeHtml(f.file)}:${f.line}</code></td>
      <td><pre>${escapeHtml(f.snippet || "")}</pre></td>
      <td>${escapeHtml(f.remediation || "")}</td>
    </tr>
  `).join("\n");

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Codexa Security Audit Report - ${escapeHtml(target || "Scan")}</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; margin: 0; }
    .container { max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #334155; padding-bottom: 1.5rem; margin-bottom: 2rem; }
    .score-card { background: #1e293b; padding: 1.5rem; border-radius: 12px; border: 1px solid #334155; display: inline-block; }
    .score-val { font-size: 3rem; font-weight: bold; color: ${score >= 80 ? "#10b981" : score >= 60 ? "#f59e0b" : "#ef4444"}; }
    table { width: 100%; border-collapse: collapse; margin-top: 2rem; background: #1e293b; border-radius: 8px; overflow: hidden; }
    th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #334155; }
    th { background: #0b1120; color: #94a3b8; font-size: 0.85rem; text-transform: uppercase; }
    pre { margin: 0; background: #0f172a; padding: 6px; border-radius: 4px; font-size: 0.85rem; color: #38bdf8; }
    .badge { padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.75rem; }
    .badge.critical { background: #ef4444; color: #fff; }
    .badge.high { background: #f97316; color: #fff; }
    .badge.medium { background: #eab308; color: #000; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div>
        <h1>Codexa Static Security Audit Report</h1>
        <p>Target: <code>${escapeHtml(target || ".")}</code> | Generated: ${new Date().toISOString()}</p>
      </div>
      <div class="score-card">
        <div>Readiness Score: <span class="score-val">${score}</span>/100 (${grade})</div>
        <small>Critical: ${critical} | High: ${high} | Medium: ${medium}</small>
      </div>
    </div>
    <table>
      <thead>
        <tr>
          <th>Severity</th>
          <th>Rule & Issue</th>
          <th>Location</th>
          <th>Evidence Snippet</th>
          <th>Remediation Guidance</th>
        </tr>
      </thead>
      <tbody>
        ${findingRows.length > 0 ? findingRows : '<tr><td colspan="5" style="text-align:center; padding: 2rem;">No security vulnerabilities identified. Repository is production-ready!</td></tr>'}
      </tbody>
    </table>
  </div>
</body>
</html>`;
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

module.exports = {
  generateHtmlReport
};
