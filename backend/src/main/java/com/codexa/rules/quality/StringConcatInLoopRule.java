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
            if (assign.getOperator() == AssignExpr.Operator.PLUS && isStringConcatenation(assign)) {
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

    private boolean isStringConcatenation(AssignExpr assign) {
        // 1. If value contains a string literal, it is definitely string concatenation
        if (!assign.getValue().findAll(com.github.javaparser.ast.expr.StringLiteralExpr.class).isEmpty()) {
            return true;
        }
        for (com.github.javaparser.ast.expr.MethodCallExpr call : assign.getValue().findAll(com.github.javaparser.ast.expr.MethodCallExpr.class)) {
            String mName = call.getNameAsString();
            if (mName.equals("toString") || mName.equals("valueOf") || mName.equals("substring") || mName.equals("replace") || mName.equals("format")) {
                return true;
            }
        }

        String targetName = assign.getTarget().toString().toLowerCase();

        // Numeric counters and metrics are never string concatenation
        if (targetName.contains("count") || targetName.contains("byte") || targetName.contains("size") ||
            targetName.contains("total") || targetName.contains("sum") || targetName.contains("idx") ||
            targetName.contains("index") || targetName.contains("num") || targetName.contains("depth") ||
            targetName.contains("headroom") || targetName.contains("penalty") || targetName.contains("debt") ||
            targetName.contains("complexity") || targetName.contains("score") || targetName.contains("offset") ||
            targetName.contains("read") || targetName.contains("len") || targetName.contains("limit") ||
            targetName.contains("max") || targetName.contains("min") || targetName.equals("i") ||
            targetName.equals("j") || targetName.equals("k")) {
            return false;
        }

        // Check if target variable was declared as String in enclosing method
        com.github.javaparser.ast.body.MethodDeclaration method = assign.findAncestor(com.github.javaparser.ast.body.MethodDeclaration.class).orElse(null);
        if (method != null) {
            for (com.github.javaparser.ast.body.VariableDeclarator var : method.findAll(com.github.javaparser.ast.body.VariableDeclarator.class)) {
                if (var.getNameAsString().equals(assign.getTarget().toString())) {
                    String type = var.getTypeAsString();
                    if (type.equals("String") || type.equals("CharSequence")) {
                        return true;
                    }
                    if (type.equals("int") || type.equals("long") || type.equals("double") || type.equals("float") ||
                        type.equals("short") || type.equals("byte") || type.equals("Integer") || type.equals("Long") ||
                        type.equals("Double") || type.equals("Float")) {
                        return false;
                    }
                }
            }
        }

        return targetName.contains("str") || targetName.contains("text") || targetName.contains("msg") ||
               targetName.contains("message") || targetName.contains("result") || targetName.contains("output") ||
               targetName.contains("html") || targetName.contains("content") || targetName.contains("csv") ||
               targetName.contains("summary") || targetName.contains("body") || targetName.contains("formatted");
    }
}
