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

class InsecureDeserializationRuleTest {

    private InsecureDeserializationRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new InsecureDeserializationRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateObjectInputStreamShouldFlagCriticalFinding() {
        String code = """
                package com.example;
                import java.io.ObjectInputStream;
                public class SessionLoader {
                    public Object deserialize(ObjectInputStream in) throws Exception {
                        return in.readObject();
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("SessionLoader.java"), "SessionLoader.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-SEC-005", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Unsafe Java Deserialization"));
    }
}
