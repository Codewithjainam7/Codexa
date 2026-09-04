package com.codexa.rules.security;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MissingAccessControlRuleTest {

    private MissingAccessControlRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new MissingAccessControlRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateSensitiveEndpointWithoutAuthShouldFlagFinding() {
        String code = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api")
                public class AdminController {

                    @PostMapping("/admin/deleteUser")
                    public String deleteUser(@RequestParam String id) {
                        return "deleted";
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("AdminController.java"), "AdminController.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-AUTH-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).requiresManualReview());
    }

    @Test
    void evaluateEndpointWithPreAuthorizeShouldNotFlagFinding() {
        String code = """
                package com.example;
                import org.springframework.web.bind.annotation.*;
                import org.springframework.security.access.prepost.PreAuthorize;

                @RestController
                public class AdminController {

                    @PreAuthorize("hasRole('ADMIN')")
                    @PostMapping("/admin/deleteUser")
                    public String deleteUser(@RequestParam String id) {
                        return "deleted";
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("AdminController.java"), "AdminController.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.isEmpty());
    }
}
