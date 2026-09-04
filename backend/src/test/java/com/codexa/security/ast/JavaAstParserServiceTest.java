package com.codexa.security.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaAstParserServiceTest {

    private JavaAstParserService astParserService;
    private AstSnippetExtractor snippetExtractor;

    @BeforeEach
    void setUp() {
        astParserService = new JavaAstParserService();
        snippetExtractor = new AstSnippetExtractor();
    }

    @Test
    void parseContentValidClassShouldProduceCompilationUnit() {
        String code = """
                package com.example.demo;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {

                    @GetMapping("/api/users")
                    public String getUsers() {
                        return "users";
                    }
                }
                """;

        Path fakePath = Paths.get("src/main/java/com/example/demo/UserController.java");
        ParsedJavaFile result = astParserService.parseContent(code, fakePath, "UserController.java");

        assertTrue(result.isParseSuccessful());
        assertTrue(result.getCompilationUnit().isPresent());
        assertTrue(result.getParseErrors().isEmpty());

        var cu = result.getCompilationUnit().get();
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        assertEquals(1, methods.size());
        assertEquals("getUsers", methods.get(0).getNameAsString());

        // Verify snippet extraction
        String snippet = snippetExtractor.extractNodeSnippet(methods.get(0), result.getLines());
        assertTrue(snippet.contains("public String getUsers()"));
    }

    @Test
    void parseContentMalformedSyntaxShouldGracefullyHandleErrorsWithoutThrowing() {
        String invalidCode = """
                public class BrokenClass {
                    public void missingBrace( {
                        int x = 10;
                """;

        Path fakePath = Paths.get("BrokenClass.java");
        ParsedJavaFile result = astParserService.parseContent(invalidCode, fakePath, "BrokenClass.java");

        // Should return a result object with errors without throwing unhandled exceptions
        assertNotNull(result);
        assertFalse(result.getParseErrors().isEmpty());
    }

    @Test
    void getSnippetFromParsedFileShouldReturnCorrectLineRange() {
        String code = """
                line 1
                line 2
                line 3
                line 4
                line 5
                """;

        ParsedJavaFile file = astParserService.parseContent(code, Paths.get("Test.java"), "Test.java");
        String snippet = file.getSnippet(2, 4);

        assertTrue(snippet.contains("line 2"));
        assertTrue(snippet.contains("line 3"));
        assertTrue(snippet.contains("line 4"));
        assertFalse(snippet.contains("line 1"));
        assertFalse(snippet.contains("line 5"));
    }
}
