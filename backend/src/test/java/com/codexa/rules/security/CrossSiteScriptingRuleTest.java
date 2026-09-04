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

class CrossSiteScriptingRuleTest {

    private CrossSiteScriptingRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new CrossSiteScriptingRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateDirectResponseWriterShouldFlagXssFinding() {
        String code = """
                package com.example;
                import jakarta.servlet.http.HttpServletResponse;
                public class XssDemo {
                    public void render(HttpServletResponse response, String userText) throws Exception {
                        response.getWriter().write("<div>" + userText + "</div>");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("XssDemo.java"), "XssDemo.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-XSS-001", findings.get(0).ruleId());
    }
}
