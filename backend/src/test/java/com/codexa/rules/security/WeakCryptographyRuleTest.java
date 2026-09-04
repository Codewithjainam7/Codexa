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

class WeakCryptographyRuleTest {

    private WeakCryptographyRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new WeakCryptographyRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateEcbModeCipherShouldFlagFinding() {
        String code = """
                package com.example;
                import javax.crypto.Cipher;
                public class Encryptor {
                    public void init() throws Exception {
                        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("Encryptor.java"), "Encryptor.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-CRYPTO-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("ECB"));
    }

    @Test
    void evaluateInsecureRandomForTokenShouldFlagFinding() {
        String code = """
                package com.example;
                import java.util.Random;
                public class TokenService {
                    public int generateOtp() {
                        Random tokenRandom = new Random();
                        return tokenRandom.nextInt(999999);
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("TokenService.java"), "TokenService.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-CRYPTO-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Insecure PRNG"));
    }
}
