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
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule CR-PARAM-006: Flags mutating controller endpoints accepting DTO request bodies without @Valid or @Validated.
 */
@Component
public class MissingParameterValidationRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-PARAM-006";
    }

    @Override
    public String getName() {
        return "Missing Input & Parameter Validation (@Valid / @Validated)";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A04:2021 - Insecure Design";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        String relPath = parsedFile.getRelativePath();
        String lowerPath = relPath.toLowerCase();

        // Skip tests and rule engines
        if (lowerPath.contains("test") || lowerPath.contains("rule") || lowerPath.contains("fixture")) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            boolean isController = clazz.getAnnotations().stream().anyMatch(a ->
                    a.getNameAsString().endsWith("Controller") || a.getNameAsString().equals("RestController"));
            if (!isController) continue;

            for (MethodDeclaration method : clazz.getMethods()) {
                boolean isMutatingEndpoint = method.getAnnotations().stream().anyMatch(a -> {
                    String name = a.getNameAsString();
                    return name.equals("PostMapping") || name.equals("PutMapping") || name.equals("PatchMapping")
                            || (name.equals("RequestMapping") && isMutatingRequestMapping(a));
                });

                if (!isMutatingEndpoint) continue;

                for (Parameter param : method.getParameters()) {
                    boolean isRequestBody = param.getAnnotations().stream().anyMatch(a ->
                            a.getNameAsString().equals("RequestBody") || a.getNameAsString().equals("ModelAttribute"));

                    if (!isRequestBody) continue;

                    // Exclude primitive or standard framework types (e.g. String, byte[], MultipartFile, Principal)
                    String paramType = param.getTypeAsString();
                    if (paramType.equals("String") || paramType.equals("byte[]") || paramType.contains("MultipartFile")
                            || paramType.contains("HttpServletRequest") || paramType.contains("Principal")
                            || paramType.contains("Authentication")) {
                        continue;
                    }

                    boolean hasValidation = param.getAnnotations().stream().anyMatch(a -> {
                        String name = a.getNameAsString();
                        return name.equals("Valid") || name.equals("Validated") || name.equals("NotNull");
                    });

                    if (!hasValidation) {
                        int line = param.getBegin().map(p -> p.line).orElse(1);
                        int endLine = param.getEnd().map(p -> p.line).orElse(line);
                        String evidence = snippetExtractor.extractNodeSnippet(param, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .title("Missing DTO Input Validation on @" + param.getNameAsString())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .owaspMapping(getOwaspMapping())
                                .filePath(relPath)
                                .startLine(line)
                                .endLine(endLine)
                                .evidence(evidence)
                                .description("Mutating HTTP endpoint (" + method.getNameAsString() + ") accepts complex payload parameter '"
                                        + param.getNameAsString() + "' of type " + paramType + " without @Valid or @Validated Bean Validation.")
                                .impact("Unvalidated user payloads allow malformed, empty, out-of-range, or malicious parameter inputs to reach service and database layers.")
                                .remediation("Add the @Valid or @Validated annotation to the @RequestBody parameter and enforce constraints (@NotBlank, @Size, @Pattern) on DTO fields.")
                                .suggestedFix("@PostMapping\npublic ResponseEntity<?> handleRequest(@Valid @RequestBody " + paramType + " " + param.getNameAsString() + ") { ... }")
                                .references(List.of("https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html"))
                                .build());
                    }
                }
            }
        }

        return findings;
    }

    private boolean isMutatingRequestMapping(AnnotationExpr annotation) {
        String str = annotation.toString().toUpperCase();
        return str.contains("POST") || str.contains("PUT") || str.contains("PATCH");
    }
}
