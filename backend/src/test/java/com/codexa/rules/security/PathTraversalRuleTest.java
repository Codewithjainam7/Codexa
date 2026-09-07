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

class PathTraversalRuleTest {

    private PathTraversalRule rule;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        rule = new PathTraversalRule();
        parserService = new JavaAstParserService();
    }

    @Test
    void evaluateVulnerablePathResolutionShouldFlagFinding() {
        String code = """
                package com.example;
                import java.io.File;
                public class FileDownloader {
                    public File download(String userPath) {
                        return new File("/uploads/" + userPath);
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("FileDownloader.java"), "FileDownloader.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("CR-SEC-003", findings.get(0).ruleId());
        assertTrue(findings.get(0).title().contains("Path Traversal"));
    }

    @Test
    void evaluateSafeNormalizedPathShouldNotFlagIfLiteral() {
        String code = """
                package com.example;
                import java.io.File;
                public class FileDownloader {
                    public File download() {
                        return new File("/uploads/static.txt");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("FileDownloader.java"), "FileDownloader.java");
        RuleContext context = new RuleContext(parsed, new PipelineContext(UUID.randomUUID(), Paths.get(".")));

        List<RuleFinding> findings = rule.evaluate(context);
        assertTrue(findings.isEmpty());
    }
}
