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

class CommandInjectionRuleTest {

    private CommandInjectionRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new CommandInjectionRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateRuntimeExecShouldFlagCriticalFinding() {
        String code = """
                package com.example;
                public class ShellRunner {
                    public void run(String userInput) throws Exception {
                        Runtime.getRuntime().exec("ping " + userInput);
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("ShellRunner.java"), "ShellRunner.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-CMD-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Runtime.getRuntime().exec()"));
    }

    @Test
    void evaluateProcessBuilderShouldFlagCriticalFinding() {
        String code = """
                package com.example;
                public class ProcessRunner {
                    public void run(String param) throws Exception {
                        ProcessBuilder pb = new ProcessBuilder("sh", "-c", param);
                        pb.start();
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("ProcessRunner.java"), "ProcessRunner.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-CMD-001", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("ProcessBuilder"));
    }
}
