package com.codexa.rules.quality;

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
import com.github.javaparser.ast.body.FieldDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ArchitectureSeparationRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-005";
    }

    @Override
    public String getName() {
        return "Controller Direct Persistence Access";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
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
        return "Architectural Cleanliness";
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

            for (FieldDeclaration field : clazz.getFields()) {
                String fieldType = field.getElementType().asString();
                if (fieldType.endsWith("Repository") || fieldType.endsWith("Dao") || fieldType.equals("EntityManager") || fieldType.equals("JdbcTemplate")) {
                    int startLine = field.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = field.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(field, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Direct persistence dependency ('" + fieldType + "') injected in Controller")
                            .description("Controller '" + clazz.getNameAsString() + "' directly injects data access component '" + fieldType + "'. Controllers should only handle HTTP concerns and delegate business logic and persistence to dedicated @Service layer classes.")
                            .impact("Tightly coupled presentation and persistence layers; difficult to mock or apply centralized transaction boundaries.")
                            .remediation("Introduce a dedicated @Service layer class between this Controller and the Repository.")
                            .suggestedFix("// Inject a Service instead:\n@Autowired\nprivate UserService userService;")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/postconstruct-and-predestroy-annotations.html"))
                            .build());
                }
            }
        });

        return findings;
    }
}
