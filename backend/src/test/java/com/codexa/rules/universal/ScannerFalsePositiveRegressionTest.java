package com.codexa.rules.universal;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.ingestion.service.FileFilterService;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.rules.security.MissingAccessControlRule;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScannerFalsePositiveRegressionTest {

    private FileFilterService fileFilterService;
    private UniversalMultiLanguageRule universalRule;
    private MissingAccessControlRule accessControlRule;
    private JavaAstParserService astParserService;

    @BeforeEach
    void setUp() {
        fileFilterService = new FileFilterService();
        universalRule = new UniversalMultiLanguageRule();
        accessControlRule = new MissingAccessControlRule();
        astParserService = new JavaAstParserService();
    }

    @Test
    void testFileFilterServiceExcludesTestFixturesAndStaticAssets() {
        // Fixtures
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("fixtures/codexa-demo-vulnerable/src")));
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("repo/fixtures/sample")));
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("test-fixtures/demo")));

        // Unit test directories
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("backend/src/test/java/com/codexa")));
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("frontend/__tests__/components")));
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("tests/unit")));

        // Static compiled assets
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("backend/src/main/resources/static/assets")));
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("static/assets")));

        // Docs
        assertTrue(fileFilterService.isIgnoredDirectory(Paths.get("docs/rules")));

        // Production source must NOT be ignored
        assertFalse(fileFilterService.isIgnoredDirectory(Paths.get("backend/src/main/java/com/codexa")));
        assertFalse(fileFilterService.isIgnoredDirectory(Paths.get("frontend/src/components")));
    }

    @Test
    void testFallbackSecretPrecision() {
        // Safe endpoint URLs & environment modes must NOT be flagged as fallback secrets
        assertFalse(UniversalMultiLanguageRule.isFallbackSecret("const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';"));
        assertFalse(UniversalMultiLanguageRule.isFallbackSecret("const url = process.env.API_ENDPOINT || 'http://localhost:8080/api';"));
        assertFalse(UniversalMultiLanguageRule.isFallbackSecret("const mode = process.env.NODE_ENV || 'development';"));
        assertFalse(UniversalMultiLanguageRule.isFallbackSecret("const host = process.env.API_HOST || 'https://api.example.com';"));

        // Genuine fallback secrets MUST be flagged
        assertTrue(UniversalMultiLanguageRule.isFallbackSecret("const secret = process.env.JWT_SECRET || 'super_secret_production_key_123';"));
        assertTrue(UniversalMultiLanguageRule.isFallbackSecret("const key = process.env.API_KEY || 'sk_live_1234567890abcdef';"));
        assertTrue(UniversalMultiLanguageRule.isFallbackSecret("const token = process.env.AUTH_TOKEN || 'auth_token_value_xyz';"));
    }

    @Test
    void testMarkdownDocumentationDoesNotTriggerCodeRules(@TempDir Path tempDir) throws IOException {
        Path docsDir = tempDir.resolve("docs");
        Files.createDirectories(docsDir);
        Path docFile = docsDir.resolve("RULES_CATALOG.md");
        Files.writeString(docFile, """
                # Rule Catalog
                ## CR-RLS-001: Row Level Security
                Vulnerable example:
                ```sql
                ALTER TABLE users ENABLE ROW LEVEL SECURITY;
                CREATE POLICY "Allow all" ON users FOR ALL USING (true);
                ```
                ## CR-CMD-001: Command Injection
                Vulnerable example:
                ```javascript
                eval("alert(1)");
                child_process.exec(cmd);
                ```
                ## CR-RAND-002: Predictable Token
                ```javascript
                const token = `tok_${Date.now()}`;
                ```
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = universalRule.evaluate(ctx);

        assertEquals(0, findings.size(), "Markdown documentation files must produce zero code vulnerability findings");
    }

    @Test
    void testMissingAccessControlDoesNotFlagPublicEndpoints() {
        String rootControllerCode = """
                package com.codexa.analysis.controller;
                import org.springframework.stereotype.Controller;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.ResponseBody;
                import org.springframework.http.ResponseEntity;
                import java.util.Map;

                @Controller
                public class RootController {
                    @GetMapping("/")
                    public String index() {
                        return "forward:/index.html";
                    }

                    @GetMapping("/api")
                    @ResponseBody
                    public ResponseEntity<Map<String, Object>> getApiInfo() {
                        return ResponseEntity.ok(Map.of("status", "UP", "configLimits", "/api/v1/config/limits"));
                    }
                }
                """;

        ParsedJavaFile parsed = astParserService.parseContent(rootControllerCode, Paths.get("RootController.java"), "RootController.java");
        RuleContext ctx = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));
        List<RuleFinding> findings = accessControlRule.evaluate(ctx);

        assertEquals(0, findings.size(), "RootController public discovery endpoints must not be flagged for missing authorization");
    }

    @Test
    void testMissingAccessControlDoesNotFlagInternalServerErrorCatch() {
        String zipControllerCode = """
                package com.codexa.ingestion.controller;
                import org.springframework.web.bind.annotation.*;
                import org.springframework.http.ResponseEntity;
                import org.springframework.http.HttpStatus;

                @RestController
                @RequestMapping("/api/v1/analyses/zip")
                public class ZipAnalysisController {
                    @PostMapping
                    public ResponseEntity<?> submitZipAnalysis() {
                        try {
                            return ResponseEntity.accepted().build();
                        } catch (Exception e) {
                            throw new RuntimeException("Internal error: " + HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
                """;

        ParsedJavaFile parsed = astParserService.parseContent(zipControllerCode, Paths.get("ZipAnalysisController.java"), "ZipAnalysisController.java");
        RuleContext ctx = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));
        List<RuleFinding> findings = accessControlRule.evaluate(ctx);

        assertEquals(0, findings.size(), "Ingestion controller mentioning INTERNAL_SERVER_ERROR must not be flagged for missing authorization");
    }

    @Test
    void testMissingAccessControlFlagsGenuineAdminEndpoint() {
        String adminCode = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/admin")
                public class AdminManagementController {
                    @PostMapping("/deleteUser")
                    public String deleteUser(@RequestParam String id) {
                        return "user deleted";
                    }
                }
                """;

        ParsedJavaFile parsed = astParserService.parseContent(adminCode, Paths.get("AdminManagementController.java"), "AdminManagementController.java");
        RuleContext ctx = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));
        List<RuleFinding> findings = accessControlRule.evaluate(ctx);

        assertEquals(1, findings.size(), "Unauthenticated administrative /api/admin/deleteUser endpoint must be flagged");
        assertEquals("CR-AUTH-001", findings.get(0).ruleId());
    }

    @Test
    void testScoringCalibrationForProductionCleanSecurityRepo() {
        com.codexa.scoring.readiness.ReadinessScoringEngine engine = new com.codexa.scoring.readiness.ReadinessScoringEngine();
        java.util.List<com.codexa.persistence.entity.FindingEntity> findings = new java.util.ArrayList<>();

        // Add 15 moderate quality findings (complexity, long methods, defensive patterns)
        for (int i = 0; i < 15; i++) {
            com.codexa.persistence.entity.FindingEntity q = new com.codexa.persistence.entity.FindingEntity();
            q.setRuleId("CR-QUAL-001");
            q.setCategory(com.codexa.analysis.model.Category.QUALITY);
            q.setSeverity(com.codexa.analysis.model.Severity.LOW);
            findings.add(q);
        }

        var result = engine.computeScores(findings);
        assertTrue(result.overallScore() >= 85.0, "Production repository with clean security must score >= 85.0, got: " + result.overallScore());
        assertEquals(100.0, result.securityScore(), "Security score must be 100.0 when 0 security vulnerabilities exist");
        assertEquals(com.codexa.analysis.model.ProductionVerdict.REVIEW_COMPLETE, result.verdict(), "Verdict must be REVIEW_COMPLETE");
    }

    @Test
    void testScoringCalibrationForCriticalVulnerableRepo() {
        com.codexa.scoring.readiness.ReadinessScoringEngine engine = new com.codexa.scoring.readiness.ReadinessScoringEngine();
        java.util.List<com.codexa.persistence.entity.FindingEntity> findings = new java.util.ArrayList<>();

        // Add CRITICAL security findings matching vulnerable repository profile (SQLi, RCE, Secrets, Traversal)
        com.codexa.persistence.entity.FindingEntity sql = new com.codexa.persistence.entity.FindingEntity();
        sql.setRuleId("CR-SQL-001");
        sql.setCategory(com.codexa.analysis.model.Category.SECURITY);
        sql.setSeverity(com.codexa.analysis.model.Severity.CRITICAL);
        findings.add(sql);

        com.codexa.persistence.entity.FindingEntity cmd = new com.codexa.persistence.entity.FindingEntity();
        cmd.setRuleId("CR-CMD-001");
        cmd.setCategory(com.codexa.analysis.model.Category.SECURITY);
        cmd.setSeverity(com.codexa.analysis.model.Severity.CRITICAL);
        findings.add(cmd);

        com.codexa.persistence.entity.FindingEntity sec = new com.codexa.persistence.entity.FindingEntity();
        sec.setRuleId("CR-SEC-001");
        sec.setCategory(com.codexa.analysis.model.Category.SECURITY);
        sec.setSeverity(com.codexa.analysis.model.Severity.CRITICAL);
        findings.add(sec);

        com.codexa.persistence.entity.FindingEntity path = new com.codexa.persistence.entity.FindingEntity();
        path.setRuleId("CR-PATH-001");
        path.setCategory(com.codexa.analysis.model.Category.SECURITY);
        path.setSeverity(com.codexa.analysis.model.Severity.CRITICAL);
        findings.add(path);

        // Add operations penalty (missing rate limit, security headers, unmonitored endpoints)
        for (int i = 0; i < 8; i++) {
            com.codexa.persistence.entity.FindingEntity op = new com.codexa.persistence.entity.FindingEntity();
            op.setRuleId("CR-OPS-001");
            op.setCategory(com.codexa.analysis.model.Category.OPERATIONS);
            op.setSeverity(com.codexa.analysis.model.Severity.MEDIUM);
            findings.add(op);
        }

        var result = engine.computeScores(findings);
        assertTrue(result.overallScore() <= 15.0, "Vulnerable repository with critical flaws must score <= 15.0, got: " + result.overallScore());
        assertEquals(com.codexa.analysis.model.ProductionVerdict.NOT_READY, result.verdict(), "Verdict must be NOT_READY");
    }
}
