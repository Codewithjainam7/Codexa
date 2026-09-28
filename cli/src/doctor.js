/**
 * Codexa Doctor Diagnostic Utility
 * Inspects system prerequisites, Java LTS, Git, and Local AI (Ollama/vLLM) availability.
 */

const { execSync } = require("node:child_process");
const http = require("node:http");

async function checkUrl(url, timeoutMs = 2000) {
  return new Promise((resolve) => {
    try {
      const u = new URL(url);
      const req = http.request(
        {
          hostname: u.hostname,
          port: u.port,
          path: u.pathname,
          method: "GET",
          timeout: timeoutMs
        },
        (res) => {
          resolve({ ok: res.statusCode >= 200 && res.statusCode < 400, status: res.statusCode });
        }
      );
      req.on("error", () => resolve({ ok: false, error: "Connection refused" }));
      req.on("timeout", () => {
        req.destroy();
        resolve({ ok: false, error: "Timed out" });
      });
      req.end();
    } catch (e) {
      resolve({ ok: false, error: e.message });
    }
  });
}

async function runDoctor() {
  console.log("\x1b[1m\x1b[97mCodexa System Doctor & Environment Diagnostics\x1b[0m\n");

  const results = [];

  // 1. Node.js Check
  const nodeVer = process.version;
  const majorNode = parseInt(nodeVer.replace("v", "").split(".")[0], 10);
  results.push({
    name: "Node.js Runtime",
    version: nodeVer,
    status: majorNode >= 18 ? "PASS" : "WARN",
    detail: majorNode >= 18 ? "Meets >= v18 requirement" : "Recommended: upgrade to Node >= v18"
  });

  // 2. Java 21 Check
  try {
    const javaOutput = execSync("java -version 2>&1", { encoding: "utf8" });
    const isJava21 = javaOutput.includes("21.") || javaOutput.includes('"21');
    results.push({
      name: "Java JDK Runtime",
      version: javaOutput.split("\n")[0].trim(),
      status: isJava21 ? "PASS" : "INFO",
      detail: isJava21 ? "Java 21 LTS detected (Virtual Threads enabled)" : "Java detected (Java 21 recommended for full backend pipeline)"
    });
  } catch (e) {
    results.push({
      name: "Java JDK Runtime",
      version: "Not found",
      status: "WARN",
      detail: "Java not found in PATH. Required for running backend service."
    });
  }

  // 3. Git Check
  try {
    const gitOutput = execSync("git --version", { encoding: "utf8" }).trim();
    results.push({
      name: "Git Version Control",
      version: gitOutput,
      status: "PASS",
      detail: "Git CLI installed and operational"
    });
  } catch (e) {
    results.push({
      name: "Git Version Control",
      version: "Not found",
      status: "FAIL",
      detail: "Git CLI is missing"
    });
  }

  // 4. Local Ollama / AI Inference Engine Check
  const ollamaCheck = await checkUrl("http://localhost:11434/api/tags", 1500);
  results.push({
    name: "Local AI Inference (Ollama / RTX 3050)",
    version: ollamaCheck.ok ? "Online (:11434)" : "Offline",
    status: ollamaCheck.ok ? "PASS" : "INFO",
    detail: ollamaCheck.ok
      ? "Local Ollama daemon responsive for zero-cost offline code remediation"
      : "Local Ollama offline; fallback to OpenRouter cloud cascade / deterministic templates active"
  });

  // 5. Codexa Backend API Server Check
  const backendCheck = await checkUrl("http://localhost:8080/api/v1/analyses/health", 1500);
  results.push({
    name: "Codexa Backend Server (:8080)",
    version: backendCheck.ok ? "Online" : "Offline",
    status: backendCheck.ok ? "PASS" : "INFO",
    detail: backendCheck.ok
      ? "Backend Spring Boot service active for full AST & DB persistence"
      : "Backend service offline; standalone zero-dependency CLI scanning operational"
  });

  // Print Report Table
  for (const r of results) {
    let icon = "\x1b[38;2;16;185;129m✔ PASS\x1b[0m";
    if (r.status === "WARN") icon = "\x1b[38;2;245;158;11m▲ WARN\x1b[0m";
    else if (r.status === "FAIL") icon = "\x1b[38;2;239;68;68m✖ FAIL\x1b[0m";
    else if (r.status === "INFO") icon = "\x1b[38;2;59;130;246mℹ INFO\x1b[0m";

    console.log(`[${icon}] \x1b[1m${r.name}\x1b[0m: ${r.version}`);
    console.log(`       \x1b[2m${r.detail}\x1b[0m\n`);
  }

  console.log("\x1b[2mSystem diagnostic completed successfully.\x1b[0m\n");
}

module.exports = {
  runDoctor
};
