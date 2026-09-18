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

    @Test
    void collectDiagnosticsForPolyglotRepoShouldDetectEndpointsAndWhiteBoxMetrics(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path srcDir = tempDir.resolve("src");
        java.nio.file.Files.createDirectories(srcDir);
        java.nio.file.Path appFile = srcDir.resolve("App.tsx");
        java.nio.file.Files.writeString(appFile, """
                interface UserProps {
                    name: string;
                    role: string;
                }

                export const App = ({ name, role }: UserProps) => {
                    const checkAccess = (r: string) => {
                        if (r === 'admin') {
                            return true;
                        } else if (r === 'moderator') {
                            return true;
                        }
                        return false;
                    };

                    return <div>{name}</div>;
                };
                """);

        java.nio.file.Path funcDir = tempDir.resolve("supabase/functions/resident-requests");
        java.nio.file.Files.createDirectories(funcDir);
        java.nio.file.Path edgeFile = funcDir.resolve("index.ts");
        java.nio.file.Files.writeString(edgeFile, """
                import { serve } from "https://deno.land/std/http/server.ts";

                serve(async (req) => {
                    const body = await req.json();
                    return new Response(JSON.stringify({ status: "created" }));
                });
                """);

        java.nio.file.Path viteFile = tempDir.resolve("vite.config.ts");
        java.nio.file.Files.writeString(viteFile, """
                import { defineConfig } from 'vite';
                export default defineConfig({
                    server: {
                        middlewares: {
                            use: (path, handler) => {}
                        }
                    }
                });
                """);

        PipelineContext context = new PipelineContext(UUID.randomUUID(), tempDir);
        context.setSourceFiles(List.of(appFile, edgeFile, viteFile));

        ProjectDiagnostics diag = collector.collect(context);

        assertNotNull(diag);
        assertNotNull(diag.whiteBox());
        assertTrue(diag.whiteBox().totalMethods() >= 2, "Should count polyglot methods/functions in TypeScript");
        assertFalse(diag.whiteBox().topComplexFiles().isEmpty(), "Top complex files must not be empty for polyglot code");
        assertTrue(diag.whiteBox().peakComplexity() >= 2, "Peak complexity should be calculated from decision branches");

        assertNotNull(diag.blackBox());
        assertTrue(diag.blackBox().totalEndpoints() >= 1, "Should discover Supabase edge function endpoints");
        assertTrue(diag.blackBox().exposedEndpoints().stream().anyMatch(e -> e.path().contains("resident-requests")), "Should discover /functions/v1/resident-requests");
        assertTrue(diag.blackBox().unauthenticatedEndpoints() >= 1, "Unauthenticated edge function must be counted");
    }
}
