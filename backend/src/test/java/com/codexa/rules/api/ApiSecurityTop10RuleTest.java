package com.codexa.rules.api;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApiSecurityTop10RuleTest {

    private final ApiSecurityTop10Rule rule = new ApiSecurityTop10Rule();
    private final JavaParser javaParser = new JavaParser();

    @Test
    @DisplayName("Should detect Mass Assignment BOPLA and unpaginated bulk API")
    void testBoplaAndUnboundedPagination() {
        String code = """
                package com.example;
                import org.springframework.web.bind.annotation.*;
                import java.util.List;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                    @PostMapping
                    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
                        return null;
                    }

                    @GetMapping
                    public List<UserEntity> getAllUsers() {
                        return null;
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        ParsedJavaFile parsedJavaFile = new ParsedJavaFile(Path.of("src/UserController.java"), "src/UserController.java", cu);

        PipelineContext pCtx = new PipelineContext(UUID.randomUUID(), Path.of("."));
        RuleContext context = new RuleContext(parsedJavaFile, pCtx);

        List<RuleFinding> findings = rule.evaluate(context);
        assertFalse(findings.isEmpty(), "Expected API security findings");

        boolean hasBopla = findings.stream().anyMatch(f -> "CR-API-001".equals(f.ruleId()));
        boolean hasPagination = findings.stream().anyMatch(f -> "CR-API-002".equals(f.ruleId()));

        assertTrue(hasBopla, "Should detect Mass Assignment entity binding (CR-API-001)");
        assertTrue(hasPagination, "Should detect unpaginated collection return (CR-API-002)");
    }
}
