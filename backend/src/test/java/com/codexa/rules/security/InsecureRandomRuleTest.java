package com.codexa.rules.security;

import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsecureRandomRuleTest {

    private InsecureRandomRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new InsecureRandomRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectNewRandomAndMathRandom() {
        String code = """
            import java.util.Random;
            public class TokenGenerator {
                public String generateToken() {
                    Random r = new Random();
                    int val = r.nextInt(10000);
                    double d = Math.random();
                    return "token-" + val + "-" + d;
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("TokenGenerator.java"), "TokenGenerator.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(2, findings.size());
        assertTrue(findings.stream().anyMatch(f -> f.severity() == Severity.HIGH && f.title().contains("java.util.Random")));
        assertTrue(findings.stream().anyMatch(f -> f.severity() == Severity.MEDIUM && f.title().contains("Math.random()")));
    }

    @Test
    void shouldPassWhenUsingSecureRandom() {
        String code = """
            import java.security.SecureRandom;
            public class SafeGenerator {
                public String generateToken() {
                    SecureRandom sr = new SecureRandom();
                    return "token-" + sr.nextInt(10000);
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("SafeGenerator.java"), "SafeGenerator.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertTrue(findings.isEmpty());
    }
}
