const { execSync } = require("child_process");
const path = require("path");

/**
 * Git Blame & Vulnerability Age Attribution Engine
 * Resolves author, commit SHA, and vulnerability age in days for code lines.
 */
class BlameEngine {
  constructor(projectDir = process.cwd()) {
    this.projectDir = projectDir;
    this.blameCache = new Map();
  }

  /**
   * Annotates findings with git blame provenance information.
   *
   * @param {Array} findings List of findings from scanner
   * @returns {Array} Findings enriched with blame metadata
   */
  annotateFindings(findings) {
    if (!Array.isArray(findings) || findings.length === 0) {
      return findings;
    }

    return findings.map((finding) => {
      if (!finding.file || !finding.line) {
        return finding;
      }

      const blameInfo = this.getLineBlame(finding.file, finding.line);
      if (blameInfo) {
        return {
          ...finding,
          provenance: {
            author: blameInfo.author,
            email: blameInfo.email,
            commit: blameInfo.commit,
            date: blameInfo.date,
            ageDays: blameInfo.ageDays,
            summary: blameInfo.summary
          }
        };
      }
      return finding;
    });
  }

  /**
   * Retrieves Git blame line metadata.
   */
  getLineBlame(filePath, lineNumber) {
    const cacheKey = `${filePath}:${lineNumber}`;
    if (this.blameCache.has(cacheKey)) {
      return this.blameCache.get(cacheKey);
    }

    try {
      const fullPath = path.resolve(this.projectDir, filePath);
      const output = execSync(
        `git blame -L ${lineNumber},${lineNumber} --porcelain -- "${fullPath}"`,
        { cwd: this.projectDir, stdio: ["ignore", "pipe", "ignore"], encoding: "utf-8" }
      );

      const lines = output.split("\n");
      const commitSha = lines[0].split(" ")[0];
      let author = "Unknown";
      let email = "";
      let authorTime = 0;
      let summary = "";

      for (const line of lines) {
        if (line.startsWith("author ")) {
          author = line.substring(7).trim();
        } else if (line.startsWith("author-mail ")) {
          email = line.substring(12).replace(/[<>]/g, "").trim();
        } else if (line.startsWith("author-time ")) {
          authorTime = parseInt(line.substring(12).trim(), 10) * 1000;
        } else if (line.startsWith("summary ")) {
          summary = line.substring(8).trim();
        }
      }

      const now = Date.now();
      const ageDays = authorTime > 0 ? Math.floor((now - authorTime) / (1000 * 60 * 60 * 24)) : 0;
      const date = authorTime > 0 ? new Date(authorTime).toISOString() : new Date().toISOString();

      const result = {
        commit: commitSha.substring(0, 8),
        author,
        email,
        date,
        ageDays,
        summary
      };

      this.blameCache.set(cacheKey, result);
      return result;
    } catch {
      // In non-git directory or uncommitted new file, return fallback
      const fallback = {
        commit: "uncommitted",
        author: "Working Tree",
        email: "",
        date: new Date().toISOString(),
        ageDays: 0,
        summary: "Local unstaged modifications"
      };
      this.blameCache.set(cacheKey, fallback);
      return fallback;
    }
  }
}

module.exports = {
  BlameEngine
};
