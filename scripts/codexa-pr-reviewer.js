#!/usr/bin/env node

/**
 * Codexa GitHub PR Review Bot & Inline Suggestion Reviewer
 * 
 * Analyzes Git PR diffs against Codexa's static security and quality rules,
 * maps findings directly to diff hunk line numbers, and posts inline review comments
 * with 1-click GitHub "Apply Suggestion" blocks.
 * 
 * Zero external dependencies: uses native Node.js and https API.
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const { execSync } = require('child_process');

// Configuration from environment variables
const GITHUB_TOKEN = process.env.GITHUB_TOKEN || process.env.GH_TOKEN;
const GITHUB_REPOSITORY = process.env.GITHUB_REPOSITORY; // e.g. "Codewithjainam7/Codexa"
const PR_NUMBER = process.env.PR_NUMBER || process.env.GITHUB_REF_NAME;
const BASE_REF = process.env.BASE_REF || 'origin/main';
const HEAD_REF = process.env.HEAD_REF || 'HEAD';
const MIN_READINESS_SCORE = parseInt(process.env.MIN_READINESS_SCORE || '85', 10);

// Codexa Rule Catalog for PR Review
const RULES = [
  {
    id: 'CR-SQL-001',
    name: 'SQL Injection in Dynamic Query',
    severity: 'CRITICAL',
    category: 'SECURITY',
    cwe: 'CWE-89',
    penalty: 30,
    test: (line) => /(executeQuery|executeUpdate|createNativeQuery)\s*\(.*(\+.*['"]|['"].*\+)/.test(line) ||
                    /SELECT\s+.*FROM\s+.*WHERE\s+.*\+\s*[a-zA-Z0-9_]+/i.test(line),
    suggest: (line) => {
      if (line.includes('executeQuery') || line.includes('Statement')) {
        return line.replace(/(['"].*)\+\s*([a-zA-Z0-9_]+)(.*['"])/, '$1?$3')
                   .replace(/Statement\s+stmt\s*=/, 'PreparedStatement stmt =')
                   .replace(/createStatement\(\)/, 'prepareStatement(sql)');
      }
      return null;
    },
    message: 'Potential SQL Injection: detected dynamic string concatenation in executable query statement.',
    remediation: 'Use PreparedStatement query parameter placeholders (?) rather than raw string concatenation.'
  },
  {
    id: 'CR-CMD-001',
    name: 'OS Command Injection',
    severity: 'CRITICAL',
    category: 'SECURITY',
    cwe: 'CWE-78',
    penalty: 30,
    test: (line) => /Runtime\.getRuntime\(\)\.exec\s*\(/.test(line) ||
                    /child_process\.(exec|execSync)\s*\(.*(\+.*['"]|['"].*\+)/.test(line) ||
                    /subprocess\.(call|Popen|run)\s*\(.*shell\s*=\s*True/.test(line),
    suggest: (line) => {
      if (line.includes('Runtime.getRuntime().exec')) {
        return line.replace(/Runtime\.getRuntime\(\)\.exec\s*\((.*)\)/, 'new ProcessBuilder(List.of($1)).start()');
      }
      return null;
    },
    message: 'OS Command Injection: dynamic parameter passed to system command execution without array-based argument separation.',
    remediation: 'Replace string shell execution with ProcessBuilder(List.of(...)) or child_process.execFile() to avoid shell interpolation.'
  },
  {
    id: 'CR-SECRET-001',
    name: 'Hardcoded Secret or API Token',
    severity: 'CRITICAL',
    category: 'SECURITY',
    cwe: 'CWE-798',
    penalty: 25,
    test: (line) => /(AKIA[0-9A-Z]{16})/.test(line) ||
                    /(ghp_[a-zA-Z0-9]{36})/.test(line) ||
                    /(sk-or-v1-[a-f0-9]{64})/.test(line) ||
                    /(api_key|apiKey|secret_key|private_key|auth_token)\s*=\s*['\"][A-Za-z0-9_\-]{20,}['\"]/i.test(line),
    suggest: (line) => {
      return line.replace(/=\s*['\"][A-Za-z0-9_\-]+['\"]/, '= System.getenv("API_KEY")');
    },
    message: 'Hardcoded Secret Detected: found high-entropy credential or known cloud provider token literal in source code.',
    remediation: 'Extract secret into environment variable (e.g. System.getenv("SECRET_NAME")) or external secrets vault.'
  },
  {
    id: 'CR-PATH-001',
    name: 'Path Traversal Vulnerability',
    severity: 'HIGH',
    category: 'SECURITY',
    cwe: 'CWE-22',
    penalty: 15,
    test: (line) => /new\s+File\s*\([^,]+,\s*[a-zA-Z0-9_]+\)/.test(line) && !line.includes('normalize'),
    suggest: (line) => {
      return line + '\n    if (!file.toPath().normalize().startsWith(baseDir.toPath().normalize())) { throw new SecurityException("Zip Slip path traversal"); }';
    },
    message: 'Path Traversal Risk: file path constructed from user-controlled variable without canonical path verification.',
    remediation: 'Verify file.toPath().normalize().startsWith(baseDir) to prevent directory traversal outside sandbox.'
  },
  {
    id: 'CR-HASH-001',
    name: 'Broken Cryptographic Hash (MD5 / SHA-1)',
    severity: 'HIGH',
    category: 'SECURITY',
    cwe: 'CWE-328',
    penalty: 12,
    test: (line) => /MessageDigest\.getInstance\s*\(\s*["'](MD5|SHA-1|SHA1)["']\s*\)/i.test(line) ||
                    /hashlib\.(md5|sha1)\s*\(/.test(line) ||
                    /crypto\.createHash\s*\(\s*["'](md5|sha1)["']\s*\)/i.test(line),
    suggest: (line) => {
      return line.replace(/["'](MD5|SHA-1|SHA1)["']/i, '"SHA-256"');
    },
    message: 'Weak Hash Algorithm: MD5 and SHA-1 suffer from practical collision attacks and are cryptographically broken.',
    remediation: 'Migrate to collision-resistant SHA-256 or adaptive password hash Argon2id (NIST SP 800-131A).'
  },
  {
    id: 'CR-RAND-001',
    name: 'Insecure Pseudorandom Number Generator',
    severity: 'MEDIUM',
    category: 'SECURITY',
    cwe: 'CWE-338',
    penalty: 8,
    test: (line) => /new\s+Random\s*\(/.test(line) && (line.includes('token') || line.includes('salt') || line.includes('key')),
    suggest: (line) => {
      return line.replace(/new\s+Random\s*\(\)/, 'new java.security.SecureRandom()');
    },
    message: 'Insecure PRNG: java.util.Random is predictable and unsuitable for security token or cryptographic key generation.',
    remediation: 'Replace with java.security.SecureRandom() for cryptographically secure pseudo-random values.'
  },
  {
    id: 'CR-QUAL-006',
    name: 'Swallowed Exception in Empty Catch Block',
    severity: 'MEDIUM',
    category: 'QUALITY',
    cwe: 'CWE-390',
    penalty: 6,
    test: (line) => /catch\s*\([A-Za-z0-9_]+\s+[A-Za-z0-9_]+\)\s*\{\s*\}/.test(line),
    suggest: (line) => {
      return line.replace(/\{\s*\}/, '{ log.warn("Operation failed: {}", e.getMessage()); }');
    },
    message: 'Swallowed Exception: empty catch block hides runtime failures and impedes operational observability.',
    remediation: 'Log the caught exception or rethrow a domain-specific ApiException.'
  }
];

/**
 * Extracts changed files and diff line mappings from git diff.
 */
function getGitDiff() {
  try {
    const diffOutput = execSync(`git diff -U0 ${BASE_REF}...${HEAD_REF}`, { encoding: 'utf8', maxBuffer: 10 * 1024 * 1024 });
    return diffOutput;
  } catch (err) {
    console.warn(`Could not run git diff against ${BASE_REF}:`, err.message);
    // Fall back to diff against HEAD~1
    return execSync('git diff -U0 HEAD~1...HEAD', { encoding: 'utf8', maxBuffer: 10 * 1024 * 1024 });
  }
}

/**
 * Parses unified git diff into structured file hunks with line numbers.
 */
function parseDiff(diffText) {
  const files = [];
  let currentFile = null;

  const lines = diffText.split('\n');
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (line.startsWith('diff --git ')) {
      const match = line.match(/diff --git a\/(.*) b\/(.*)/);
      if (match) {
        currentFile = {
          path: match[2],
          hunks: []
        };
        files.push(currentFile);
      }
    } else if (line.startsWith('@@ ') && currentFile) {
      // e.g. @@ -12,0 +13,4 @@
      const match = line.match(/@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@/);
      if (match) {
        const startLine = parseInt(match[1], 10);
        const lineCount = match[2] ? parseInt(match[2], 10) : 1;
        const hunk = {
          startLine,
          lineCount,
          lines: []
        };
        currentFile.hunks.push(hunk);
      }
    } else if (currentFile && currentFile.hunks.length > 0) {
      const currentHunk = currentFile.hunks[currentFile.hunks.length - 1];
      if (line.startsWith('+') && !line.startsWith('+++')) {
        const addedLine = line.substring(1);
        const currentLineNumber = currentHunk.startLine + currentHunk.lines.filter(l => l.type === 'add').length;
        currentHunk.lines.push({
          type: 'add',
          lineNumber: currentLineNumber,
          content: addedLine
        });
      }
    }
  }

  return files;
}

/**
 * Evaluates diff lines against Codexa rules.
 */
function auditDiff(files) {
  const findings = [];
  let totalDeductions = 0;

  for (const file of files) {
    // Skip test files, markdown, and documentation
    if (file.path.endsWith('.md') || file.path.includes('docs/') || file.path.includes('test/')) {
      continue;
    }

    for (const hunk of file.hunks) {
      for (const lineObj of hunk.lines) {
        if (lineObj.type !== 'add') continue;

        const content = lineObj.content;
        for (const rule of RULES) {
          if (rule.test(content)) {
            const suggestion = rule.suggest ? rule.suggest(content) : null;
            findings.push({
              ruleId: rule.id,
              ruleName: rule.name,
              category: rule.category,
              severity: rule.severity,
              cwe: rule.cwe,
              penalty: rule.penalty,
              filePath: file.path,
              lineNumber: lineObj.lineNumber,
              lineContent: content,
              message: rule.message,
              remediation: rule.remediation,
              suggestion
            });
            totalDeductions += rule.penalty;
          }
        }
      }
    }
  }

  const rawScore = 100 - totalDeductions;
  const readinessScore = Math.max(0, Math.min(100, rawScore));

  return {
    findings,
    readinessScore,
    verdict: readinessScore >= MIN_READINESS_SCORE && !findings.some(f => f.severity === 'CRITICAL') ? 'PRODUCTION_READY' : 'GATE_FAILED'
  };
}

/**
 * Builds GitHub Pull Request Review Payload with inline suggestion comments.
 */
function buildReviewPayload(auditResult, commitId) {
  const comments = [];

  for (const finding of auditResult.findings) {
    let commentBody = `### 🛡️ Codexa Security Alert: [${finding.ruleId}] ${finding.ruleName}\n\n`;
    commentBody += `**Severity:** \`${finding.severity}\` | **Taxonomy:** \`${finding.cwe}\` | **Category:** \`${finding.category}\`\n\n`;
    commentBody += `> ${finding.message}\n\n`;
    commentBody += `**Remediation Advice:** ${finding.remediation}\n\n`;

    if (finding.suggestion && finding.suggestion !== finding.lineContent) {
      commentBody += `#### 💡 Suggested 1-Click Fix:\n`;
      commentBody += `\`\`\`suggestion\n${finding.suggestion}\n\`\`\`\n`;
    }

    comments.push({
      path: finding.filePath,
      line: finding.lineNumber,
      body: commentBody
    });
  }

  // Header Summary Badge
  const verdictEmoji = auditResult.verdict === 'PRODUCTION_READY' ? '✅' : '🚫';
  let summary = `## ${verdictEmoji} Codexa Security & Quality PR Audit\n\n`;
  summary += `| Metric | Result | Status |\n`;
  summary += `|:---|:---:|:---:|\n`;
  summary += `| **Production Readiness Score** | **${auditResult.readinessScore} / 100** | ${auditResult.readinessScore >= MIN_READINESS_SCORE ? '🟢 PASSED' : '🔴 FAILED'} |\n`;
  summary += `| **Total Violations Detected** | **${auditResult.findings.length}** | ${auditResult.findings.length === 0 ? '🟢 CLEAN' : '⚠️ ACTION REQUIRED'} |\n`;
  summary += `| **Critical Security Blockers** | **${auditResult.findings.filter(f => f.severity === 'CRITICAL').length}** | ${auditResult.findings.filter(f => f.severity === 'CRITICAL').length === 0 ? '🟢 ZERO' : '🚨 BLOCKED'} |\n`;
  summary += `| **Audit Verdict** | \`${auditResult.verdict}\` | ${auditResult.verdict === 'PRODUCTION_READY' ? '🟢 APPROVED' : '🛑 CHANGES REQUIRED'} |\n\n`;

  if (auditResult.findings.length > 0) {
    summary += `### 🔍 Finding Breakdown\n`;
    for (const f of auditResult.findings) {
      summary += `- **[${f.ruleId}]** \`${f.severity}\` in \`${f.filePath}:${f.lineNumber}\`: ${f.message}\n`;
    }
  } else {
    summary += `> 🌟 **Clean PR**: Zero security vulnerabilities or quality defects detected in modified diff hunks. Ready for merge!`;
  }

  summary += `\n\n---\n*Automated review by [Codexa Security Auditor](https://github.com/Codewithjainam7/Codexa)*`;

  return {
    commit_id: commitId,
    body: summary,
    event: auditResult.verdict === 'PRODUCTION_READY' ? 'COMMENT' : 'REQUEST_CHANGES',
    comments
  };
}

/**
 * Main execution entrypoint.
 */
function main() {
  console.log('🛡️  Starting Codexa GitHub PR Review Bot...');
  console.log(`Base Ref: ${BASE_REF}, Head Ref: ${HEAD_REF}`);

  const diffText = getGitDiff();
  if (!diffText || diffText.trim().length === 0) {
    console.log('✅ No diff detected between target branch and HEAD. PR is clean.');
    process.exit(0);
  }

  const parsedFiles = parseDiff(diffText);
  console.log(`Inspected ${parsedFiles.length} changed files across pull request.`);

  const auditResult = auditDiff(parsedFiles);
  console.log(`Audit complete: Readiness Score = ${auditResult.readinessScore}/100, Verdict = ${auditResult.verdict}, Findings = ${auditResult.findings.length}`);

  let commitId = 'HEAD';
  try {
    commitId = execSync('git rev-parse HEAD', { encoding: 'utf8' }).trim();
  } catch (e) {
    // fallback
  }

  const payload = buildReviewPayload(auditResult, commitId);

  // Write payload to JSON artifact for GitHub Actions step consumption
  fs.writeFileSync('codexa-pr-review-payload.json', JSON.stringify(payload, null, 2), 'utf8');
  console.log('Wrote review payload to codexa-pr-review-payload.json');

  if (auditResult.verdict !== 'PRODUCTION_READY') {
    console.error(`🚨 Codexa Gating Failed: Score ${auditResult.readinessScore} < ${MIN_READINESS_SCORE} or Critical blockers found.`);
    process.exit(1);
  } else {
    console.log('✅ Codexa Gating Passed: PR meets production readiness standards.');
    process.exit(0);
  }
}

if (require.main === module) {
  main();
}

module.exports = { parseDiff, auditDiff, buildReviewPayload, RULES };
