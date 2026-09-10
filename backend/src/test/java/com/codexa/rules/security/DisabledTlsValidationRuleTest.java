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

class DisabledTlsValidationRuleTest {

    private DisabledTlsValidationRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new DisabledTlsValidationRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectEmptyTrustManagerMethods() {
        String code = """
            import javax.net.ssl.X509TrustManager;
            import java.security.cert.X509Certificate;
            public class TrustAllManager implements X509TrustManager {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("TrustAllManager.java"), "TrustAllManager.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(2, findings.size());
        assertTrue(findings.stream().allMatch(f -> f.severity() == Severity.CRITICAL));
    }
}
