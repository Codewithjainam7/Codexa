package com.codexa.analysis.service;

import com.codexa.analysis.model.ProjectDiagnostics;
import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServletIngressDiscoveryTest {

    private ProjectDiagnosticsCollector collector;
    private JavaAstParserService parserService;

    @BeforeEach
    void setUp() {
        collector = new ProjectDiagnosticsCollector();
        parserService = new JavaAstParserService();
    }

    @Test
    void shouldDiscoverServletWithWebServletAnnotation() {
        String code = """
                package com.demo.servlets;
                import jakarta.servlet.annotation.WebServlet;
                import jakarta.servlet.http.*;
                import java.io.IOException;

                @WebServlet(name = "UserServlet", urlPatterns = {"/api/users", "/users"})
                public class UserServlet extends HttpServlet {
                    @Override
                    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        resp.getWriter().println("user-list");
                    }

                    @Override
                    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        resp.getWriter().println("user-created");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("UserServlet.java"), "UserServlet.java");
        PipelineContext context = new PipelineContext(UUID.randomUUID(), Paths.get("."));
        context.setParsedJavaFiles(List.of(parsed));

        ProjectDiagnostics diag = collector.collect(context);

        assertNotNull(diag.blackBox());
        assertTrue(diag.blackBox().totalEndpoints() >= 2, "Should discover GET and POST methods for UserServlet");
        assertTrue(diag.blackBox().exposedEndpoints().stream().anyMatch(e -> e.path().equals("/api/users")));
        assertEquals(2, diag.blackBox().unauthenticatedEndpoints(), "Endpoints without session auth should be marked unauthenticated");
    }

    @Test
    void shouldDiscoverServletMappedViaWebXml(@TempDir Path tempDir) throws IOException {
        Path webInf = tempDir.resolve("WEB-INF");
        Files.createDirectories(webInf);
        Path webXml = webInf.resolve("web.xml");
        Files.writeString(webXml, """
                <?xml version="1.0" encoding="UTF-8"?>
                <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="3.1">
                    <servlet>
                        <servlet-name>PaymentServlet</servlet-name>
                        <servlet-class>com.demo.servlets.PaymentServlet</servlet-class>
                    </servlet>
                    <servlet-mapping>
                        <servlet-name>PaymentServlet</servlet-name>
                        <url-pattern>/checkout/pay</url-pattern>
                    </servlet-mapping>
                </web-app>
                """);

        String code = """
                package com.demo.servlets;
                import jakarta.servlet.http.*;
                import java.io.IOException;

                public class PaymentServlet extends HttpServlet {
                    @Override
                    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        resp.getWriter().println("paid");
                    }
                }
                """;

        ParsedJavaFile parsed = parserService.parseContent(code, Paths.get("PaymentServlet.java"), "PaymentServlet.java");
        PipelineContext context = new PipelineContext(UUID.randomUUID(), tempDir);
        context.setParsedJavaFiles(List.of(parsed));
        context.setSourceFiles(List.of(webXml));

        ProjectDiagnostics diag = collector.collect(context);

        assertNotNull(diag.blackBox());
        assertTrue(diag.blackBox().totalEndpoints() >= 1);
        assertTrue(diag.blackBox().exposedEndpoints().stream().anyMatch(e -> e.path().equals("/checkout/pay")),
                "Endpoint path should resolve from web.xml mapping");
    }

    @Test
    void perimeterShouldFlagMissingRateLimitingAndMissingHeadersWhenAbsent() {
        PipelineContext context = new PipelineContext(UUID.randomUUID(), Paths.get("."));
        context.setSourceFiles(List.of());

        ProjectDiagnostics diag = collector.collect(context);

        assertNotNull(diag.blackBox().perimeterStatus());
        assertTrue(diag.blackBox().perimeterStatus().rateLimitingStatus().contains("UNPROTECTED"),
                "Should flag unprotected rate limiting");
        assertTrue(diag.blackBox().perimeterStatus().securityHeadersStatus().contains("MISSING HEADERS"),
                "Should flag missing security headers");

        // Verify compliance checklist contains both
        assertTrue(diag.complianceChecklist().stream().anyMatch(c -> c.title().contains("Rate-Limiting")));
        assertTrue(diag.complianceChecklist().stream().anyMatch(c -> c.title().contains("Security Headers")));
    }
}
