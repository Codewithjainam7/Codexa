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
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LongMethodRule implements AnalysisRule {

    private static final int MAX_METHOD_LINES = 50;
    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-002";
    }

    @Override
    public String getName() {
        return "Excessive Method Length";
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
        return "Code Maintainability";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getRange().isPresent()) {
                var range = method.getRange().get();
                int lineCount = range.end.line - range.begin.line + 1;
                if (lineCount > MAX_METHOD_LINES) {
                    String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Long method ('" + method.getNameAsString() + "' is " + lineCount + " lines > " + MAX_METHOD_LINES + ")")
                            .description("Method '" + method.getNameAsString() + "' spans " + lineCount + " lines. Long methods tend to violate the Single Responsibility Principle and obscure logic errors.")
                            .impact("Decreased code readability and elevated cognitive load during maintenance.")
                            .remediation("Extract sub-operations into dedicated private helper methods.")
                            .suggestedFix("// Extract logical steps in '" + method.getNameAsString() + "' into separate methods.")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(range.begin.line)
                            .endLine(range.end.line)
                            .evidence(evidence)
                            .references(List.of("https://refactoring.guru/smells/long-method"))
                            .build());
                }
            }
        });

        return findings;
    }
}
