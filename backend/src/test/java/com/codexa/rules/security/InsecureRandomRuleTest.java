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

        ParsedJavaFile parsed = parserService.parseContent(Path.of("TokenGenerator.java"), code);
        RuleContext ctx = RuleContext.builder().parsedJavaFile(parsed).build();
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(2, findings.size());
        assertTrue(findings.stream().anyMatch(f -> f.getSeverity() == Severity.HIGH && f.getTitle().contains("java.util.Random")));
        assertTrue(findings.stream().anyMatch(f -> f.getSeverity() == Severity.MEDIUM && f.getTitle().contains("Math.random()")));
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

        ParsedJavaFile parsed = parserService.parseContent(Path.of("SafeGenerator.java"), code);
        RuleContext ctx = RuleContext.builder().parsedJavaFile(parsed).build();
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertTrue(findings.isEmpty());
    }
}
