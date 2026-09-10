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

class WeakHashAlgorithmRuleTest {

    private WeakHashAlgorithmRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new WeakHashAlgorithmRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectMd5AndSha1() {
        String code = """
            import java.security.MessageDigest;
            public class Hasher {
                public void hash() throws Exception {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("Hasher.java"), "Hasher.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(2, findings.size());
        assertTrue(findings.stream().allMatch(f -> f.severity() == Severity.HIGH));
    }

    @Test
    void shouldPassForSha256() {
        String code = """
            import java.security.MessageDigest;
            public class SecureHasher {
                public void hash() throws Exception {
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("SecureHasher.java"), "SecureHasher.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertTrue(findings.isEmpty());
    }
}
