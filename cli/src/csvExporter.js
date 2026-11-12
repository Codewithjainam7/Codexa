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

function generateCsvReport(findings) {
  const headers = ["Rule ID", "Severity", "Title", "File", "Line", "CWE", "Remediation"];
  const rows = [headers.join(",")];

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
      rows.push(row.join(","));
    }
  }

  return rows.join("\r\n");
}

module.exports = {
  generateCsvReport,
  sanitizeCsvCell
};
