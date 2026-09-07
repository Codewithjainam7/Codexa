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

class CsrfProtectionRuleTest {

    private CsrfProtectionRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new CsrfProtectionRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateMutatingGetEndpointShouldFlagFinding() {
        String code = """
                package com.example;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;
                @RestController
                public class UserController {
                    private UserRepository repo;
                    @GetMapping("/users/delete")
                    public void deleteUser(String id) {
                        repo.deleteById(id);
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("UserController.java"), "UserController.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-SEC-006", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("State Mutation in Safe HTTP GET Endpoint"));
    }
}
