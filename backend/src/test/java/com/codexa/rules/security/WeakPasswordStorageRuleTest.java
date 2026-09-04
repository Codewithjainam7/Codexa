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

class WeakPasswordStorageRuleTest {

    private WeakPasswordStorageRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new WeakPasswordStorageRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateMd5HashShouldFlagFinding() {
        String code = """
                package com.example;
                import java.security.MessageDigest;
                public class PasswordHasher {
                    public byte[] hash(String pwd) throws Exception {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        return md.digest(pwd.getBytes());
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("PasswordHasher.java"), "PasswordHasher.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-PASS-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("MD5"));
    }
}
