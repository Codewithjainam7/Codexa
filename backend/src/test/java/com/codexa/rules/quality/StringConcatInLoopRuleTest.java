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

class StringConcatInLoopRuleTest {

    private StringConcatInLoopRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new StringConcatInLoopRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDetectStringConcatInLoop() {
        String code = """
            import java.util.List;
            public class Formatter {
                public String formatAll(List<String> items) {
                    String result = "";
                    for (String item : items) {
                        result += item + ", ";
                    }
                    return result;
                }
            }
            """;

        ParsedJavaFile parsed = parserService.parseContent(code, Path.of("Formatter.java"), "Formatter.java");
        RuleContext ctx = new RuleContext(parsed, null);
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertEquals(1, findings.size());
        assertEquals(Severity.LOW, findings.get(0).severity());
    }
}
