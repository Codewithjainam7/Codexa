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

class ParameterSecurityRulesTest {

    private MissingParameterValidationRule missingValidationRule;
    private ReflectedParameterXssRule reflectedXssRule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        missingValidationRule = new MissingParameterValidationRule();
        reflectedXssRule = new ReflectedParameterXssRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void missingValidationRuleShouldFlagMutatingEndpointWithoutValidationAnnotation() {
        String code = """
                package com.example.demo;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                    @PostMapping
                    public String createUser(@RequestBody UserDto dto) {
                        return "ok";
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("UserController.java"), "UserController.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = missingValidationRule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-PARAM-006", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Missing DTO Input Validation"));
    }

    @Test
    void missingValidationRuleShouldPassWhenValidAnnotationIsPresent() {
        String code = """
                package com.example.demo;
                import org.springframework.web.bind.annotation.*;
                import jakarta.validation.Valid;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                    @PostMapping
                    public String createUser(@Valid @RequestBody UserDto dto) {
                        return "ok";
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("UserController.java"), "UserController.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = missingValidationRule.evaluate(context);
        assertTrue(findings.isEmpty());
    }

    @Test
    void reflectedXssRuleShouldDetectDirectParameterReflectionInServletResponse() {
        String code = """
                package com.demo.servlets;
                import jakarta.servlet.http.*;
                import java.io.IOException;

                public class SearchServlet extends HttpServlet {
                    @Override
                    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        String query = req.getParameter("q");
                        resp.getWriter().println("<html>Search results for: " + req.getParameter("q") + "</html>");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("SearchServlet.java"), "SearchServlet.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = reflectedXssRule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-PARAM-007", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Insecure Parameter Reflection") || findings.get(0).description().contains("Reflected Cross-Site Scripting"));
    }
}
