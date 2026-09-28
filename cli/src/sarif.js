/**
 * OASIS SARIF v2.1.0 Formatter for Codexa CLI
 * Standard output format for GitHub Advanced Security and IDE extensions.
 */

function toSarifSeverity(severity) {
  switch (severity) {
    case "CRITICAL":
    case "HIGH":
      return "error";
    case "MEDIUM":
      return "warning";
    case "LOW":
    case "INFO":
    default:
      return "note";
  }
}

function generateSarif(scanResult) {
  const { findings, target } = scanResult;

  const rulesMap = new Map();
  for (const f of findings) {
    if (!rulesMap.has(f.ruleId)) {
      rulesMap.set(f.ruleId, {
        id: f.ruleId,
        name: f.title,
        shortDescription: { text: f.title },
        fullDescription: { text: f.description },
        help: { text: f.remediation },
        properties: {
          category: f.category,
          severity: f.severity,
          cwe: [f.cwe]
        }
      });
    }
  }

  const results = findings.map(f => ({
    ruleId: f.ruleId,
    level: toSarifSeverity(f.severity),
    message: { text: `${f.title}: ${f.description}` },
    locations: [
      {
        physicalLocation: {
          artifactLocation: {
            uri: f.file.replace(/\\/g, "/")
          },
          region: {
            startLine: f.line,
            startColumn: 1,
            snippet: {
              text: f.snippet
            }
          }
        }
      }
    ]
  }));

  return {
    $schema: "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
    version: "2.1.0",
    runs: [
      {
        tool: {
          driver: {
            name: "Codexa Static Analysis CLI",
            version: "1.0.0",
            informationUri: "https://github.com/Codewithjainam7/Codexa",
            rules: Array.from(rulesMap.values())
          }
        },
        results
      }
    ]
  };
}

module.exports = {
  generateSarif
};
