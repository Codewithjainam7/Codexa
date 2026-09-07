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

class HardcodedIpAddressRuleTest {

    private HardcodedIpAddressRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new HardcodedIpAddressRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectHardcodedPrivateIp() {
        String code = """
            public class NetworkConfig {
                private String internalDb = "192.168.1.55";
                private String gateway = "10.0.0.1";
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(Path.of("NetworkConfig.java"), code);
        RuleContext ctx = RuleContext.builder().parsedJavaFile(parsed).build();
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(2, findings.size());
        assertTrue(findings.stream().allMatch(f -> f.getSeverity() == Severity.MEDIUM));
    }

    @Test
    void shouldIgnorePublicHostnames() {
        String code = """
            public class NetworkConfig {
                private String apiDomain = "api.codexa.internal";
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(Path.of("NetworkConfig.java"), code);
        RuleContext ctx = RuleContext.builder().parsedJavaFile(parsed).build();
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertTrue(findings.isEmpty());
    }
}
