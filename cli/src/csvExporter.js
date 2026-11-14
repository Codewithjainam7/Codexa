/**
 * RFC 4180 Compliant CSV Exporter with Formula Injection Sanitization.
 */

function sanitizeCsvCell(value) {
  if (value === null || value === undefined) {
    return "";
  }
  let str = String(value).trim();
  // Neutralize CSV formula injection (=, +, -, @)
  if (/^[=+\-@\t\r]/.test(str)) {
    str = "'" + str;
  }
  // Escape internal double quotes
  if (str.includes('"') || str.includes(',') || str.includes('\n') || str.includes('\r')) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

function generateCsvReport(findings, options = {}) {
  const headers = ["Rule ID", "Severity", "Title", "File", "Line", "CWE", "Remediation"];
  if (options.includeTimestamp) {
    headers.push("Timestamp");
  }
  const rows = [headers.join(",")];
  const now = options.timestamp || new Date().toISOString();

  if (Array.isArray(findings)) {
    for (const f of findings) {
      const row = [
        sanitizeCsvCell(f.ruleId),
        sanitizeCsvCell(f.severity),
        sanitizeCsvCell(f.title),
        sanitizeCsvCell(f.file),
        sanitizeCsvCell(f.line),
        sanitizeCsvCell(f.cwe || "N/A"),
        sanitizeCsvCell(f.remediation || "")
      ];
      if (options.includeTimestamp) {
        row.push(sanitizeCsvCell(f.timestamp || now));
      }
      rows.push(row.join(","));
    }
  }

  return rows.join("\r\n");
}

module.exports = {
  generateCsvReport,
  sanitizeCsvCell
};
