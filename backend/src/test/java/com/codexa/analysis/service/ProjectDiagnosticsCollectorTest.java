package com.codexa.analysis.service;

import com.codexa.analysis.model.ProjectDiagnostics;
import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDiagnosticsCollectorTest {

    private ProjectDiagnosticsCollector collector;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        collector = new ProjectDiagnosticsCollector();
        parserService = new JavaAstParserService();
    }

    @Test
    void collectDiagnosticsForSampleControllerShouldDetectEndpointsAndMetrics() {
        String code = """
                package com.example.demo;
                import org.springframework.web.bind.annotation.*;
                @RestController
                @RequestMapping("/api/v1/orders")
                public class OrderController {
                    @GetMapping("/{id}")
                    public String getOrder(@PathVariable String id) {
                        if (id == null) return "error";
                        return "order-" + id;
                    }

                    @PostMapping
                    public String createOrder(@RequestBody String body) {
                        return "created";
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("OrderController.java"), "OrderController.java");
        PipelineContext context = new PipelineContext(UUID.randomUUID(), Paths.get("."));
        context.setParsedJavaFiles(List.of(parsed));

        ProjectDiagnostics diag = collector.collect(context);

        assertNotNull(diag);
        assertNotNull(diag.whiteBox());
        assertEquals(1, diag.whiteBox().totalClasses());
        assertEquals(2, diag.whiteBox().totalMethods());
        assertTrue(diag.whiteBox().avgComplexity() >= 1.0);

        assertNotNull(diag.blackBox());
        assertEquals(2, diag.blackBox().totalEndpoints());
        assertTrue(diag.blackBox().exposedEndpoints().stream().anyMatch(e -> e.path().contains("/api/v1/orders")));

        assertNotNull(diag.complianceChecklist());
        assertFalse(diag.complianceChecklist().isEmpty());
    }
}
