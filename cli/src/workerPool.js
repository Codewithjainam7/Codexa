const os = require("os");
const path = require("path");
const { scanFile } = require("./scanner");

/**
 * Concurrent Worker Pool Scanner for high-performance monorepo analysis.
 * Distributes file evaluation across concurrent asynchronous worker lanes.
 */
class WorkerPoolScanner {
  constructor(concurrency = os.cpus().length || 4, options = {}) {
    this.concurrency = Math.max(1, concurrency);
    this.ignoredPatterns = options.ignoredPatterns || [
      "node_modules", ".git", "target", "dist", "build", ".next", ".idea", ".vscode"
    ];
  }

  isIgnored(filePath) {
    const normalized = filePath.replace(/\\/g, "/");
    return this.ignoredPatterns.some(p => normalized.includes(`/${p}/`) || normalized.startsWith(`${p}/`));
  }

  /**
   * Scans an array of file paths concurrently using an in-process worker pool.
   *
   * @param {Array<string>} files Array of file paths to scan
   * @param {Function} onProgress Optional callback (scannedCount, totalCount)
   * @returns {Promise<Object>} Aggregated findings and telemetry metrics
   */
  async scanBatch(files, onProgress = null) {
    const startTime = Date.now();
    const findings = [];
    let linesScanned = 0;
    let completed = 0;

    // Filter out ignored files and directories
    const activeFiles = files.filter(f => !this.isIgnored(f));
    const queue = [...activeFiles];
    const total = activeFiles.length;

    const worker = async () => {
      while (queue.length > 0) {
        const file = queue.shift();
        if (!file) break;

        try {
          const res = scanFile(file, path.dirname(file));
          if (res && res.findings) {
            findings.push(...res.findings);
            linesScanned += res.lineCount || 0;
          }
        } catch {
          // Keep resilient on unreadable file
        }

        completed++;
        if (onProgress) {
          onProgress(completed, total);
        }
      }
    };

    // Spin up concurrent worker lanes
    const workerPromises = [];
    const poolSize = Math.min(this.concurrency, Math.max(1, files.length));

    for (let i = 0; i < poolSize; i++) {
      workerPromises.push(worker());
    }

    await Promise.all(workerPromises);

    return {
      filesScanned: completed,
      linesScanned,
      findings,
      durationMs: Date.now() - startTime,
      concurrency: poolSize
    };
  }
}

module.exports = {
  WorkerPoolScanner
};
