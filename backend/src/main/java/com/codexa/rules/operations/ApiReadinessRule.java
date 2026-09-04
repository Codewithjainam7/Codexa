package com.codexa.rules.operations;

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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiReadinessRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-OPS-004";
    }

    @Override
    public String getName() {
        return "API Input Validation Hygiene";
    }

    @Override
    public Category getCategory() {
        return Category.OPERATIONS;
    }

    @Override
    public Severity getSeverity() {
        return Severity.LOW;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "Operational Hygiene";
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

            for (MethodDeclaration method : clazz.getMethods()) {
                for (Parameter param : method.getParameters()) {
                    boolean hasRequestBody = param.isAnnotationPresent("RequestBody");
                    boolean hasValid = param.isAnnotationPresent("Valid") || param.isAnnotationPresent("Validated");

                    if (hasRequestBody && !hasValid) {
                        int startLine = param.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = param.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(param, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Unvalidated @RequestBody parameter ('" + param.getNameAsString() + "')")
                                .description("Controller endpoint parameter '" + param.getNameAsString() + "' accepts JSON payload via @RequestBody without @Valid or @Validated annotations. Unvalidated inputs can trigger NullPointerExceptions and corrupt database state.")
                                .impact("Unchecked invalid payloads entering application domain logic.")
                                .remediation("Add @Valid annotation to request body parameter and define Bean Validation constraints (@NotBlank, @NotNull, @Size) on DTO fields.")
                                .suggestedFix("public ResponseEntity<?> handleRequest(@Valid @RequestBody " + param.getTypeAsString() + " " + param.getNameAsString() + ")")
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://spring.io/guides/gs/validating-form-input"))
                                .build());
                    }
                }
            }
        });

        return findings;
    }
}
