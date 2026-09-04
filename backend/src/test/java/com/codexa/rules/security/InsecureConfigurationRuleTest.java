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

class InsecureConfigurationRuleTest {

    private InsecureConfigurationRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new InsecureConfigurationRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateWildcardCorsShouldFlagFinding() {
        String code = """
                package com.example;
                import org.springframework.web.servlet.config.annotation.CorsRegistry;
                public class WebConfig {
                    public void addCorsMappings(CorsRegistry registry) {
                        registry.addMapping("/**").allowedOrigins("*");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("WebConfig.java"), "WebConfig.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-CONFIG-001", findings.get(0).ruleId());
    }
}
