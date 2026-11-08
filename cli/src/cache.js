/**
 * Codexa CLI Incremental Scan Cache Engine
 * Uses SHA-256 hashes of file contents to skip unchanged files on repeat audits.
 */

const fs = require("node:fs");
const path = require("node:path");
const crypto = require("node:crypto");

const CACHE_FILE = ".codexacache";

function computeHash(content) {
  return crypto.createHash("sha256").update(content).digest("hex");
}

function loadCache(projectDir) {
  const cachePath = path.join(projectDir, CACHE_FILE);
  if (fs.existsSync(cachePath)) {
    try {
      return JSON.parse(fs.readFileSync(cachePath, "utf8"));
    } catch {
      return {};
    }
  }
  return {};
}

function saveCache(projectDir, cacheData) {
  const cachePath = path.join(projectDir, CACHE_FILE);
  try {
    fs.writeFileSync(cachePath, JSON.stringify(cacheData, null, 2), "utf8");
  } catch {
    // Ignore cache save failure
  }
}

module.exports = {
  CACHE_FILE,
  computeHash,
  loadCache,
  saveCache
};
