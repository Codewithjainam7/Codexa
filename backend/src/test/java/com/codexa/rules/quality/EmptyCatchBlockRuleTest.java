package com.codexa.rules.quality;

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

class EmptyCatchBlockRuleTest {

    private EmptyCatchBlockRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new EmptyCatchBlockRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectEmptyCatchBlock() {
        String code = """
            public class QuietWorker {
                public void work() {
                    try {
                        doSomething();
                    } catch (Exception ex) {
                        // Empty catch
                    }
                }
                private void doSomething() {}
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("QuietWorker.java"), "QuietWorker.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(1, findings.size());
        assertEquals(Severity.MEDIUM, findings.get(0).severity());
    }
}
