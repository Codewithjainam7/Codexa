package com.codexa.rules.llm;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LlmApplicationSecurityRuleTest {

    private final LlmApplicationSecurityRule rule = new LlmApplicationSecurityRule();
    private final JavaParser javaParser = new JavaParser();

    @Test
    @DisplayName("Should detect Prompt Injection string concatenation in prompt variable")
    void testPromptInjectionDetection() {
        String code = """
                public class AiService {
                    public String ask(String userInput) {
                        String systemPrompt = "You are a helpful assistant.";
                        String prompt = systemPrompt + "\\nUser question: " + userInput;
                        return client.call(prompt);
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        ParsedJavaFile parsedJavaFile = new ParsedJavaFile(Path.of("src/AiService.java"), "src/AiService.java", cu);

        PipelineContext pCtx = new PipelineContext(UUID.randomUUID(), Path.of("."));
        RuleContext context = new RuleContext(parsedJavaFile, pCtx);

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.stream().anyMatch(f -> "CR-LLM-001".equals(f.ruleId())), "Should flag Prompt Injection (CR-LLM-001)");
    }
}
