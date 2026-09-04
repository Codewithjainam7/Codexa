package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class MissingAccessControlRule implements AnalysisRule {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "admin", "manage", "delete", "destroy", "role", "permission", "payment", "billing", "config", "internal"
    );

    private static final Set<String> AUTH_ANNOTATIONS = Set.of(
            "PreAuthorize", "Secured", "RolesAllowed", "PermitAll", "DenyAll"
    );

    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
            "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping", "RequestMapping"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-AUTH-001";
    }

    @Override
    public String getName() {
        return "Missing Access Control or Authorization on Sensitive Endpoint";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.HIGH;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.MEDIUM;
    }

    @Override
    public String getOwaspMapping() {
        return "A01:2025 - Broken Access Control";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            boolean isController = clazz.isAnnotationPresent("RestController") || clazz.isAnnotationPresent("Controller");
            if (!isController) {
                return;
            }

            boolean classHasAuth = hasAuthAnnotation(clazz.getAnnotations());

            for (MethodDeclaration method : clazz.getMethods()) {
                if (isEndpointMethod(method) && !classHasAuth && !hasAuthAnnotation(method.getAnnotations())) {
                    String methodName = method.getNameAsString().toLowerCase();
                    String methodText = method.toString().toLowerCase();

                    boolean isSensitive = SENSITIVE_KEYWORDS.stream().anyMatch(kw -> methodName.contains(kw) || methodText.contains(kw));

                    if (isSensitive) {
                        int startLine = method.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = method.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Sensitive controller endpoint missing authorization check")
                                .description("Endpoint method '" + method.getNameAsString() + "' appears to perform privileged or administrative actions but lacks method-level authorization annotations (@PreAuthorize, @Secured, @RolesAllowed).")
                                .impact("Unauthorized access to restricted resources and privilege escalation.")
                                .remediation("Enforce strict role-based access control using Spring Security annotations (e.g., @PreAuthorize(\"hasRole('ADMIN')\")) or ensure centralized security configuration filters this path.")
                                .suggestedFix("@PreAuthorize(\"hasRole('ADMIN')\")\n" + evidence)
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .requiresManualReview(true)
                                .references(List.of("https://owasp.org/Top10/2025/A01_2025-Broken_Access_Control/", "https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html"))
                                .build());
                    }
                }
            }
        });

        return findings;
    }

    private boolean isEndpointMethod(MethodDeclaration method) {
        return method.getAnnotations().stream().anyMatch(a -> MAPPING_ANNOTATIONS.contains(a.getNameAsString()));
    }

    private boolean hasAuthAnnotation(List<AnnotationExpr> annotations) {
        return annotations.stream().anyMatch(a -> AUTH_ANNOTATIONS.contains(a.getNameAsString()));
    }
}
