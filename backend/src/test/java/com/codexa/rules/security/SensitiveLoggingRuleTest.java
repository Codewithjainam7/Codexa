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

class SensitiveLoggingRuleTest {

    private SensitiveLoggingRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new SensitiveLoggingRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateSensitiveLogShouldFlagFinding() {
        String code = """
                package com.example;
                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;
                public class AuthService {
                    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
                    public void login(String user, String password) {
                        log.info("Login attempt for user: {} with password: {}", user, password);
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("AuthService.java"), "AuthService.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-LOG-001", findings.get(0).ruleId());
    }
}
