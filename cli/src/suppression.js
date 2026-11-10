const fs = require("fs");
const path = require("path");

/**
 * SARIF v2.1.0 Suppression & False-Positive Annotator
 * Supports inline code comments (// codexa-ignore CR-XXX: reason) and .codexaignore files.
 */
class SuppressionEngine {
  constructor(projectDir = process.cwd()) {
    this.projectDir = projectDir;
    this.ignoredRules = new Set();
    this.loadCodexaIgnore();
  }

  loadCodexaIgnore() {
    const ignorePath = path.join(this.projectDir, ".codexaignore");
    if (fs.existsSync(ignorePath)) {
      try {
        const content = fs.readFileSync(ignorePath, "utf-8");
        content.split("\n").forEach((line) => {
          const trimmed = line.trim();
          if (trimmed && !trimmed.startsWith("#")) {
            this.ignoredRules.add(trimmed);
          }
        });
      } catch {
        // Ignore unreadable .codexaignore
      }
    }
  }

  /**
   * Filters and marks findings as suppressed if matching inline comments or .codexaignore.
   */
  processSuppressions(findings, fileContentsMap = {}) {
    const activeFindings = [];
    const suppressedFindings = [];

    for (const finding of findings) {
      // 1. Check project-wide .codexaignore
      if (this.ignoredRules.has(finding.ruleId)) {
        suppressedFindings.push({
          ...finding,
          suppressions: [
            {
              kind: "external",
              status: "accepted",
              justification: "Suppressed via .codexaignore"
            }
          ]
        });
        continue;
      }

      // 2. Check in-source comment suppression
      const fileContent = fileContentsMap[finding.file];
      if (fileContent && finding.line) {
        const lines = fileContent.split(/\r?\n/);
        const currentLine = lines[finding.line - 1] || "";
        const prevLine = finding.line > 1 ? lines[finding.line - 2] : "";

        const suppressRegex = new RegExp(`codexa-(?:ignore|disable)\\s+${finding.ruleId}(?::\\s*(.*))?`, "i");
        const match = currentLine.match(suppressRegex) || prevLine.match(suppressRegex);

        if (match) {
          const reason = match[1] ? match[1].trim() : "Inline suppression comment";
          suppressedFindings.push({
            ...finding,
            suppressions: [
              {
                kind: "inSource",
                status: "accepted",
                justification: reason
              }
            ]
          });
          continue;
        }
      }

      activeFindings.push(finding);
    }

    return {
      activeFindings,
      suppressedFindings
    };
  }
}

module.exports = {
  SuppressionEngine
};
