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

class HardcodedSecretsRuleTest {

    private HardcodedSecretsRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new HardcodedSecretsRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateHardcodedApiKeyShouldFlagAndMaskEvidence() {
        String code = """
                package com.example;
                public class Config {
                    private static final String API_KEY = "sk-live-9382104928104821";
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("Config.java"), "Config.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        RuleFinding finding = findings.get(0);
        assertEquals("CR-SEC-001", finding.ruleId());
        // Verify masking
        assertTrue(finding.evidence().contains("sk**********************"));
        assertFalse(finding.evidence().contains("sk-live-9382104928104821"));
    }

    @Test
    void evaluatePlaceholderApiKeyShouldNotFlagFinding() {
        String code = """
                package com.example;
                public class Config {
                    private static final String API_KEY = "your_api_key_placeholder";
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("Config.java"), "Config.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.isEmpty());
    }
}
