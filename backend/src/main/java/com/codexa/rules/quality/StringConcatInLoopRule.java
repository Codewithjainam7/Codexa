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
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StringConcatInLoopRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-PERF-001";
    }

    @Override
    public String getName() {
        return "String Concatenation in Loop";
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
        return "Code Efficiency & Performance";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) return findings;

        CompilationUnit cu = parsedFile.getCompilationUnit().get();
        String filename = parsedFile.getRelativePath();

        for (AssignExpr assign : cu.findAll(AssignExpr.class)) {
            if (assign.getOperator() == AssignExpr.Operator.PLUS) {
                boolean inLoop = assign.findAncestor(ForStmt.class).isPresent()
                        || assign.findAncestor(ForEachStmt.class).isPresent()
                        || assign.findAncestor(WhileStmt.class).isPresent()
                        || assign.findAncestor(DoStmt.class).isPresent();

                if (inLoop) {
                    int startLine = assign.getBegin().map(p -> p.line).orElse(1);
                    int endLine = assign.getEnd().map(p -> p.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(assign, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("String Concatenation in Loop via +=")
                            .description("String concatenation (+ or +=) inside a loop creates repetitive intermediate String and StringBuilder allocations with O(N^2) memory overhead.")
                            .impact("Excessive GC pressure, elevated heap churn, and degraded performance under large datasets.")
                            .remediation("Use a single StringBuilder initialized outside the loop and call .append() inside.")
                            .suggestedFix("StringBuilder sb = new StringBuilder();\nfor (...) {\n    sb.append(item);\n}")
                            .owaspMapping(getOwaspMapping())
                            .filePath(filename)
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://wiki.sei.cmu.edu/confluence/display/java/STR01-J.+Do+not+assume+that+the+String+concatenation+operator+is+efficient"))
                            .build());
                }
            }
        }

        return findings;
    }
}
