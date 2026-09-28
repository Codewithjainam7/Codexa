/**
 * Test Suite for Codexa PR Reviewer Engine
 */

const assert = require('assert');
const { parseDiff, auditDiff, buildReviewPayload } = require('./codexa-pr-reviewer');

console.log('🧪 Running Codexa PR Reviewer Unit Tests...');

// Test 1: Unified diff parsing
const sampleDiff = `diff --git a/src/main/UserDao.java b/src/main/UserDao.java
index e69de29..d95f3ad 100644
--- a/src/main/UserDao.java
+++ b/src/main/UserDao.java
@@ -10,0 +11,3 @@
+    public void findUser(String id) {
+        String sql = "SELECT * FROM users WHERE id = '" + id + "'";
+        stmt.executeQuery(sql);
@@ -25,0 +29,2 @@
+    String apiKey = "AKIA1234567890EXAMPLE";
+    MessageDigest md = MessageDigest.getInstance("MD5");
`;

const parsed = parseDiff(sampleDiff);
assert.strictEqual(parsed.length, 1, 'Should parse 1 file');
assert.strictEqual(parsed[0].path, 'src/main/UserDao.java');
assert.strictEqual(parsed[0].hunks.length, 2, 'Should parse 2 hunks');
console.log('✅ Test 1 Passed: Unified diff successfully parsed into file hunks.');

// Test 2: Static rule audit
const audit = auditDiff(parsed);
assert.ok(audit.findings.length >= 3, `Expected at least 3 findings, got ${audit.findings.length}`);

const ruleIds = audit.findings.map(f => f.ruleId);
assert.ok(ruleIds.includes('CR-SQL-001'), 'Should detect SQL injection');
assert.ok(ruleIds.includes('CR-SECRET-001'), 'Should detect hardcoded AWS key');
assert.ok(ruleIds.includes('CR-HASH-001'), 'Should detect MD5 hash algorithm');

assert.strictEqual(audit.verdict, 'GATE_FAILED', 'Verdict should be GATE_FAILED due to CRITICAL findings');
console.log(`✅ Test 2 Passed: Detected ${audit.findings.length} findings with exact rule mappings.`);

// Test 3: GitHub Review Payload generation with inline suggestions
const payload = buildReviewPayload(audit, 'abc123commit');
assert.strictEqual(payload.commit_id, 'abc123commit');
assert.strictEqual(payload.event, 'REQUEST_CHANGES');
assert.ok(payload.body.includes('Codexa Security & Quality PR Audit'));
assert.ok(payload.comments.length >= 3);

// Verify inline suggestion format
const sqlFinding = payload.comments.find(c => c.body.includes('CR-SQL-001'));
assert.ok(sqlFinding, 'Should have comment for SQL injection');
assert.ok(sqlFinding.line >= 11, 'Line number should match hunk offset');

console.log('✅ Test 3 Passed: Review payload and inline suggestions formatted successfully.');
console.log('🎉 All 3 PR Reviewer Unit Tests Passed Successfully!');
