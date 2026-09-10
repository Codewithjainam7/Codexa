package com.codexa.rules.operations;

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

class UnboundedThreadPoolRuleTest {

    private UnboundedThreadPoolRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new UnboundedThreadPoolRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectNewCachedThreadPool() {
        String code = """
            import java.util.concurrent.Executors;
            import java.util.concurrent.ExecutorService;
            public class JobWorker {
                public ExecutorService start() {
                    return Executors.newCachedThreadPool();
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("JobWorker.java"), "JobWorker.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(1, findings.size());
        assertEquals(Severity.MEDIUM, findings.get(0).severity());
    }
}
