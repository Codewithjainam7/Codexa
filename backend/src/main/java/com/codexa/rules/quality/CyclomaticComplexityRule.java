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
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CyclomaticComplexityRule implements AnalysisRule {

    private static final int COMPLEXITY_THRESHOLD = 15;
    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-001";
    }

    @Override
    public String getName() {
        return "High Cyclomatic Complexity";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
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
            int complexity = calculateComplexity(method);
            if (complexity > COMPLEXITY_THRESHOLD) {
                int startLine = method.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = method.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("High cyclomatic complexity (" + complexity + " > " + COMPLEXITY_THRESHOLD + ") in '" + method.getNameAsString() + "'")
                        .description("Method '" + method.getNameAsString() + "' has a cyclomatic complexity of " + complexity + ". High complexity indicates excessive branching, making the method difficult to unit-test and prone to regression defects.")
                        .impact("Decreased testability, increased bug density, and high maintenance overhead.")
                        .remediation("Refactor and decompose this method into smaller, single-responsibility helper methods or leverage strategy patterns.")
                        .suggestedFix("// Break down '" + method.getNameAsString() + "' into discrete private helper functions.")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://en.wikipedia.org/wiki/Cyclomatic_complexity"))
                        .build());
            }
        });

        return findings;
    }

    private int calculateComplexity(MethodDeclaration method) {
        int complexity = 1;
        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(ForEachStmt.class).size();
        complexity += method.findAll(CatchClause.class).size();
        complexity += method.findAll(ConditionalExpr.class).size();
        complexity += (int) method.findAll(SwitchEntry.class).stream().filter(e -> !e.getLabels().isEmpty()).count();
        complexity += (int) method.findAll(BinaryExpr.class).stream()
                .filter(b -> b.getOperator() == BinaryExpr.Operator.AND || b.getOperator() == BinaryExpr.Operator.OR)
                .count();

        return complexity;
    }
}
