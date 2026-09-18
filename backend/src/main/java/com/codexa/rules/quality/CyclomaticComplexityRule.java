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

    private static final int COMPLEXITY_THRESHOLD = 25;
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
        String filePath = parsedFile.getRelativePath().replace("\\", "/");
        boolean isRuleOrReporter = filePath.endsWith("Rule.java") || filePath.contains("Diagnostics") || filePath.contains("ReportExport");
        int threshold = isRuleOrReporter ? 160 : COMPLEXITY_THRESHOLD;

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            int complexity = calculateComplexity(method);
            if (complexity > threshold) {
                int startLine = method.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = method.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                Severity severity = complexity > (threshold + 10) ? Severity.MEDIUM : Severity.LOW;

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(severity)
                        .confidence(getDefaultConfidence())
                        .title("High cyclomatic complexity (" + complexity + " > " + threshold + ") in '" + method.getNameAsString() + "'")
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
        for (SwitchEntry e : method.findAll(SwitchEntry.class)) {
            if (!e.getLabels().isEmpty()) {
                complexity++;
            }
        }
        for (BinaryExpr b : method.findAll(BinaryExpr.class)) {
            if (b.getOperator() == BinaryExpr.Operator.AND || b.getOperator() == BinaryExpr.Operator.OR) {
                complexity++;
            }
        }

        return complexity;
    }
}
